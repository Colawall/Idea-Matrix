package com.example.data.repository

import com.example.data.local.AuditDao
import com.example.data.local.AuditEntity
import com.example.data.local.DailyCheckInEntity
import com.example.data.local.DailyCheckInDao
import com.example.data.local.IdeaDao
import com.example.data.local.IdeaEntity
import com.example.data.model.ExecutionStep
import com.example.data.model.IdeaResource
import com.example.data.model.RequirementType
import com.example.data.model.TimingStatus
import com.example.data.model.WorkSessionLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class IdeaRepository(
    private val ideaDao: IdeaDao,
    private val checkInDao: DailyCheckInDao,
    private val auditDao: AuditDao
) {
    val allIdeas: Flow<List<IdeaEntity>> = ideaDao.getAllIdeas()
    val topFeaturedIdeas: Flow<List<IdeaEntity>> = ideaDao.getTopFeaturedIdeas()
    val allCheckIns: Flow<List<DailyCheckInEntity>> = checkInDao.getAllCheckIns()
    val latestCheckIn: Flow<DailyCheckInEntity?> = checkInDao.getLatestCheckIn()
    val allAudits: Flow<List<AuditEntity>> = auditDao.getAllAudits()

    suspend fun ensureInitialSeedData() = withContext(Dispatchers.IO) {
        if (ideaDao.getCount() == 0) {
            val seedIdeas = listOf(
                IdeaEntity(
                    title = "Micro-SaaS Invoicing API",
                    description = "Automated billing reconciliation and tax webhook pipeline for stripe-first startups.",
                    category = "B2B SaaS",
                    neededRequirements = "Money,Skills,Tools",
                    moneyScore = 90,
                    skillsScore = 95,
                    timeScore = 80,
                    toolsScore = 90,
                    peopleScore = 85,
                    knowledgeScore = 90,
                    readinessScore = 92,
                    status = "Ready to Push",
                    isFeatured = true,
                    nextAction = "Deploy sandbox endpoint & talk to 5 beta developers",
                    timeBoxDays = 60,
                    timingStatus = "Active",
                    startedAtMillis = System.currentTimeMillis() - (14L * 24 * 3600 * 1000),
                    lastClockStartedAtMillis = System.currentTimeMillis() - (3600 * 1000),
                    accumulatedActiveDurationMillis = 14L * 24 * 3600 * 1000,
                    stepsJson = ExecutionStep.listToJson(
                        listOf(
                            ExecutionStep(title = "Define OpenAPI 3.1 specification for webhooks", isCompleted = true),
                            ExecutionStep(title = "Setup Stripe webhook test runner on Cloud Run", isCompleted = true),
                            ExecutionStep(title = "Interview 5 early indie founders", isCompleted = false),
                            ExecutionStep(title = "Draft public sandbox landing page", isCompleted = false)
                        )
                    ),
                    resourcesJson = IdeaResource.listToJson(
                        listOf(
                            IdeaResource(title = "Stripe Webhooks Guide", urlOrNote = "https://stripe.com/docs/webhooks", type = "Link"),
                            IdeaResource(title = "Postman Sandbox Collection", urlOrNote = "https://postman.com/collection", type = "Tool"),
                            IdeaResource(title = "Tax Compliance Checklist", urlOrNote = "Checklist for EU/US VAT", type = "Doc")
                        )
                    )
                ),
                IdeaEntity(
                    title = "AI Recipe Prep Agent",
                    description = "Vision-based fridge scanner turning leftover groceries into chef-quality meal prep recipes.",
                    category = "Mobile App",
                    neededRequirements = "Skills,Time,Knowledge",
                    moneyScore = 70,
                    skillsScore = 85,
                    timeScore = 75,
                    toolsScore = 80,
                    peopleScore = 65,
                    knowledgeScore = 80,
                    readinessScore = 80,
                    status = "Ready to Push",
                    isFeatured = true,
                    nextAction = "Fine-tune prompt and record 30-sec demo",
                    timeBoxDays = 30,
                    timingStatus = "Planning",
                    stepsJson = ExecutionStep.listToJson(
                        listOf(
                            ExecutionStep(title = "CameraX integration with edge food-detection", isCompleted = true),
                            ExecutionStep(title = "Fine-tune Gemini prompt for ingredient pairing", isCompleted = false),
                            ExecutionStep(title = "Record interactive demo on emulator", isCompleted = false)
                        )
                    ),
                    resourcesJson = IdeaResource.listToJson(
                        listOf(
                            IdeaResource(title = "Food-101 Dataset", urlOrNote = "https://data.vision.ee.ethz.ch/cvl/datasets_extra/food-101/", type = "Doc"),
                            IdeaResource(title = "MealPrep Subreddit feedback notes", urlOrNote = "r/MealPrepSunday user trends", type = "Note")
                        )
                    )
                ),
                IdeaEntity(
                    title = "DevRel Content Repurposer",
                    description = "CLI that converts git commits and PR descriptions into technical changelogs and social posts.",
                    category = "Creator Tool",
                    neededRequirements = "Skills,Tools,Time",
                    moneyScore = 90,
                    skillsScore = 85,
                    timeScore = 60,
                    toolsScore = 75,
                    peopleScore = 60,
                    knowledgeScore = 85,
                    readinessScore = 74,
                    status = "In Progress",
                    isFeatured = false,
                    nextAction = "Publish open-source CLI v0.1",
                    timeBoxDays = 90,
                    timingStatus = "Paused",
                    startedAtMillis = System.currentTimeMillis() - (20L * 24 * 3600 * 1000),
                    accumulatedActiveDurationMillis = 15L * 24 * 3600 * 1000,
                    stepsJson = ExecutionStep.listToJson(
                        listOf(
                            ExecutionStep(title = "Git log parser for semantic commit messages", isCompleted = true),
                            ExecutionStep(title = "Markdown template builder for changelogs", isCompleted = false)
                        )
                    ),
                    resourcesJson = IdeaResource.listToJson(
                        listOf(
                            IdeaResource(title = "Conventional Commits Spec", urlOrNote = "https://www.conventionalcommits.org/", type = "Doc")
                        )
                    )
                ),
                IdeaEntity(
                    title = "Local Coffee Roasters Club",
                    description = "Curated subscription box featuring single-origin micro-lots from local independent roasters.",
                    category = "E-Commerce",
                    neededRequirements = "Money,People,Knowledge",
                    moneyScore = 55,
                    skillsScore = 75,
                    timeScore = 65,
                    toolsScore = 70,
                    peopleScore = 60,
                    knowledgeScore = 65,
                    readinessScore = 62,
                    status = "Validating",
                    isFeatured = false,
                    nextAction = "Partner with 3 local roasteries",
                    stepsJson = ExecutionStep.listToJson(
                        listOf(
                            ExecutionStep(title = "Visit 5 neighborhood roasters in person", isCompleted = false),
                            ExecutionStep(title = "Calculate shipping & eco packaging margin", isCompleted = false)
                        )
                    ),
                    resourcesJson = IdeaResource.listToJson(
                        listOf(
                            IdeaResource(title = "Roaster Wholesale Price Sheet", urlOrNote = "Sample 12oz pricing tier", type = "Doc")
                        )
                    )
                ),
                IdeaEntity(
                    title = "Freelance Contract Escrow",
                    description = "Zero-fee milestone payment vaults for remote developers and independent agencies.",
                    category = "Fintech",
                    neededRequirements = "Money,Skills,Knowledge,People",
                    moneyScore = 45,
                    skillsScore = 70,
                    timeScore = 60,
                    toolsScore = 65,
                    peopleScore = 50,
                    knowledgeScore = 60,
                    readinessScore = 56,
                    status = "Exploring",
                    isFeatured = false,
                    nextAction = "Review regulatory & legal framework",
                    stepsJson = ExecutionStep.listToJson(
                        listOf(
                            ExecutionStep(title = "Consult fintech compliance lawyer", isCompleted = false)
                        )
                    ),
                    resourcesJson = IdeaResource.listToJson(
                        listOf(
                            IdeaResource(title = "FinCEN Escrow Guidelines", urlOrNote = "https://www.fincen.gov/", type = "Link")
                        )
                    )
                )
            )
            ideaDao.insertAll(seedIdeas)
        }

        if (checkInDao.getCount() == 0) {
            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60L * 60L * 1000L
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val scores = listOf(65, 70, 74, 80, 84, 88, 92)
            val tags = listOf(
                "Exploring",
                "Getting There",
                "Getting There",
                "Super Ready",
                "Super Ready",
                "Fired Up",
                "Fired Up"
            )

            val checkIns = days.mapIndexed { index, day ->
                val offset = (6 - index) * dayMillis
                DailyCheckInEntity(
                    dayName = day,
                    dateLabel = "Day ${index + 1}",
                    readinessLevel = scores[index],
                    feelingTag = tags[index],
                    timestamp = now - offset
                )
            }
            checkInDao.insertAll(checkIns)
        }
    }

    suspend fun saveIdea(
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
        resources: List<IdeaResource> = emptyList()
    ): Long = withContext(Dispatchers.IO) {
        val readiness = IdeaEntity.calculateWeightedReadiness(
            needed = neededRequirements,
            money = moneyScore,
            skills = skillsScore,
            time = timeScore,
            tools = toolsScore,
            people = peopleScore,
            knowledge = knowledgeScore
        )

        val entity = IdeaEntity(
            title = title,
            description = description,
            category = category,
            neededRequirements = RequirementType.toCommaSeparated(neededRequirements),
            moneyScore = moneyScore,
            skillsScore = skillsScore,
            timeScore = timeScore,
            toolsScore = toolsScore,
            peopleScore = peopleScore,
            knowledgeScore = knowledgeScore,
            readinessScore = readiness,
            status = if (readiness >= 75) "Ready to Push" else if (readiness >= 50) "In Progress" else "Exploring",
            isFeatured = readiness >= 80,
            nextAction = nextAction.ifBlank { "Map first 3 tasks" },
            stepsJson = ExecutionStep.listToJson(steps),
            resourcesJson = IdeaResource.listToJson(resources)
        )
        ideaDao.insertIdea(entity)
    }

    suspend fun updateIdea(idea: IdeaEntity) = withContext(Dispatchers.IO) {
        ideaDao.updateIdea(idea)
    }

    suspend fun deleteIdea(id: Long) = withContext(Dispatchers.IO) {
        ideaDao.deleteById(id)
    }

    suspend fun logDailyCheckIn(readiness: Int, feeling: String) = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: "Today"
        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dateLabel = "$monthName $dayOfMonth".trim()

        val checkIn = DailyCheckInEntity(
            dayName = dayName,
            dateLabel = dateLabel,
            readinessLevel = readiness,
            feelingTag = feeling,
            timestamp = System.currentTimeMillis()
        )
        checkInDao.insertCheckIn(checkIn)
    }

    fun getAuditsForIdea(ideaId: Long): Flow<List<AuditEntity>> = auditDao.getAuditsForIdea(ideaId)

    suspend fun saveAudit(audit: AuditEntity): Long = withContext(Dispatchers.IO) {
        auditDao.insertAudit(audit)
    }

    suspend fun deleteAudit(id: Long) = withContext(Dispatchers.IO) {
        auditDao.deleteById(id)
    }

    suspend fun startIdeaClock(idea: IdeaEntity): IdeaEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val updated = idea.copy(
            timingStatus = TimingStatus.ACTIVE.label,
            executionStatus = if (idea.executionStatus == "Not Started" || idea.executionStatus.isBlank()) "Started" else idea.executionStatus,
            startedAtMillis = idea.startedAtMillis ?: now,
            lastClockStartedAtMillis = now,
            updatedAt = now
        )
        ideaDao.updateIdea(updated)
        updated
    }

    suspend fun pauseIdeaClock(idea: IdeaEntity): IdeaEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val additional = if (idea.lastClockStartedAtMillis != null && idea.lastClockStartedAtMillis > 0) {
            (now - idea.lastClockStartedAtMillis).coerceAtLeast(0L)
        } else 0L

        val updated = idea.copy(
            timingStatus = TimingStatus.PAUSED.label,
            accumulatedActiveDurationMillis = idea.accumulatedActiveDurationMillis + additional,
            lastClockStartedAtMillis = null,
            updatedAt = now
        )
        ideaDao.updateIdea(updated)
        updated
    }

    suspend fun completeIdeaClock(idea: IdeaEntity): IdeaEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val additional = if (idea.lastClockStartedAtMillis != null && idea.lastClockStartedAtMillis > 0) {
            (now - idea.lastClockStartedAtMillis).coerceAtLeast(0L)
        } else 0L

        val updated = idea.copy(
            timingStatus = TimingStatus.DONE.label,
            executionStatus = "Done",
            accumulatedActiveDurationMillis = idea.accumulatedActiveDurationMillis + additional,
            lastClockStartedAtMillis = null,
            updatedAt = now
        )
        ideaDao.updateIdea(updated)
        updated
    }

    suspend fun setIdeaPlanning(idea: IdeaEntity): IdeaEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val additional = if (idea.lastClockStartedAtMillis != null && idea.lastClockStartedAtMillis > 0) {
            (now - idea.lastClockStartedAtMillis).coerceAtLeast(0L)
        } else 0L

        val updated = idea.copy(
            timingStatus = TimingStatus.PLANNING.label,
            accumulatedActiveDurationMillis = idea.accumulatedActiveDurationMillis + additional,
            lastClockStartedAtMillis = null,
            updatedAt = now
        )
        ideaDao.updateIdea(updated)
        updated
    }

    suspend fun updateTimeBox(
        idea: IdeaEntity,
        timeBoxDays: Int?,
        targetDateMillis: Long?
    ): IdeaEntity = withContext(Dispatchers.IO) {
        val updated = idea.copy(
            timeBoxDays = timeBoxDays,
            targetDateMillis = targetDateMillis,
            updatedAt = System.currentTimeMillis()
        )
        ideaDao.updateIdea(updated)
        updated
    }

    suspend fun extendIdeaTime(
        idea: IdeaEntity,
        additionalDays: Int = 30
    ): IdeaEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val additionalMillis = additionalDays * 24L * 60L * 60L * 1000L
        val updated = if (idea.targetDateMillis != null && idea.targetDateMillis > 0) {
            idea.copy(
                targetDateMillis = idea.targetDateMillis + additionalMillis,
                timingStatus = if (idea.timingStatus == TimingStatus.PAUSED.label) TimingStatus.ACTIVE.label else idea.timingStatus,
                lastClockStartedAtMillis = if (idea.timingStatus == TimingStatus.PAUSED.label || idea.lastClockStartedAtMillis == null) now else idea.lastClockStartedAtMillis,
                updatedAt = now
            )
        } else {
            val currentDays = idea.timeBoxDays ?: 30
            idea.copy(
                timeBoxDays = currentDays + additionalDays,
                timingStatus = if (idea.timingStatus == TimingStatus.PAUSED.label) TimingStatus.ACTIVE.label else idea.timingStatus,
                lastClockStartedAtMillis = if (idea.timingStatus == TimingStatus.PAUSED.label || idea.lastClockStartedAtMillis == null) now else idea.lastClockStartedAtMillis,
                updatedAt = now
            )
        }
        ideaDao.updateIdea(updated)
        updated
    }

    suspend fun addWorkSession(
        idea: IdeaEntity,
        session: WorkSessionLog
    ): IdeaEntity = withContext(Dispatchers.IO) {
        val currentSessions = idea.getSessionLogs().toMutableList()
        currentSessions.add(0, session)
        val updated = idea.copy(
            sessionLogsJson = WorkSessionLog.listToJson(currentSessions),
            updatedAt = System.currentTimeMillis()
        )
        ideaDao.updateIdea(updated)
        updated
    }

    suspend fun updateReminderConfig(
        idea: IdeaEntity,
        config: com.example.data.model.ReminderConfig
    ): IdeaEntity = withContext(Dispatchers.IO) {
        val updated = idea.copy(
            reminderConfigJson = config.toJsonObject().toString(),
            updatedAt = System.currentTimeMillis()
        )
        ideaDao.updateIdea(updated)
        updated
    }
}
