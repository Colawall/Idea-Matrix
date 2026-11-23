package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.local.AppDatabase
import com.example.data.local.AuditEntity
import com.example.data.local.DailyCheckInEntity
import com.example.data.local.IdeaEntity
import com.example.data.model.AppNotification
import com.example.data.model.CollaboratorConnection
import com.example.data.model.ExecutionStep
import com.example.data.model.IdeaActivity
import com.example.data.model.IdeaMember
import com.example.data.model.IdeaResource
import com.example.data.model.IdeaRole
import com.example.data.model.ReminderConfig
import com.example.data.model.ReminderFrequency
import com.example.data.model.RequirementType
import com.example.data.model.TimingStatus
import com.example.data.model.UserProfile
import com.example.data.model.WorkSessionLog
import com.example.data.repository.CollaborationRepository
import com.example.data.repository.IdeaRepository
import com.example.reminders.ReminderScheduler
import com.example.ui.util.NotificationHelper
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class FeelingEffectType {
    THUNDER_LIGHTNING,
    FLAMES,
    ROCKET_TAKEOFF,
    PLANT_SPURT,
    BATTERY_CHARGING
}

data class DailyFeelingOption(
    val emoji: String = "",
    val title: String,
    val readiness: Int,
    val effectType: FeelingEffectType = FeelingEffectType.THUNDER_LIGHTNING
)

class IdeaViewModel(application: Application) : AndroidViewModel(application) {
    private val appDb = AppDatabase.getInstance(application)
    private val ideaDao = appDb.ideaDao()
    private val checkInDao = appDb.dailyCheckInDao()
    private val auditDao = appDb.auditDao()
    private val repository = IdeaRepository(ideaDao, checkInDao, auditDao)
    private val prefs = application.getSharedPreferences("idea_matrix_prefs", Context.MODE_PRIVATE)

    // Firebase Auth and Collaboration Repositories
    private val authRepository = AuthRepository.create(application)
    private val collaborationRepository = CollaborationRepository.create(application)

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
    val userProfile: StateFlow<UserProfile?> = authRepository.userProfile
    val isAuthLoading: StateFlow<Boolean> = authRepository.isLoading
    val authError: StateFlow<String?> = authRepository.authError

    private val _isGuestMode = MutableStateFlow(
        prefs.getBoolean("guest_mode_enabled", false)
    )
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    fun setGuestMode(enabled: Boolean) {
        _isGuestMode.value = enabled
        prefs.edit().putBoolean("guest_mode_enabled", enabled).apply()
    }

    private val _connections = MutableStateFlow<List<CollaboratorConnection>>(emptyList())
    val connections: StateFlow<List<CollaboratorConnection>> = _connections.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private var collabSyncJob: Job? = null
    private var connectionsJob: Job? = null
    private var notificationsJob: Job? = null
    private val notifiedNotificationIds = mutableSetOf<String>()

