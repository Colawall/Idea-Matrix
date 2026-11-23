package com.example.ui.screens.ideas

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IdeaEntity
import com.example.data.model.ExecutionStep
import com.example.data.model.IdeaResource
import com.example.data.model.RequirementType
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import com.example.ui.components.DaysRemainingChip
import com.example.ui.components.ReminderSettingsSection
import com.example.ui.components.TimelineSection
import com.example.ui.components.WorkSessionTimerSection
import com.example.ui.screens.audit.AuditScreen
import com.example.ui.components.CircularReadinessMeter
import com.example.ui.components.ReadinessPillBadge
import com.example.ui.screens.addidea.RequirementSliderCard
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.DarkSurfaceDeep
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.MintGreenLight
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.viewmodel.IdeaViewModel

@Composable
fun IdeaDetailEditScreen(
    idea: IdeaEntity,
    viewModel: IdeaViewModel,
    onDismiss: () -> Unit,
    initialEditMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    val currentUser by viewModel.currentUser.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val acceptedConnections = remember(connections) { connections.filter { it.status == "accepted" } }
    val activities by viewModel.observeActivities(idea.firestoreId).collectAsState(initial = emptyList())

    val isOwner = idea.isOwner(currentUser?.uid) || idea.ownerId.isBlank()
    val isExecutor = idea.isExecutor(currentUser?.uid)
    val canChangeStatus = isOwner || isExecutor
    var currentExecutionStatus by remember(idea.executionStatus) { mutableStateOf(idea.executionStatus) }

    var isEditMode by remember(idea, initialEditMode) { mutableStateOf(initialEditMode) }
    var showAuditScreen by remember { mutableStateOf(false) }

    if (showAuditScreen) {
        AuditScreen(
            viewModel = viewModel,
            onBack = { showAuditScreen = false },
            preselectedIdeaId = idea.id
        )
        return
    }

    // Editable state initialized from idea
    var editTitle by remember(idea) { mutableStateOf(idea.title) }
    var editDescription by remember(idea) { mutableStateOf(idea.description) }
    var editCategory by remember(idea) { mutableStateOf(idea.category) }
    var editNextAction by remember(idea) { mutableStateOf(idea.nextAction) }

    var selectedRequirements by remember(idea) {
        mutableStateOf(idea.getRequirementsSet())
    }

    var moneyScore by remember(idea) { mutableIntStateOf(idea.moneyScore) }
    var skillsScore by remember(idea) { mutableIntStateOf(idea.skillsScore) }
    var timeScore by remember(idea) { mutableIntStateOf(idea.timeScore) }
    var toolsScore by remember(idea) { mutableIntStateOf(idea.toolsScore) }
    var peopleScore by remember(idea) { mutableIntStateOf(idea.peopleScore) }
    var knowledgeScore by remember(idea) { mutableIntStateOf(idea.knowledgeScore) }

    val currentSteps = remember(idea) {
        mutableStateListOf<ExecutionStep>().apply {
            addAll(idea.getExecutionSteps())
        }
    }

    val currentResources = remember(idea) {
        mutableStateListOf<IdeaResource>().apply {
            addAll(idea.getResourcesList())
        }
    }

    // New step text state
    var newStepText by remember { mutableStateOf("") }

    // New resource input state
    var showAddResourceDialog by remember { mutableStateOf(false) }
    var newResourceTitle by remember { mutableStateOf("") }
    var newResourceUrlOrNote by remember { mutableStateOf("") }
    var newResourceType by remember { mutableStateOf("URL") }

    val calculatedReadiness by remember {
        derivedStateOf {
            IdeaEntity.calculateWeightedReadiness(
                needed = selectedRequirements,
                money = moneyScore,
                skills = skillsScore,
                time = timeScore,
                tools = toolsScore,
                people = peopleScore,
                knowledge = knowledgeScore
            )
        }
    }

    val hasUnsavedChanges by remember {
        derivedStateOf {
            editTitle != idea.title ||
            editDescription != idea.description ||
            editCategory != idea.category ||
            editNextAction != idea.nextAction ||
            selectedRequirements != idea.getRequirementsSet() ||
            moneyScore != idea.moneyScore ||
            skillsScore != idea.skillsScore ||
            timeScore != idea.timeScore ||
            toolsScore != idea.toolsScore ||
            peopleScore != idea.peopleScore ||
            knowledgeScore != idea.knowledgeScore ||
            ExecutionStep.listToJson(currentSteps) != idea.stepsJson ||
            IdeaResource.listToJson(currentResources) != idea.resourcesJson
        }
    }

    BackHandler {
        if (isEditMode) {
            isEditMode = false
        } else {
            onDismiss()
        }
    }

    fun saveAllEdits() {
        val updated = idea.copy(
            title = editTitle.trim().ifBlank { idea.title },
            description = editDescription.trim().ifBlank { idea.description },
            category = editCategory.trim().ifBlank { idea.category },
            nextAction = editNextAction.trim().ifBlank { idea.nextAction },
            neededRequirements = RequirementType.toCommaSeparated(selectedRequirements),
            moneyScore = moneyScore,
            skillsScore = skillsScore,
            timeScore = timeScore,
            toolsScore = toolsScore,
            peopleScore = peopleScore,
            knowledgeScore = knowledgeScore,
            readinessScore = calculatedReadiness,
            status = if (calculatedReadiness >= 75) "Ready to Push" else if (calculatedReadiness >= 50) "In Progress" else "Exploring",
            isFeatured = calculatedReadiness >= 80,
            stepsJson = ExecutionStep.listToJson(currentSteps),
            resourcesJson = IdeaResource.listToJson(currentResources)
        )
        viewModel.updateExistingIdea(updated) {
            isEditMode = false
            Toast.makeText(context, "Changes saved to Idea Matrix!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("idea_detail_edit_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top App Bar: Back Button, Mode Switch [View | Edit], Save / Delete
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (isEditMode) isEditMode = false else onDismiss()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.cardBackground)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Segmented Mode Toggle [ View | Edit ]
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (colors.isDark) DarkSurfaceElevated else Color(0x1F000000),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // View Tab
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (!isEditMode) (if (colors.isDark) SkyBluePrimary else Color.White) else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { isEditMode = false }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "View",
                                        tint = if (!isEditMode) (if (colors.isDark) DarkCardBlack else Color.Black) else colors.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "View",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (!isEditMode) (if (colors.isDark) DarkCardBlack else Color.Black) else colors.textSecondary
                                    )
                                }
                            }

                            // Edit Tab
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isEditMode) MintGreenAccent else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { isEditMode = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = if (isEditMode) DarkCardBlack else colors.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isEditMode) DarkCardBlack else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Quick Save button in header if in edit mode or dirty
                        if (isEditMode || hasUnsavedChanges) {
                            Button(
                                onClick = { saveAllEdits() },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreenAccent),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = DarkCardBlack,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Save",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DarkCardBlack
                                    )
                                }
                            }
                        }

                        // Delete button: Only Owner can delete the idea
                        if (isOwner) {
                            IconButton(
                                onClick = {
                                    viewModel.deleteIdea(idea.id)
                                    onDismiss()
                                    Toast.makeText(context, "Idea removed", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22EF4444))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Headline Card: Title, Description, Category (Editable)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (isEditMode) {
                            // Editable Category
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "CATEGORY (EDITABLE)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SkyBluePrimary
                                )
                                OutlinedTextField(
                                    value = editCategory,
                                    onValueChange = { editCategory = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    placeholder = { Text("e.g. B2B SaaS, Mobile App, AI Tool") },
                                    singleLine = true
                                )

                                // Preset Category Chips
                                val presetCategories = listOf(
                                    "B2B SaaS", "Mobile App", "AI Tool", "Creator Economy",
                                    "E-Commerce", "Fintech", "Health & Bio", "Community"
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    presetCategories.forEach { cat ->
                                        val isSelected = editCategory.equals(cat, ignoreCase = true)
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .clickable { editCategory = cat },
                                            shape = RoundedCornerShape(50),
                                            color = if (isSelected) MintGreenAccent else colors.chipBg
                                        ) {
                                            Text(
                                                text = cat,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) DarkCardBlack else colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // Editable Name
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "IDEA NAME",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SkyBluePrimary
                                )
                                OutlinedTextField(
                                    value = editTitle,
                                    onValueChange = { editTitle = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )
                            }

                            // Editable Pitch / Description
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "ONE-LINE PITCH & DESCRIPTION",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SkyBluePrimary
                                )
                                OutlinedTextField(
                                    value = editDescription,
                                    onValueChange = { editDescription = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    minLines = 2,
                                    maxLines = 4
                                )
                            }

                            // Editable First Action
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "FIRST ACTION ITEM",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SkyBluePrimary
                                )
                                OutlinedTextField(
                                    value = editNextAction,
                                    onValueChange = { editNextAction = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )
                            }
                        } else {
                            // View Mode Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = colors.chipBg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { isEditMode = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Category: ${editCategory}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.chipText
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Category",
                                            tint = SkyBluePrimary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                ReadinessPillBadge(percentage = calculatedReadiness, isDarkCard = colors.isDark)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = editTitle,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp
                                    ),
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { isEditMode = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Idea",
                                        tint = SkyBluePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = editDescription,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = colors.textSecondary
                            )

                            if (editNextAction.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Next Action:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SkyBluePrimary
                                    )
                                    Text(
                                        text = editNextAction,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Execution Lifecycle Status Card
            item {
                ExecutionStatusCard(
                    currentStatus = currentExecutionStatus,
                    canChangeStatus = canChangeStatus,
                    isOwner = isOwner,
                    isExecutor = isExecutor,
                    onStatusChange = { newStatus ->
                        currentExecutionStatus = newStatus
                        viewModel.changeIdeaExecutionStatus(idea, newStatus) { res ->
                            res.onSuccess {
                                Toast.makeText(context, "Execution status updated to $newStatus", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Failed to update status", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }

            // Timeline & Time Box Section
            item {
                TimelineSection(
                    idea = idea,
                    viewModel = viewModel
                )
            }

            // Simple Session Log with Live Timer
            item {
                WorkSessionTimerSection(
                    idea = idea,
                    viewModel = viewModel
                )
            }

            // Smart Reminders Settings
            item {
                ReminderSettingsSection(
                    idea = idea,
                    viewModel = viewModel
                )
            }

            // Audit Retrospective Quick Action
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showAuditScreen = true }
                        .testTag("btn_idea_audit_retrospective"),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.cardBackground,
                    border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SkyBluePrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = SkyBluePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Audit & Readiness Retrospective",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Weekly / monthly summary & bottleneck analysis",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Collaborators & Team Roles Card
            item {
                IdeaTeamCollaboratorsCard(
                    idea = idea,
                    isOwner = isOwner,
                    connections = acceptedConnections,
                    currentUserId = currentUser?.uid ?: "",
                    onInviteCollaborator = { collaborator, asExecutor ->
                        viewModel.inviteCollaboratorToIdea(idea, collaborator, asExecutor) { res ->
                            res.onSuccess {
                                Toast.makeText(context, "Invited ${collaborator.displayName} to idea!", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Failed to invite", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onSetExecutor = { memberUid ->
                        viewModel.setExecutor(idea, memberUid) { res ->
                            res.onSuccess {
                                Toast.makeText(context, "Executor assigned!", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Failed to assign executor", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onRemoveMember = { memberUid ->
                        viewModel.removeMember(idea, memberUid) { res ->
                            res.onSuccess {
                                Toast.makeText(context, "Member removed from idea", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Failed to remove member", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onDemoteExecutor = { memberUid ->
                        viewModel.demoteExecutor(idea, memberUid) { res ->
                            res.onSuccess {
                                Toast.makeText(context, "Appointed executor demoted to contributor", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Failed to demote executor", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }

            // Live Calculated Readiness Meter Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = if (colors.isDark) DarkCardBlack else colors.cardBackground,
                    border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Calculated Readiness",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = SkyBluePrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val (statusIcon, statusIconColor, statusText) = when {
                                calculatedReadiness >= 80 -> Triple(Icons.Filled.LocalFireDepartment, Color(0xFFF97316), "Ready to execute immediately")
                                calculatedReadiness >= 60 -> Triple(Icons.Filled.RocketLaunch, SkyBluePrimary, "Good foundation, slight bottlenecks")
                                else -> Triple(Icons.Filled.Spa, MintGreenDark, "Needs prep work in selected areas")
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = statusIcon,
                                    contentDescription = null,
                                    tint = statusIconColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        CircularReadinessMeter(
                            percentage = calculatedReadiness,
                            size = 64.dp,
                            strokeWidth = 6.dp,
                            textColor = if (colors.isDark) Color.White else colors.textPrimary
                        )
                    }
                }
            }

            // Requirements Section: Keep the features screenshotted above (make editable by user)
            // Screenshot 2: "What is holding you back right now?"
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "What is holding you back right now?",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Pick what you don’t have yet. This helps measure what’s missing. Selected chips turn blue.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        // Requirement Bottlenecks (Money, Skills, Time, Tools, People, Knowledge)
                        RequirementType.entries.forEach { req ->
                            val isSelected = selectedRequirements.contains(req)
                            val chipBg by animateColorAsState(
                                targetValue = if (isSelected) SkyBluePrimary else if (colors.isDark) DarkSurfaceElevated else colors.chipBg,
                                label = "req_detail_bg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) DarkCardBlack else colors.textPrimary,
                                label = "req_detail_text"
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        selectedRequirements = if (selectedRequirements.contains(req)) {
                                            if (selectedRequirements.size > 1) selectedRequirements - req else selectedRequirements
                                        } else {
                                            selectedRequirements + req
                                        }
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = chipBg,
                                border = if (!isSelected && colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0x33000000) else colors.cardBackground),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = req.icon,
                                                contentDescription = req.title,
                                                tint = if (isSelected) DarkCardBlack else SkyBluePrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = req.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                ),
                                                color = textColor
                                            )
                                            Text(
                                                text = req.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = if (isSelected) DarkCardBlack.copy(alpha = 0.8f) else colors.textSecondary
                                            )
                                        }
                                    }

                                    // Status Checkmark Pill
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(DarkCardBlack),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = SkyBluePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Requirement Sliders (from Screenshot 3 & 4)
                        if (selectedRequirements.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rate Your Readiness Sliders",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = SkyBluePrimary
                            )
                            Text(
                                text = "Adjust requirement sliders (0-100%). Circular readiness meter recalculates live.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )

                            selectedRequirements.forEach { req ->
                                val (currentScore, onScoreChange) = when (req) {
                                    RequirementType.MONEY -> moneyScore to { s: Int -> moneyScore = s }
                                    RequirementType.SKILLS -> skillsScore to { s: Int -> skillsScore = s }
                                    RequirementType.TIME -> timeScore to { s: Int -> timeScore = s }
                                    RequirementType.TOOLS -> toolsScore to { s: Int -> toolsScore = s }
                                    RequirementType.PEOPLE -> peopleScore to { s: Int -> peopleScore = s }
                                    RequirementType.KNOWLEDGE -> knowledgeScore to { s: Int -> knowledgeScore = s }
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (colors.isDark) DarkSurfaceElevated else colors.chipBg,
                                    border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = req.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "$currentScore%",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = SkyBluePrimary
                                            )
                                        }

                                        Slider(
                                            value = currentScore.toFloat(),
                                            onValueChange = { onScoreChange(it.toInt()) },
                                            valueRange = 0f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = SkyBluePrimary,
                                                activeTrackColor = SkyBluePrimary,
                                                inactiveTrackColor = if (colors.isDark) DarkBorder else Color(0x33000000)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Steps to Execution (one by one as much as the user want to add)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val completedCount = currentSteps.count { it.isCompleted }
                        val totalStepsCount = currentSteps.size
                        val progressRatio = if (totalStepsCount > 0) completedCount.toFloat() / totalStepsCount else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Steps to Execution",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Add steps to execution one by one as much as you want",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (progressRatio >= 1f && totalStepsCount > 0) MintGreenAccent else SkyBluePrimary,
                                        RoundedCornerShape(50)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$completedCount/$totalStepsCount Done",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DarkCardBlack
                                )
                            }
                        }

                        if (totalStepsCount > 0) {
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = if (progressRatio >= 1f) MintGreenAccent else SkyBluePrimary,
                                trackColor = if (colors.isDark) DarkBorder else Color(0x1F000000)
                            )
                        }

                        // Steps list
                        if (currentSteps.isEmpty()) {
                            Text(
                                text = "No execution steps yet. Add your milestones one by one below!",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        } else {
                            currentSteps.forEachIndexed { index, step ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (colors.isDark) DarkSurfaceElevated else colors.chipBg,
                                    border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    val updatedStep = step.copy(isCompleted = !step.isCompleted)
                                                    currentSteps[index] = updatedStep
                                                    val updatedIdea = idea.copy(stepsJson = ExecutionStep.listToJson(currentSteps))
                                                    viewModel.updateExistingIdea(updatedIdea)
                                                },
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (step.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = if (step.isCompleted) "Completed" else "Incomplete",
                                                tint = if (step.isCompleted) MintGreenAccent else colors.textSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text(
                                                text = step.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    textDecoration = if (step.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                                    fontWeight = if (step.isCompleted) FontWeight.Normal else FontWeight.Medium
                                                ),
                                                color = if (step.isCompleted) colors.textSecondary else colors.textPrimary
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                currentSteps.removeAt(index)
                                                val updatedIdea = idea.copy(stepsJson = ExecutionStep.listToJson(currentSteps))
                                                viewModel.updateExistingIdea(updatedIdea)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete Step",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add new step input row (one by one as much as the user wants to add)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newStepText,
                                onValueChange = { newStepText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Add next execution step...", color = colors.textSecondary) },
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newStepText.isNotBlank()) {
                                            currentSteps.add(ExecutionStep(title = newStepText.trim(), isCompleted = false))
                                            newStepText = ""
                                            val updatedIdea = idea.copy(stepsJson = ExecutionStep.listToJson(currentSteps))
                                            viewModel.updateExistingIdea(updatedIdea)
                                        }
                                    }
                                )
                            )

                            Button(
                                onClick = {
                                    if (newStepText.isNotBlank()) {
                                        currentSteps.add(ExecutionStep(title = newStepText.trim(), isCompleted = false))
                                        newStepText = ""
                                        val updatedIdea = idea.copy(stepsJson = ExecutionStep.listToJson(currentSteps))
                                        viewModel.updateExistingIdea(updatedIdea)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Step",
                                    tint = if (colors.isDark) DarkCardBlack else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Resources (Resou)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Resources",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Links, tools, documentation & notes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }

                            Button(
                                onClick = { showAddResourceDialog = !showAddResourceDialog },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = if (colors.isDark) DarkCardBlack else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Add Resource",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (colors.isDark) DarkCardBlack else Color.White
                                    )
                                }
                            }
                        }

                        // Add Resource Expandable Form
                        AnimatedVisibility(visible = showAddResourceDialog) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = if (colors.isDark) DarkSurfaceElevated else colors.chipBg,
                                border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Add New Resource",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SkyBluePrimary
                                    )

                                    OutlinedTextField(
                                        value = newResourceTitle,
                                        onValueChange = { newResourceTitle = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Resource Title (e.g. Stripe API Docs)") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    OutlinedTextField(
                                        value = newResourceUrlOrNote,
                                        onValueChange = { newResourceUrlOrNote = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("URL or Quick Note") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Resource Type chips (URL, Doc, PDF, Tool, Note)
                                    val resourceTypes = listOf("URL", "Doc", "PDF", "Tool", "Note")
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        resourceTypes.forEach { type ->
                                            val isSelected = newResourceType == type
                                            val pillColor = if (isSelected) {
                                                when (type) {
                                                    "PDF" -> Color(0xFFEF4444)
                                                    "Doc" -> Color(0xFF6366F1)
                                                    "URL" -> SkyBluePrimary
                                                    "Tool" -> MintGreenAccent
                                                    else -> if (colors.isDark) Color.White else DarkCardBlack
                                                }
                                            } else colors.chipBg

                                            val textColor = if (isSelected) {
                                                when (type) {
                                                    "PDF", "Doc", "URL", "Tool" -> DarkCardBlack
                                                    else -> if (colors.isDark) DarkCardBlack else Color.White
                                                }
                                            } else colors.textPrimary

                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable { newResourceType = type },
                                                shape = RoundedCornerShape(50),
                                                color = pillColor
                                            ) {
                                                Text(
                                                    text = type,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { showAddResourceDialog = false }) {
                                            Text("Cancel", color = colors.textSecondary)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = {
                                                if (newResourceTitle.isNotBlank()) {
                                                    currentResources.add(
                                                        IdeaResource(
                                                            title = newResourceTitle.trim(),
                                                            urlOrNote = newResourceUrlOrNote.trim(),
                                                            type = newResourceType
                                                        )
                                                    )
                                                    newResourceTitle = ""
                                                    newResourceUrlOrNote = ""
                                                    showAddResourceDialog = false
                                                    val updatedIdea = idea.copy(resourcesJson = IdeaResource.listToJson(currentResources))
                                                    viewModel.updateExistingIdea(updatedIdea)
                                                }
                                            },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack
                                            )
                                        ) {
                                            Text(
                                                text = "Save Resource",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (colors.isDark) DarkCardBlack else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Resources list
                        if (currentResources.isEmpty()) {
                            Text(
                                text = "No resources attached yet. Add links, tools, or notes to speed up your launch!",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        } else {
                            currentResources.forEachIndexed { index, res ->
                                val iconBg = when (res.type) {
                                    "PDF" -> Color(0x33EF4444)
                                    "Doc" -> Color(0x336366F1)
                                    "URL", "Link" -> if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight
                                    "Tool" -> Color(0x3310B981)
                                    else -> colors.chipBg
                                }
                                val iconTint = when (res.type) {
                                    "PDF" -> Color(0xFFEF4444)
                                    "Doc" -> Color(0xFF818CF8)
                                    "URL", "Link" -> SkyBluePrimary
                                    "Tool" -> MintGreenAccent
                                    else -> colors.textSecondary
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (colors.isDark) DarkSurfaceElevated else colors.chipBg,
                                    border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {

                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(iconBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (res.type) {
                                                        "PDF", "Doc" -> Icons.Default.Description
                                                        "URL", "Link" -> Icons.Default.Link
                                                        else -> Icons.Default.OpenInNew
                                                    },
                                                    contentDescription = res.type,
                                                    tint = iconTint,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = res.title,
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = colors.textPrimary
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .background(iconBg, RoundedCornerShape(50))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = res.type,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = iconTint
                                                        )
                                                    }
                                                }
                                                if (res.urlOrNote.isNotBlank()) {
                                                    Text(
                                                        text = res.urlOrNote,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                        color = colors.textSecondary,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val cleanUrl = res.urlOrNote.trim()
                                            val isWebUrl = cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://") || cleanUrl.startsWith("www.")
                                            if (isWebUrl) {
                                                IconButton(
                                                    onClick = {
                                                        try {
                                                            val targetUri = if (cleanUrl.startsWith("www.")) {
                                                                Uri.parse("https://$cleanUrl")
                                                            } else {
                                                                Uri.parse(cleanUrl)
                                                            }
                                                            val intent = Intent(Intent.ACTION_VIEW, targetUri)
                                                            context.startActivity(intent)
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, "Could not open resource link", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.OpenInNew,
                                                        contentDescription = "Open Link",
                                                        tint = iconTint,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    currentResources.removeAt(index)
                                                    val updatedIdea = idea.copy(resourcesJson = IdeaResource.listToJson(currentResources))
                                                    viewModel.updateExistingIdea(updatedIdea)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Delete Resource",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Activity Feed & Discussion Card
            item {
                IdeaActivityFeedCard(
                    activities = activities,
                    onPostComment = { text ->
                        viewModel.postComment(idea.firestoreId, text) { res ->
                            res.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Failed to post comment", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            // Save Floating Action Footer in Edit Mode or if Unsaved Changes
            if (isEditMode || hasUnsavedChanges) {
                item {
                    Button(
                        onClick = { saveAllEdits() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreenAccent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = DarkCardBlack,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Save All Changes",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkCardBlack
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
