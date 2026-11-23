package com.example.ui.screens.addidea

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IdeaEntity
import com.example.data.model.ExecutionStep
import com.example.data.model.IdeaResource
import com.example.data.model.RequirementType
import com.example.ui.components.CircularReadinessMeter
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.DarkSurfaceDeep
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.viewmodel.IdeaViewModel

@Composable
fun AddIdeaScreen(
    viewModel: IdeaViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(1) } // 1, 2, 3, 4
    val totalSteps = 4

    // Step 1 State: "What's your idea?"
    var ideaTitle by remember { mutableStateOf("") }
    var ideaDescription by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("B2B SaaS") }
    var nextAction by remember { mutableStateOf("Build initial interactive prototype") }

    // Step 2 State: "What do you need to start?" (Money, Skills, Time, Tools, People, Knowledge)
    var selectedRequirements by remember {
        mutableStateOf(
            setOf(RequirementType.SKILLS, RequirementType.TIME, RequirementType.TOOLS)
        )
    }

    // Step 3 State: Feasibility & Requirement Scores (0-100)
    var moneyScore by remember { mutableIntStateOf(80) }
    var skillsScore by remember { mutableIntStateOf(85) }
    var timeScore by remember { mutableIntStateOf(75) }
    var toolsScore by remember { mutableIntStateOf(90) }
    var peopleScore by remember { mutableIntStateOf(70) }
    var knowledgeScore by remember { mutableIntStateOf(85) }

    // Step 4 State: Steps to Execution & Resources
    val stepsList = remember { mutableStateListOf<ExecutionStep>() }
    val resourcesList = remember { mutableStateListOf<IdeaResource>() }

    // Calculated weighted readiness
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

    BackHandler {
        if (currentStep > 1) {
            currentStep--
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurfaceDeep)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("add_idea_screen_dark")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top Bar: Close Button & Segmented Progress Bar
            Spacer(modifier = Modifier.height(8.dp))
            TopBarWithSegmentedProgress(
                currentStep = currentStep,
                totalSteps = totalSteps,
                onClose = onDismiss
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Content Area (Scrollable)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                    label = "step_content"
                ) { step ->
                    when (step) {
                        1 -> StepOneWhatsYourIdea(
                            title = ideaTitle,
                            onTitleChange = { ideaTitle = it },
                            description = ideaDescription,
                            onDescriptionChange = { ideaDescription = it },
                            selectedCategory = selectedCategory,
                            onCategoryChange = { selectedCategory = it },
                            nextAction = nextAction,
                            onNextActionChange = { nextAction = it }
                        )

                        2 -> StepTwoWhatDoYouNeed(
                            selected = selectedRequirements,
                            onToggle = { req ->
                                selectedRequirements = if (selectedRequirements.contains(req)) {
                                    if (selectedRequirements.size > 1) selectedRequirements - req else selectedRequirements
                                } else {
                                    selectedRequirements + req
                                }
                            }
                        )

                        3 -> StepThreeRateRequirements(
                            selected = selectedRequirements,
                            calculatedReadiness = calculatedReadiness,
                            money = moneyScore,
                            onMoneyChange = { moneyScore = it },
                            skills = skillsScore,
                            onSkillsChange = { skillsScore = it },
                            time = timeScore,
                            onTimeChange = { timeScore = it },
                            tools = toolsScore,
                            onToolsChange = { toolsScore = it },
                            people = peopleScore,
                            onPeopleChange = { peopleScore = it },
                            knowledge = knowledgeScore,
                            onKnowledgeChange = { knowledgeScore = it }
                        )

                        4 -> StepFourStepsAndResources(
                            steps = stepsList,
                            onAddStep = { title ->
                                stepsList.add(ExecutionStep(title = title, isCompleted = false))
                            },
                            onRemoveStep = { idx ->
                                stepsList.removeAt(idx)
                            },
                            resources = resourcesList,
                            onAddResource = { title, urlOrNote, type ->
                                resourcesList.add(IdeaResource(title = title, urlOrNote = urlOrNote, type = type))
                            },
                            onRemoveResource = { idx ->
                                resourcesList.removeAt(idx)
                            }
                        )
                    }
                }
            }

            // Bottom Navigation: White Next Pill & Skip Text Button
            BottomControlButtons(
                currentStep = currentStep,
                totalSteps = totalSteps,
                canProceed = when (currentStep) {
                    1 -> ideaTitle.isNotBlank()
                    2 -> selectedRequirements.isNotEmpty()
                    else -> true
                },
                onNext = {
                    if (currentStep < totalSteps) {
                        currentStep++
                    } else {
                        // Save & finish
                        viewModel.addNewIdea(
                            title = ideaTitle.ifBlank { "Untitled Project" },
                            description = ideaDescription.ifBlank { "New high-potential startup bet." },
                            category = selectedCategory.ifBlank { "General Tech" },
                            neededRequirements = selectedRequirements,
                            moneyScore = moneyScore,
                            skillsScore = skillsScore,
                            timeScore = timeScore,
                            toolsScore = toolsScore,
                            peopleScore = peopleScore,
                            knowledgeScore = knowledgeScore,
                            nextAction = nextAction.ifBlank { "Prototype validation" },
                            steps = stepsList.toList(),
                            resources = resourcesList.toList(),
                            onSuccess = { onDismiss() }
                        )
                    }
                },
                onSkip = {
                    if (currentStep < 3) {
                        currentStep = 3
                    } else if (currentStep == 3) {
                        currentStep = 4
                    } else {
                        // Save directly
                        viewModel.addNewIdea(
                            title = ideaTitle.ifBlank { "Untitled Project" },
                            description = ideaDescription.ifBlank { "New high-potential startup bet." },
                            category = selectedCategory.ifBlank { "General Tech" },
                            neededRequirements = selectedRequirements,
                            moneyScore = moneyScore,
                            skillsScore = skillsScore,
                            timeScore = timeScore,
                            toolsScore = toolsScore,
                            peopleScore = peopleScore,
                            knowledgeScore = knowledgeScore,
                            nextAction = nextAction.ifBlank { "Prototype validation" },
                            steps = stepsList.toList(),
                            resources = resourcesList.toList(),
                            onSuccess = { onDismiss() }
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TopBarWithSegmentedProgress(
    currentStep: Int,
    totalSteps: Int,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextWhiteMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = SkyBluePrimary
            )

            // Balance placeholder
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 1..totalSteps) {
                val isFilled = i <= currentStep
                val segmentColor by animateColorAsState(
                    targetValue = if (isFilled) SkyBluePrimary else DarkBorder,
                    label = "segment_color"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(segmentColor)
                )
            }
        }
    }
}