    fun getTodayDateString(): String {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault())
        val y = cal.get(java.util.Calendar.YEAR)
        val m = cal.get(java.util.Calendar.MONTH) + 1
        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format(java.util.Locale.US, "%04d-%02d-%02d", y, m, d)
    }

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode = _themeMode.asStateFlow()

    private val _userName = MutableStateFlow(
        prefs.getString("user_name", "Alex") ?: "Alex"
    )
    val userName = _userName.asStateFlow()

    fun updateUserName(name: String) {
        val clean = name.trim().ifBlank { "Alex" }
        _userName.value = clean
        prefs.edit().putString("user_name", clean).apply()
    }

    fun getFirstName(): String {
        val profileName = userProfile.value?.displayName
        val nameToUse = if (!profileName.isNullOrBlank()) profileName else _userName.value
        return nameToUse.trim().split(Regex("\\s+")).firstOrNull { it.isNotBlank() }?.replaceFirstChar { it.uppercase() } ?: "Alex"
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun toggleDarkMode() {
        val next = when (_themeMode.value) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(next)
    }

    val feelingOptions = listOf(
        DailyFeelingOption("", "Fired Up", 95, FeelingEffectType.THUNDER_LIGHTNING),
        DailyFeelingOption("", "Super Ready", 85, FeelingEffectType.FLAMES),
        DailyFeelingOption("", "Getting There", 70, FeelingEffectType.ROCKET_TAKEOFF),
        DailyFeelingOption("", "Explorer", 55, FeelingEffectType.PLANT_SPURT),
        DailyFeelingOption("", "Recharging", 40, FeelingEffectType.BATTERY_CHARGING)
    )

    private val _shouldShowDailyFeelingButtons = MutableStateFlow(
        prefs.getString("last_checkin_completed_date", "") != getTodayDateString()
    )
    val shouldShowDailyFeelingButtons = _shouldShowDailyFeelingButtons.asStateFlow()

    private val _activeEffectOption = MutableStateFlow<DailyFeelingOption?>(null)
    val activeEffectOption = _activeEffectOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _selectedFeeling = MutableStateFlow<DailyFeelingOption?>(null)
    val selectedFeeling = _selectedFeeling.asStateFlow()

    private val _showNotificationDialog = MutableStateFlow(false)
    val showNotificationDialog = _showNotificationDialog.asStateFlow()

    val allIdeas: StateFlow<List<IdeaEntity>>
    val filteredIdeas: StateFlow<List<IdeaEntity>>
    val topFeaturedIdeas: StateFlow<List<IdeaEntity>>
    val allCheckIns: StateFlow<List<DailyCheckInEntity>>
    val latestCheckIn: StateFlow<DailyCheckInEntity?>

    init {
        viewModelScope.launch {
            repository.ensureInitialSeedData()
        }

        allIdeas = repository.allIdeas.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        topFeaturedIdeas = repository.topFeaturedIdeas.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCheckIns = repository.allCheckIns.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        latestCheckIn = repository.latestCheckIn.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        filteredIdeas = combine(allIdeas, _searchQuery, _selectedFilter) { ideas, query, filter ->
            ideas.filter { idea ->
                val matchesQuery = query.isBlank() ||
                        idea.title.contains(query, ignoreCase = true) ||
                        idea.description.contains(query, ignoreCase = true) ||
                        idea.category.contains(query, ignoreCase = true) ||
                        idea.neededRequirements.contains(query, ignoreCase = true)

                val matchesFilter = when (filter) {
                    "All" -> true
                    "High Readiness" -> idea.readinessScore >= 75
                    "In Progress" -> idea.status == "In Progress" || (idea.readinessScore in 50..74)
                    "Validating" -> idea.status == "Validating" || idea.status == "Exploring"
                    else -> idea.category.equals(filter, ignoreCase = true)
                }

                matchesQuery && matchesFilter
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Observe Auth changes to start/stop Firestore collaboration sync
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    startCollaborationSync(user.uid)
                } else {
                    stopCollaborationSync()
                }
            }
        }
    }

    // ==========================================
    // AUTHENTICATION
    // ==========================================

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activity)
            result.onSuccess {
                setGuestMode(false)
                // If profile is ready, seed existing ideas to Firestore
                val profile = userProfile.value
                if (profile != null) {
                    syncExistingIdeasToCloud(profile)
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        setGuestMode(false)
        stopCollaborationSync()
    }

    fun clearAuthError() {
        authRepository.clearError()
    }

    // ==========================================
    // REAL-TIME COLLABORATION SYNC
    // ==========================================

    private fun startCollaborationSync(userId: String) {
        stopCollaborationSync()

        // 1. Sync Ideas from Firestore
        collabSyncJob = viewModelScope.launch {
            collaborationRepository.observeIdeas(userId).collect { remoteIdeas ->
                for (remote in remoteIdeas) {
                    val existing = ideaDao.getIdeaByFirestoreId(remote.firestoreId)
                    if (existing != null) {
                        val merged = remote.copy(id = existing.id)
                        ideaDao.updateIdea(merged)
                        if (_selectedIdeaForDetail.value?.firestoreId == remote.firestoreId) {
                            _selectedIdeaForDetail.value = merged
                        }
                    } else {
                        ideaDao.insertIdea(remote)
                    }
                }
            }
        }

        // 2. Sync Connections
        connectionsJob = viewModelScope.launch {
            collaborationRepository.observeConnections(userId).collect { list ->
                _connections.value = list
            }
        }

        // 3. Sync Notifications & trigger push notification alerts
        notificationsJob = viewModelScope.launch {
            collaborationRepository.observeNotifications(userId).collect { list ->
                _notifications.value = list
                val newUnread = list.filter { !it.isRead && !notifiedNotificationIds.contains(it.id) }
                for (notif in newUnread) {
                    notifiedNotificationIds.add(notif.id)
                    NotificationHelper.showNotification(
                        context = getApplication(),
                        notificationId = notif.id.hashCode(),
                        title = notif.title,
                        message = notif.body
                    )
                }
            }
        }
    }

    private fun stopCollaborationSync() {
        collabSyncJob?.cancel()
        collabSyncJob = null
        connectionsJob?.cancel()
        connectionsJob = null
        notificationsJob?.cancel()
        notificationsJob = null
        _connections.value = emptyList()
        _notifications.value = emptyList()
    }

    private suspend fun syncExistingIdeasToCloud(profile: UserProfile) {
        val ideas = ideaDao.getAllIdeas()
        // Ensure any local unsynced ideas have owner set
        // Handled as user edits or creates
    }

    // ==========================================
    // CONNECTIONS & CODE SHARING
    // ==========================================

    fun sendConnectionRequest(targetCode: String, onComplete: (Result<String>) -> Unit) {
        val profile = userProfile.value ?: run {
            onComplete(Result.failure(Exception("Please sign in first.")))
            return
        }

        viewModelScope.launch {
            val res = collaborationRepository.sendConnectionRequest(profile, targetCode)
            onComplete(res)
        }
    }

    fun acceptConnection(connection: CollaboratorConnection, onComplete: ((Result<Unit>) -> Unit)? = null) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            val res = collaborationRepository.acceptConnection(connection, uid)
            onComplete?.invoke(res)
        }
    }

    fun declineConnection(connectionId: String, onComplete: ((Result<Unit>) -> Unit)? = null) {
        viewModelScope.launch {
            val res = collaborationRepository.declineConnection(connectionId)
            onComplete?.invoke(res)
        }
    }

    fun getAcceptedCollaborators(): List<UserProfile> {
        val uid = currentUser.value?.uid ?: return emptyList()
        return _connections.value.filter { it.status == "accepted" }.map { conn ->
            if (conn.senderId == uid) {
                UserProfile(
                    uid = conn.receiverId,
                    displayName = conn.receiverName,
                    email = conn.receiverEmail,
                    photoUrl = conn.receiverPhoto,
                    shareCode = conn.receiverCode
                )
            } else {
                UserProfile(
                    uid = conn.senderId,
                    displayName = conn.senderName,
                    email = conn.senderEmail,
                    photoUrl = conn.senderPhoto,
                    shareCode = ""
                )
            }
        }.distinctBy { it.uid }
    }

    // ==========================================
    // COLLABORATIVE IDEA ACTIONS & ROLES
    // ==========================================

    fun changeIdeaExecutionStatus(
        idea: IdeaEntity,
        newStatus: String,
        onComplete: ((Result<Unit>) -> Unit)? = null
    ) {
        val profile = userProfile.value
        val uid = profile?.uid ?: ""

        if (!idea.canChangeExecutionStatus(uid)) {
            onComplete?.invoke(Result.failure(Exception("Only the Owner and the Executor can change execution status.")))
            return
        }

        val updatedLocal = idea.copy(
            executionStatus = newStatus,
            status = if (newStatus == "Live") "Live in Market" else if (newStatus == "Done") "Ready to Push" else "In Progress"
        )
        updateExistingIdea(updatedLocal)

        if (profile != null && idea.firestoreId.isNotBlank()) {
            viewModelScope.launch {
                val res = collaborationRepository.changeExecutionStatus(idea, newStatus, profile)
                onComplete?.invoke(res)
            }
        } else {
            onComplete?.invoke(Result.success(Unit))
        }
    }

    fun inviteCollaboratorToIdea(
        idea: IdeaEntity,
        collaborator: UserProfile,
        asExecutor: Boolean,
        onComplete: ((Result<Unit>) -> Unit)? = null
    ) {
        val owner = userProfile.value ?: return
        viewModelScope.launch {
            val res = collaborationRepository.inviteCollaboratorToIdea(idea, collaborator, asExecutor, owner)
            onComplete?.invoke(res)
        }
    }

    fun setExecutor(
        idea: IdeaEntity,
        newExecutorUid: String,
        onComplete: ((Result<Unit>) -> Unit)? = null
    ) {
        val currentMembers = idea.getMembersList().toMutableList()
        var executorName = ""
        for (i in 0 until currentMembers.size) {
            if (currentMembers[i].uid == newExecutorUid) {
                currentMembers[i] = currentMembers[i].copy(role = IdeaRole.EXECUTOR)
                executorName = currentMembers[i].displayName
            }
        }
        val updatedLocal = idea.copy(
            executorId = newExecutorUid,
            executorName = executorName,
            membersJson = IdeaMember.listToJson(currentMembers),
            updatedAt = System.currentTimeMillis()
        )
        updateExistingIdea(updatedLocal)
        if (_selectedIdeaForDetail.value?.id == idea.id || _selectedIdeaForDetail.value?.firestoreId == idea.firestoreId) {
            _selectedIdeaForDetail.value = updatedLocal
        }

        val owner = userProfile.value
        if (owner != null && idea.firestoreId.isNotBlank()) {
            viewModelScope.launch {
                val res = collaborationRepository.setExecutor(idea, newExecutorUid, owner)
                onComplete?.invoke(res)
            }
        } else {
            onComplete?.invoke(Result.success(Unit))
        }
    }

    fun demoteExecutor(
        idea: IdeaEntity,
        memberUid: String,
        onComplete: ((Result<Unit>) -> Unit)? = null
    ) {
        val currentMembers = idea.getMembersList().toMutableList()
        for (i in 0 until currentMembers.size) {
            if (currentMembers[i].uid == memberUid) {
                currentMembers[i] = currentMembers[i].copy(role = IdeaRole.CONTRIBUTOR)
            }
        }
        val remainingExecutors = currentMembers.filter { it.role == IdeaRole.EXECUTOR }
        val newPrimary = remainingExecutors.firstOrNull()
        val updatedLocal = idea.copy(
            executorId = newPrimary?.uid ?: "",
            executorName = newPrimary?.displayName ?: "",
            membersJson = IdeaMember.listToJson(currentMembers),
            updatedAt = System.currentTimeMillis()
        )
        updateExistingIdea(updatedLocal)
        if (_selectedIdeaForDetail.value?.id == idea.id || _selectedIdeaForDetail.value?.firestoreId == idea.firestoreId) {
            _selectedIdeaForDetail.value = updatedLocal
        }

        val owner = userProfile.value
        if (owner != null && idea.firestoreId.isNotBlank()) {
            viewModelScope.launch {
                val res = collaborationRepository.demoteExecutor(idea, memberUid, owner)
                onComplete?.invoke(res)
            }
        } else {
            onComplete?.invoke(Result.success(Unit))
        }
    }

    fun removeMember(
        idea: IdeaEntity,
        memberUid: String,
        onComplete: ((Result<Unit>) -> Unit)? = null
    ) {
        val owner = userProfile.value ?: return
        viewModelScope.launch {
            val res = collaborationRepository.removeMember(idea, memberUid, owner)
            onComplete?.invoke(res)
        }
    }

    // ==========================================
    // ACTIVITY FEED & COMMENTS
    // ==========================================

    fun observeActivities(ideaId: String): Flow<List<IdeaActivity>> {
        if (ideaId.isBlank()) return emptyFlow()
        return collaborationRepository.observeActivities(ideaId)
    }

    fun postComment(ideaId: String, comment: String, onComplete: ((Result<Unit>) -> Unit)? = null) {
        val profile = userProfile.value ?: return
        viewModelScope.launch {
            val res = collaborationRepository.postComment(ideaId, comment, profile)
            onComplete?.invoke(res)
        }
    }

    fun markNotificationRead(notificationId: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            collaborationRepository.markNotificationRead(uid, notificationId)
        }
    }

    // ==========================================
    // SEARCH & FILTER & DAILY FEELING
    // ==========================================

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
    }

    fun toggleNotificationDialog(show: Boolean) {
        _showNotificationDialog.value = show
    }

    fun triggerDailyFeelingSelection(option: DailyFeelingOption) {
        _activeEffectOption.value = option
    }

    fun completeDailyFeelingSelection(option: DailyFeelingOption) {
        _selectedFeeling.value = option
        _activeEffectOption.value = null
        val today = getTodayDateString()
        prefs.edit().putString("last_checkin_completed_date", today).apply()
        _shouldShowDailyFeelingButtons.value = false

        viewModelScope.launch {
            repository.logDailyCheckIn(
                readiness = option.readiness,
                feeling = option.title
            )
        }
    }

    fun dismissDailyFeelingEffect() {
        _activeEffectOption.value?.let { opt ->
            completeDailyFeelingSelection(opt)
        } ?: run {
            _activeEffectOption.value = null
        }
    }

    fun resetTodayCheckIn() {
        prefs.edit().remove("last_checkin_completed_date").apply()
        _shouldShowDailyFeelingButtons.value = true
        _selectedFeeling.value = null
        _activeEffectOption.value = null
    }

    fun selectDailyFeeling(option: DailyFeelingOption) {
        triggerDailyFeelingSelection(option)
    }

    private val _selectedIdeaForDetail = MutableStateFlow<IdeaEntity?>(null)
    val selectedIdeaForDetail = _selectedIdeaForDetail.asStateFlow()

    private val _startInEditMode = MutableStateFlow(false)
    val startInEditMode = _startInEditMode.asStateFlow()

    fun selectIdeaForDetail(idea: IdeaEntity?, editMode: Boolean = false) {
        _selectedIdeaForDetail.value = idea
        _startInEditMode.value = editMode
    }

    fun addNewIdea(
        title: String,
        description: String,
        category: String,
        neededRequirements: Set<RequirementType>,
        moneyScore: Int,
        skillsScore: Int,
        timeScore: Int,
        toolsScore: Int,
        peopleScore: Int,
        knowledgeScore: Int,
        nextAction: String,
        steps: List<ExecutionStep> = emptyList(),
        resources: List<IdeaResource> = emptyList(),
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val profile = userProfile.value
            val ownerId = profile?.uid ?: ""
            val ownerName = profile?.displayName ?: _userName.value
            val ownerPhoto = profile?.photoUrl ?: ""

            val membersList = if (ownerId.isNotBlank()) {
                listOf(
                    IdeaMember(
                        uid = ownerId,
                        displayName = ownerName,
                        email = profile?.email ?: "",
                        photoUrl = ownerPhoto,
                        role = IdeaRole.OWNER
                    )
                )
            } else emptyList()

            val memberIds = if (ownerId.isNotBlank()) ownerId else ""

            val readiness = IdeaEntity.calculateWeightedReadiness(
                needed = neededRequirements,
                money = moneyScore,
                skills = skillsScore,
                time = timeScore,
                tools = toolsScore,
                people = peopleScore,
                knowledge = knowledgeScore
            )

            val initialEntity = IdeaEntity(
                title = title.trim(),
                description = description.trim(),
                category = category.trim().ifBlank { "General Tech" },
                neededRequirements = RequirementType.toCommaSeparated(neededRequirements),
                moneyScore = moneyScore,
                skillsScore = skillsScore,
                timeScore = timeScore,
                toolsScore = toolsScore,
                peopleScore = peopleScore,
                knowledgeScore = knowledgeScore,
                readinessScore = readiness,
                status = if (readiness >= 75) "Ready to Push" else if (readiness >= 50) "In Progress" else "Exploring",
                executionStatus = "Not Started",
                isFeatured = readiness >= 80,
                nextAction = nextAction.trim().ifBlank { "Map first 3 tasks" },
                stepsJson = ExecutionStep.listToJson(steps),
                resourcesJson = IdeaResource.listToJson(resources),
                ownerId = ownerId,
                ownerName = ownerName,
                ownerPhoto = ownerPhoto,
                memberIds = memberIds,
                membersJson = IdeaMember.listToJson(membersList)
            )

            val localId = ideaDao.insertIdea(initialEntity)

            // If signed in, sync to Firestore
            if (profile != null) {
                val cloudRes = collaborationRepository.saveIdea(initialEntity, profile)
                cloudRes.onSuccess { docId ->
                    val withCloudId = initialEntity.copy(id = localId, firestoreId = docId)
                    ideaDao.updateIdea(withCloudId)
                }
            }

            onSuccess(localId)
        }
    }

    fun updateExistingIdea(idea: IdeaEntity, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val readiness = IdeaEntity.calculateWeightedReadiness(
                needed = idea.getRequirementsSet(),
                money = idea.moneyScore,
                skills = idea.skillsScore,
                time = idea.timeScore,
                tools = idea.toolsScore,
                people = idea.peopleScore,
                knowledge = idea.knowledgeScore
            )
            val updated = idea.copy(
                readinessScore = readiness,
                status = if (readiness >= 75) "Ready to Push" else if (readiness >= 50) "In Progress" else "Exploring",
                isFeatured = readiness >= 80
            )
            repository.updateIdea(updated)
            if (_selectedIdeaForDetail.value?.id == idea.id) {
                _selectedIdeaForDetail.value = updated
            }

            // Sync to Firestore if linked
            val profile = userProfile.value
            if (profile != null && idea.firestoreId.isNotBlank()) {
                collaborationRepository.saveIdea(updated, profile)
            }

            onSuccess?.invoke()
        }
    }

    fun toggleStepCompletion(idea: IdeaEntity, stepId: String) {
        val steps = idea.getExecutionSteps().toMutableList()
        val index = steps.indexOfFirst { it.id == stepId }
        if (index != -1) {
            val step = steps[index]
            steps[index] = step.copy(isCompleted = !step.isCompleted)
            val updated = idea.copy(stepsJson = ExecutionStep.listToJson(steps))
            updateExistingIdea(updated)
        }
    }

    fun addStepToIdea(idea: IdeaEntity, stepTitle: String) {
        if (stepTitle.isBlank()) return
        val steps = idea.getExecutionSteps().toMutableList()
        steps.add(ExecutionStep(title = stepTitle.trim(), isCompleted = false))
        val updated = idea.copy(stepsJson = ExecutionStep.listToJson(steps))
        updateExistingIdea(updated)
    }

    fun deleteStepFromIdea(idea: IdeaEntity, stepId: String) {
        val steps = idea.getExecutionSteps().filter { it.id != stepId }
        val updated = idea.copy(stepsJson = ExecutionStep.listToJson(steps))
        updateExistingIdea(updated)
    }

    fun addResourceToIdea(idea: IdeaEntity, title: String, urlOrNote: String, type: String) {
        if (title.isBlank()) return
        val resources = idea.getResourcesList().toMutableList()
        resources.add(IdeaResource(title = title.trim(), urlOrNote = urlOrNote.trim(), type = type))
        val updated = idea.copy(resourcesJson = IdeaResource.listToJson(resources))
        updateExistingIdea(updated)
    }

    fun deleteResourceFromIdea(idea: IdeaEntity, resourceId: String) {
        val resources = idea.getResourcesList().filter { it.id != resourceId }
        val updated = idea.copy(resourcesJson = IdeaResource.listToJson(resources))
        updateExistingIdea(updated)
    }

    fun deleteIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            val uid = currentUser.value?.uid ?: ""
            if (idea.firestoreId.isNotBlank()) {
                collaborationRepository.deleteIdea(idea, uid)
            }
            repository.deleteIdea(idea.id)
            if (_selectedIdeaForDetail.value?.id == idea.id) {
                _selectedIdeaForDetail.value = null
            }
        }
    }

    fun deleteIdea(ideaId: Long) {
        val target = allIdeas.value.firstOrNull { it.id == ideaId }
        if (target != null) {
            deleteIdea(target)
        } else {
            viewModelScope.launch {
                repository.deleteIdea(ideaId)
                if (_selectedIdeaForDetail.value?.id == ideaId) {
                    _selectedIdeaForDetail.value = null
                }
            }
        }
    }

    val allAudits: StateFlow<List<AuditEntity>> = repository.allAudits.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Active live session timer
    private val _activeSessionIdeaId = MutableStateFlow<Long?>(null)
    val activeSessionIdeaId = _activeSessionIdeaId.asStateFlow()

    private val _activeSessionStartTime = MutableStateFlow<Long?>(null)
    val activeSessionStartTime = _activeSessionStartTime.asStateFlow()

    private val _activeSessionElapsedSeconds = MutableStateFlow(0L)
    val activeSessionElapsedSeconds = _activeSessionElapsedSeconds.asStateFlow()

    private var sessionTimerJob: Job? = null

    fun startWorkSession(ideaId: Long) {
        _activeSessionIdeaId.value = ideaId
        val now = System.currentTimeMillis()
        _activeSessionStartTime.value = now
        _activeSessionElapsedSeconds.value = 0L

        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                _activeSessionElapsedSeconds.value = (System.currentTimeMillis() - now) / 1000L
            }
        }
    }

    fun stopWorkSession(notes: String = "", onSaved: ((WorkSessionLog) -> Unit)? = null) {
        val ideaId = _activeSessionIdeaId.value ?: return
        val startTime = _activeSessionStartTime.value ?: System.currentTimeMillis()
        val endTime = System.currentTimeMillis()
        val durationMillis = (endTime - startTime).coerceAtLeast(1000L)

        sessionTimerJob?.cancel()
        sessionTimerJob = null
        _activeSessionIdeaId.value = null
        _activeSessionStartTime.value = null
        _activeSessionElapsedSeconds.value = 0L

        viewModelScope.launch {
            val idea = allIdeas.value.firstOrNull { it.id == ideaId }
            if (idea != null) {
                val session = WorkSessionLog(
                    startTimeMillis = startTime,
                    endTimeMillis = endTime,
                    durationMillis = durationMillis,
                    notes = notes.trim()
                )
                val updated = repository.addWorkSession(idea, session)
                if (_selectedIdeaForDetail.value?.id == idea.id) {
                    _selectedIdeaForDetail.value = updated
                }
                onSaved?.invoke(session)
            }
        }
    }

    fun cancelWorkSession() {
        sessionTimerJob?.cancel()
        sessionTimerJob = null
        _activeSessionIdeaId.value = null
        _activeSessionStartTime.value = null
        _activeSessionElapsedSeconds.value = 0L
    }

    // Timing Lifecycle Clock Controls
    fun startIdeaTimingClock(idea: IdeaEntity) {
        viewModelScope.launch {
            val updated = repository.startIdeaClock(idea)
            if (_selectedIdeaForDetail.value?.id == idea.id) {
                _selectedIdeaForDetail.value = updated
            }
            val profile = userProfile.value
            if (profile != null && updated.firestoreId.isNotBlank()) {
                collaborationRepository.saveIdea(updated, profile)
            }
        }
    }

    fun pauseIdeaTimingClock(idea: IdeaEntity) {
        viewModelScope.launch {
            val updated = repository.pauseIdeaClock(idea)
            if (_selectedIdeaForDetail.value?.id == idea.id) {
                _selectedIdeaForDetail.value = updated
            }
            val profile = userProfile.value
            if (profile != null && updated.firestoreId.isNotBlank()) {
                collaborationRepository.saveIdea(updated, profile)
            }
        }
    }

    fun completeIdeaTimingClock(idea: IdeaEntity) {
        viewModelScope.launch {
            val updated = repository.completeIdeaClock(idea)
            if (_selectedIdeaForDetail.value?.id == idea.id) {
                _selectedIdeaForDetail.value = updated
            }
            val profile = userProfile.value
            if (profile != null && updated.firestoreId.isNotBlank()) {
                collaborationRepository.saveIdea(updated, profile)
            }
        }
    }

    fun setIdeaPlanningStatus(idea: IdeaEntity) {
        viewModelScope.launch {
            val updated = repository.setIdeaPlanning(idea)
            if (_selectedIdeaForDetail.value?.id == idea.id) {
                _selectedIdeaForDetail.value = updated
            }
            val profile = userProfile.value
            if (profile != null && updated.firestoreId.isNotBlank()) {
                collaborationRepository.saveIdea(updated, profile)
            }
        }
    }

    fun updateTimeBox(idea: IdeaEntity, timeBoxDays: Int?, targetDateMillis: Long?) {
        viewModelScope.launch {
            val updated = repository.updateTimeBox(idea, timeBoxDays, targetDateMillis)
            if (_selectedIdeaForDetail.value?.id == idea.id) {
                _selectedIdeaForDetail.value = updated
            }
            val profile = userProfile.value
            if (profile != null && updated.firestoreId.isNotBlank()) {
                collaborationRepository.saveIdea(updated, profile)
            }
        }
    }

    fun extendIdeaTime(idea: IdeaEntity, additionalDays: Int = 30) {
        viewModelScope.launch {
            val updated = repository.extendIdeaTime(idea, additionalDays)
            if (_selectedIdeaForDetail.value?.id == idea.id) {
                _selectedIdeaForDetail.value = updated
            }
            val profile = userProfile.value
            if (profile != null && updated.firestoreId.isNotBlank()) {
                collaborationRepository.saveIdea(updated, profile)
            }
        }
    }

    fun updateReminderSettings(idea: IdeaEntity, config: ReminderConfig) {
        viewModelScope.launch {
            val updated = repository.updateReminderConfig(idea, config)
            if (_selectedIdeaForDetail.value?.id == idea.id) {
                _selectedIdeaForDetail.value = updated
            }
            val app = getApplication<Application>()
            ReminderScheduler.scheduleReminder(app, updated)
        }
    }

    fun reduceReminderFrequency(idea: IdeaEntity) {
        val currentConfig = idea.getReminderConfig()
        val newConfig = currentConfig.copy(
            frequency = ReminderFrequency.WEEKLY,
            consecutiveDismissals = 0
        )
        updateReminderSettings(idea, newConfig)
    }

    fun resetDismissalCount(idea: IdeaEntity) {
        val currentConfig = idea.getReminderConfig()
        val newConfig = currentConfig.copy(consecutiveDismissals = 0)
        updateReminderSettings(idea, newConfig)
    }

    fun deleteAudit(auditId: Long) {
        viewModelScope.launch {
            repository.deleteAudit(auditId)
        }
    }

    fun generateAudit(
        targetIdeaId: Long?,
        periodType: String,
        onComplete: (AuditEntity) -> Unit
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val daysBack = if (periodType.equals("Monthly", ignoreCase = true)) 30L else 7L
            val cutoffMillis = now - (daysBack * 24L * 60L * 60L * 1000L)

            val ideaList = allIdeas.value
            val targetIdeas = if (targetIdeaId != null && targetIdeaId > 0) {
                ideaList.filter { it.id == targetIdeaId }
            } else {
                ideaList
            }

            val checkIns = allCheckIns.value
            val olderCheckIns = checkIns.filter { it.timestamp < cutoffMillis }

            val readinessStart: Int
            val readinessEnd: Int

            if (targetIdeas.isEmpty()) {
                readinessStart = 50
                readinessEnd = 50
            } else if (targetIdeas.size == 1) {
                val idea = targetIdeas.first()
                readinessEnd = idea.readinessScore
                val historicalScore = olderCheckIns.maxByOrNull { it.timestamp }?.readinessLevel
                readinessStart = historicalScore ?: (idea.readinessScore - (idea.getExecutionSteps().count { it.isCompleted } * 4)).coerceIn(10, 100)
            } else {
                readinessEnd = (targetIdeas.sumOf { it.readinessScore } / targetIdeas.size.toDouble()).roundToInt().coerceIn(0, 100)
                val avgOld = if (olderCheckIns.isNotEmpty()) olderCheckIns.takeLast(3).map { it.readinessLevel }.average().roundToInt() else (readinessEnd - 6)
                readinessStart = avgOld.coerceIn(10, 100)
            }

            val readinessDelta = readinessEnd - readinessStart

            var totalTimeElapsed = 0L
            var totalPlannedDuration = 0L

            targetIdeas.forEach { idea ->
                totalTimeElapsed += idea.getTotalActiveElapsedMillis(now)
                totalPlannedDuration += (idea.getPlannedDurationMillis() ?: (30L * 24L * 60L * 60L * 1000L))
            }

            val elapsedVsPlannedPercent = if (totalPlannedDuration > 0) {
                ((totalTimeElapsed.toDouble() / totalPlannedDuration.toDouble()) * 100).roundToInt().coerceIn(0, 100)
            } else 0

            val allSteps = targetIdeas.flatMap { it.getExecutionSteps() }
            val completedSteps = allSteps.filter { it.isCompleted }.map { it.title }
            val totalStepsCount = allSteps.size
            val completedStepsCount = completedSteps.size

            val blockers = mutableListOf<String>()
            targetIdeas.forEach { idea ->
                RequirementType.entries.forEach { type ->
                    val score = idea.getScoreFor(type)
                    if (score < 60) {
                        blockers.add("${idea.title}: ${type.title} readiness low at $score%")
                    }
                }
                if (idea.isTimeExpired(now)) {
                    blockers.add("${idea.title}: Time box expired — action needed (Extend, Pause, or Shelve)")
                }
            }
            if (blockers.isEmpty()) {
                blockers.add("No critical bottlenecks detected across evaluated ideas.")
            }

            val recommendations = mutableListOf<String>()
            targetIdeas.forEach { idea ->
                val gapPair = idea.getLargestGapRequirement()
                if (gapPair != null) {
                    val (type, score) = gapPair
                    val uncompleted = idea.getExecutionSteps().filter { !it.isCompleted }
                    val nextItem = if (idea.nextAction.isNotBlank()) idea.nextAction else (uncompleted.firstOrNull()?.title ?: "Address ${type.title} gap")
                    recommendations.add("${idea.title} • Next step: $nextItem (${type.title} gap: ${100 - score} pts)")
                }
            }
            if (recommendations.isEmpty()) {
                recommendations.add("Define immediate next actions for active ideas in planning.")
            }

            val titleLabel = if (targetIdeas.size == 1) targetIdeas.first().title else "Portfolio (${targetIdeas.size} ideas)"
            val summaryNote = if (readinessDelta >= 0) {
                "Steady positive momentum with +$readinessDelta% readiness progression over the $periodType period. $completedStepsCount steps marked done."
            } else {
                "Readiness slowed by $readinessDelta% over the $periodType period. Focus on resolving the ${blockers.firstOrNull() ?: "priority gaps"}."
            }

            val audit = AuditEntity(
                ideaId = if (targetIdeas.size == 1) targetIdeas.first().id else 0L,
                ideaTitle = titleLabel,
                periodType = periodType,
                generatedAtMillis = now,
                readinessStart = readinessStart,
                readinessEnd = readinessEnd,
                readinessDelta = readinessDelta,
                timeElapsedMillis = totalTimeElapsed,
                plannedDurationMillis = totalPlannedDuration,
                elapsedVsPlannedPercent = elapsedVsPlannedPercent,
                completedStepsCount = completedStepsCount,
                totalStepsCount = totalStepsCount,
                completedStepsJson = AuditEntity.stringListToJson(completedSteps),
                blockersJson = AuditEntity.stringListToJson(blockers),
                recommendedNextStepsJson = AuditEntity.stringListToJson(recommendations),
                summaryNote = summaryNote
            )

            val newId = repository.saveAudit(audit)
            val savedAudit = audit.copy(id = newId)
            onComplete(savedAudit)
        }
    }
}