@Composable
fun StepOneWhatsYourIdea(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    nextAction: String,
    onNextActionChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Headline
        Text(
            text = "What’s your idea?",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            ),
            color = TextWhite
        )

        Text(
            text = "Give your concept a clear name and a concise one-line elevator pitch.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextWhiteMuted
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Idea Name Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "IDEA NAME",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SkyBluePrimary
            )
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_idea_name"),
                placeholder = {
                    Text(
                        "e.g. AI Workflow Automator",
                        color = TextWhiteMuted.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true
            )
        }

        // Elevator Pitch Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "ONE-LINE PITCH",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SkyBluePrimary
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_idea_pitch"),
                placeholder = {
                    Text(
                        "What problem does this solve and for whom?",
                        color = TextWhiteMuted.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                minLines = 2,
                maxLines = 3
            )
        }

        // Category Selection (Editable)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "CATEGORY (EDITABLE)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SkyBluePrimary
            )
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = onCategoryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_idea_category"),
                placeholder = {
                    Text(
                        "Type custom category or select below...",
                        color = TextWhiteMuted.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true
            )
            val categories = listOf("B2B SaaS", "Mobile App", "AI Tool", "Creator Economy", "E-Commerce", "Fintech", "Health & Bio", "Community")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onCategoryChange(cat) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) MintGreenAccent else DarkSurfaceElevated
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) DarkCardBlack else TextWhite
                        )
                    }
                }
            }
        }

        // Immediate Action
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "FIRST ACTION ITEM",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SkyBluePrimary
            )
            OutlinedTextField(
                value = nextAction,
                onValueChange = onNextActionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_next_action"),
                placeholder = {
                    Text(
                        "What is the very first step?",
                        color = TextWhiteMuted.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedBorderColor = SkyBluePrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun StepTwoWhatDoYouNeed(
    selected: Set<RequirementType>,
    onToggle: (RequirementType) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "What is holding you back right now?",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            ),
            color = TextWhite
        )

        Text(
            text = "Pick what you don’t have yet. This helps measure what’s missing.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextWhiteMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-select chips: Money, Skills, Time, Tools, People, Knowledge
        // Requirements turn sky blue (#49CBEB) when selected!
        RequirementType.entries.forEach { req ->
            val isSelected = selected.contains(req)
            val chipBg by animateColorAsState(
                targetValue = if (isSelected) SkyBluePrimary else DarkSurfaceElevated,
                label = "req_chip_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) DarkCardBlack else TextWhite,
                label = "req_chip_text"
            )
            val descColor by animateColorAsState(
                targetValue = if (isSelected) DarkCardBlack.copy(alpha = 0.8f) else TextWhiteMuted,
                label = "req_desc_text"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("req_chip_${req.title}")
                    .shadow(
                        elevation = if (isSelected) 8.dp else 2.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = if (isSelected) SkyBluePrimary else Color.Transparent
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) SkyBluePrimary else DarkBorder,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable { onToggle(req) },
                shape = RoundedCornerShape(24.dp),
                color = chipBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0x33000000) else DarkCardBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = req.icon,
                                contentDescription = req.title,
                                tint = if (isSelected) DarkCardBlack else SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = req.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = textColor
                            )
                            Text(
                                text = req.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = descColor
                            )
                        }
                    }

                    // Checkmark or pill circle
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) DarkCardBlack else Color(0x33FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
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
    }
}

@Composable
fun StepThreeRateRequirements(
    selected: Set<RequirementType>,
    calculatedReadiness: Int,
    money: Int,
    onMoneyChange: (Int) -> Unit,
    skills: Int,
    onSkillsChange: (Int) -> Unit,
    time: Int,
    onTimeChange: (Int) -> Unit,
    tools: Int,
    onToolsChange: (Int) -> Unit,
    people: Int,
    onPeopleChange: (Int) -> Unit,
    knowledge: Int,
    onKnowledgeChange: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Rate your readiness",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            ),
            color = TextWhite
        )

        Text(
            text = "Adjust each requirement score. Your readiness score is a weighted average.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextWhiteMuted
        )

        // Live Calculated Readiness Meter Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DarkCardBlack,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
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
                        else -> Triple(Icons.Filled.Spa, Color(0xFF86F5D0), "Needs prep work in selected areas")
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
                            color = TextWhiteMuted
                        )
                    }
                }

                CircularReadinessMeter(
                    percentage = calculatedReadiness,
                    size = 64.dp,
                    strokeWidth = 6.dp,
                    textColor = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Sliders for selected requirements
        selected.forEach { req ->
            val (currentScore, onScoreChange) = when (req) {
                RequirementType.MONEY -> money to onMoneyChange
                RequirementType.SKILLS -> skills to onSkillsChange
                RequirementType.TIME -> time to onTimeChange
                RequirementType.TOOLS -> tools to onToolsChange
                RequirementType.PEOPLE -> people to onPeopleChange
                RequirementType.KNOWLEDGE -> knowledge to onKnowledgeChange
            }

            RequirementSliderCard(
                req = req,
                score = currentScore,
                onScoreChange = onScoreChange
            )
        }
    }
}

@Composable
fun RequirementSliderCard(
    req: RequirementType,
    score: Int,
    onScoreChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = req.icon,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = req.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .background(DarkCardBlack, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$score%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MintGreenAccent
                    )
                }
            }

            Slider(
                value = score.toFloat(),
                onValueChange = { onScoreChange(it.toInt()) },
                valueRange = 0f..100f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = SkyBluePrimary,
                    activeTrackColor = SkyBluePrimary,
                    inactiveTrackColor = DarkBorder
                )
            )
        }
    }
}

@Composable
fun BottomControlButtons(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Skip Text Button
        TextButton(
            onClick = onSkip,
            modifier = Modifier.testTag("btn_skip")
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = TextWhiteMuted
            )
        }

        // White Next Pill Button
        Button(
            onClick = onNext,
            enabled = canProceed,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = DarkCardBlack,
                disabledContainerColor = Color.White.copy(alpha = 0.3f),
                disabledContentColor = DarkCardBlack.copy(alpha = 0.4f)
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
            modifier = Modifier
                .testTag("btn_next_pill")
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(50))
        ) {
            Text(
                text = if (currentStep == totalSteps) "Save to Matrix" else "Next",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = DarkCardBlack
            )
        }
    }
}

@Composable
fun StepFourStepsAndResources(
    steps: List<ExecutionStep>,
    onAddStep: (String) -> Unit,
    onRemoveStep: (Int) -> Unit,
    resources: List<IdeaResource>,
    onAddResource: (String, String, String) -> Unit,
    onRemoveResource: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    var newStepInput by remember { mutableStateOf("") }

    var resTitleInput by remember { mutableStateOf("") }
    var resUrlInput by remember { mutableStateOf("") }
    var resTypeInput by remember { mutableStateOf("URL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Execution Steps & Resources",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            ),
            color = TextWhite
        )

        Text(
            text = "Add steps to execution one by one as much as you want, and attach links or reference playbooks.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextWhiteMuted
        )

        // Section A: Steps to Execution
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = DarkCardBlack,
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STEPS TO EXECUTION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SkyBluePrimary
                    )
                    Text(
                        text = "${steps.size} added",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MintGreenAccent
                    )
                }

                // Add step row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newStepInput,
                        onValueChange = { newStepInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_new_step"),
                        placeholder = {
                            Text(
                                "e.g. Build prototype landing page",
                                color = TextWhiteMuted.copy(alpha = 0.6f)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (newStepInput.isNotBlank()) {
                                onAddStep(newStepInput.trim())
                                newStepInput = ""
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkCardBlack
                        )
                    }
                }

                // List of steps
                if (steps.isEmpty()) {
                    Text(
                        text = "No steps added yet. Add your sequential milestones above!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhiteMuted
                    )
                } else {
                    steps.forEachIndexed { index, step ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurfaceElevated,
                            border = BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(DarkCardBlack),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = SkyBluePrimary
                                        )
                                    }

                                    Text(
                                        text = step.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = TextWhite
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveStep(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Step",
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

        // Section B: Resources (Resou)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = DarkCardBlack,
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RESOURCES & TOOLS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SkyBluePrimary
                    )
                    Text(
                        text = "${resources.size} added",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MintGreenAccent
                    )
                }

                OutlinedTextField(
                    value = resTitleInput,
                    onValueChange = { resTitleInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Resource title (e.g. Supabase Auth Docs)",
                            color = TextWhiteMuted.copy(alpha = 0.6f)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = SkyBluePrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = resUrlInput,
                        onValueChange = { resUrlInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "URL or brief note",
                                color = TextWhiteMuted.copy(alpha = 0.6f)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (resTitleInput.isNotBlank()) {
                                onAddResource(resTitleInput.trim(), resUrlInput.trim(), resTypeInput)
                                resTitleInput = ""
                                resUrlInput = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreenAccent),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkCardBlack
                        )
                    }
                }

                // Type Chips (URL, Doc, PDF, Tool, Note)
                val types = listOf("URL", "Doc", "PDF", "Tool", "Note")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { type ->
                        val isSelected = resTypeInput == type
                        val pillColor = if (isSelected) {
                            when (type) {
                                "PDF" -> Color(0xFFEF4444)
                                "Doc" -> Color(0xFF6366F1)
                                "URL" -> SkyBluePrimary
                                "Tool" -> MintGreenAccent
                                else -> TextWhite
                            }
                        } else DarkSurfaceElevated

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { resTypeInput = type },
                            shape = RoundedCornerShape(50),
                            color = pillColor
                        ) {
                            Text(
                                text = type,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) DarkCardBlack else TextWhite
                            )
                        }
                    }
                }

                // Resources List
                if (resources.isNotEmpty()) {
                    resources.forEachIndexed { index, res ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurfaceElevated,
                            border = BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = res.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextWhite
                                        )
                                        val badgeBg = when (res.type) {
                                            "PDF" -> Color(0x33EF4444)
                                            "Doc" -> Color(0x336366F1)
                                            "URL", "Link" -> Color(0x3349CBEB)
                                            "Tool" -> Color(0x3310B981)
                                            else -> DarkCardBlack
                                        }
                                        val badgeColor = when (res.type) {
                                            "PDF" -> Color(0xFFEF4444)
                                            "Doc" -> Color(0xFF818CF8)
                                            "URL", "Link" -> SkyBluePrimary
                                            "Tool" -> MintGreenAccent
                                            else -> TextWhiteMuted
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(badgeBg, RoundedCornerShape(50))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = res.type,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = badgeColor
                                            )
                                        }
                                    }
                                    if (res.urlOrNote.isNotBlank()) {
                                        Text(
                                            text = res.urlOrNote,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = TextWhiteMuted,
                                            maxLines = 1
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onRemoveResource(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Resource",
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

