package com.example.ui.screens.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BatteryChargingFull
import com.example.ui.screens.audit.AuditScreen
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WorkspacePremium
import com.example.ui.viewmodel.FeelingEffectType
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.components.DailyFeelingEffectOverlay
import kotlin.math.sin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.IdeaEntity
import com.example.ui.components.CircularReadinessMeter
import com.example.ui.components.DaysRemainingChip
import com.example.ui.components.MemberAvatarsRow
import com.example.ui.components.ReadinessPillBadge
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.MintGreenLight
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.viewmodel.DailyFeelingOption
import com.example.ui.viewmodel.IdeaViewModel

@Composable
fun HomeScreen(
    viewModel: IdeaViewModel,
    onNavigateToAddIdea: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredIdeas by viewModel.filteredIdeas.collectAsState()
    val topFeaturedIdeas by viewModel.topFeaturedIdeas.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedFeeling by viewModel.selectedFeeling.collectAsState()
    val showNotificationDialog by viewModel.showNotificationDialog.collectAsState()
    val shouldShowDailyFeelingButtons by viewModel.shouldShowDailyFeelingButtons.collectAsState()
    val activeEffectOption by viewModel.activeEffectOption.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userFirstName = remember(userName) { viewModel.getFirstName() }
    val currentUser by viewModel.currentUser.collectAsState()

    var activeStartIdea by remember { mutableStateOf<IdeaEntity?>(null) }
    var showAuditScreen by remember { mutableStateOf(false) }

    if (showAuditScreen) {
        AuditScreen(
            viewModel = viewModel,
            onBack = { showAuditScreen = false }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_list"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header: Greeting, Dark Mode toggle, Bell Button
            item {
                HomeHeader(
                    userFirstName = userFirstName,
                    isDarkMode = colors.isDark,
                    onToggleDarkMode = viewModel::toggleDarkMode,
                    onBellClick = { viewModel.toggleNotificationDialog(true) },
                    onAuditClick = { showAuditScreen = true }
                )
            }

            // Search Pill Bar
            item {
                SearchPillBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Daily Check-in: "How ready do you feel today?" - Only appears if visiting for first time today according to user timezone
            if (shouldShowDailyFeelingButtons) {
                item {
                    AnimatedVisibility(
                        visible = shouldShowDailyFeelingButtons,
                        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                        exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                    ) {
                        DailyCheckInSection(
                            options = viewModel.feelingOptions,
                            selectedOption = selectedFeeling,
                            onSelect = viewModel::triggerDailyFeelingSelection,
                            modifier = Modifier.padding(top = 12.dp, bottom = 18.dp)
                        )
                    }
                }
            }

            // Two Featured Idea Cards (Top priority - Most ready in black)
            if (topFeaturedIdeas.isNotEmpty() && searchQuery.isBlank()) {
                item {
                    FeaturedIdeasSection(
                        ideas = topFeaturedIdeas,
                        onIdeaClick = { viewModel.selectIdeaForDetail(it) },
                        onEditClick = { viewModel.selectIdeaForDetail(it, editMode = true) },
                        onStartClick = { activeStartIdea = it }
                    )
                }
            }

            // "Ready to push" Section Title & Filter Chips
            item {
                ReadyToPushHeader(
                    selectedFilter = selectedFilter,
                    onFilterSelect = viewModel::onFilterSelected
                )
            }

            // "Ready to push" List of Idea Cards
            if (filteredIdeas.isEmpty()) {
                item {
                    EmptyIdeasState(
                        isSearching = searchQuery.isNotBlank(),
                        onAddClick = onNavigateToAddIdea
                    )
                }
            } else {
                items(filteredIdeas, key = { it.id }) { idea ->
                    val isOwner = idea.isOwner(currentUser?.uid) || idea.ownerId.isBlank()
                    IdeaCard(
                        idea = idea,
                        onCardClick = { viewModel.selectIdeaForDetail(idea) },
                        onEditClick = { viewModel.selectIdeaForDetail(idea, editMode = true) },
                        onStartClick = { activeStartIdea = idea },
                        onDeleteClick = { viewModel.deleteIdea(idea.id) },
                        showDelete = isOwner,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Floating Action Button to Add Idea
        FloatingActionButton(
            onClick = onNavigateToAddIdea,
            containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack,
            contentColor = if (colors.isDark) DarkCardBlack else MintGreenAccent,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp)
                .testTag("fab_add_idea")
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Idea",
                    tint = if (colors.isDark) DarkCardBlack else MintGreenAccent
                )
                Text(
                    text = "New Idea",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = if (colors.isDark) DarkCardBlack else Color.White
                )
            }
        }
    }

    // Notification Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleNotificationDialog(false) },
            confirmButton = {
                Button(
                    onClick = { viewModel.toggleNotificationDialog(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Got it", color = if (colors.isDark) DarkCardBlack else Color.White)
                }
            },
            title = {
                Text(
                    text = "Idea Matrix Daily Dispatch",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = MintGreenAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "1 idea reached 90%+ readiness this week!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = "Consistency reminder: Check your resource bottlenecks before shipping marketing campaigns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBackground
        )
    }

    // Active Idea "Start" Modal
    if (activeStartIdea != null) {
        val idea = activeStartIdea!!
        val canChangeStatus = idea.canChangeExecutionStatus(currentUser?.uid)
        val isOwner = idea.isOwner(currentUser?.uid) || idea.ownerId.isBlank()
        val isExecutor = idea.isExecutor(currentUser?.uid)
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { activeStartIdea = null },
            confirmButton = {
                Button(
                    onClick = {
                        if (canChangeStatus && (idea.executionStatus == "Not Started" || idea.executionStatus.isBlank())) {
                            viewModel.changeIdeaExecutionStatus(idea, "Started") { res ->
                                res.onSuccess {
                                    Toast.makeText(context, "Execution status updated to Started", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        activeStartIdea = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreenAccent),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (canChangeStatus && (idea.executionStatus == "Not Started" || idea.executionStatus.isBlank())) "Start Execution" else "Done",
                        color = DarkCardBlack,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { activeStartIdea = null }) {
                    Text("Close", color = colors.textSecondary)
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = SkyBluePrimary
                    )
                    Text(
                        text = "Execution: ${idea.title}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = idea.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )

                    // Execution Status Actions (Owner & Executor)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Execution Lifecycle:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Current: ${idea.executionStatus.ifBlank { "Not Started" }}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SkyBluePrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Started", "Done", "Live").forEach { statusKey ->
                                val isSelected = idea.executionStatus.equals(statusKey, ignoreCase = true)
                                val bg = when (statusKey) {
                                    "Live" -> MintGreenAccent
                                    "Done" -> SkyBluePrimary
                                    else -> Color(0xFFF59E0B)
                                }
                                Button(
                                    onClick = {
                                        if (canChangeStatus) {
                                            viewModel.changeIdeaExecutionStatus(idea, statusKey) { res ->
                                                res.onSuccess {
                                                    Toast.makeText(context, "Status set to $statusKey", Toast.LENGTH_SHORT).show()
                                                    activeStartIdea = null
                                                }.onFailure { err ->
                                                    Toast.makeText(context, err.message ?: "Failed to update", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    enabled = canChangeStatus,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) bg else (if (colors.isDark) DarkCardBlack else Color(0xFFF1F5F9)),
                                        contentColor = if (isSelected) DarkCardBlack else colors.textPrimary,
                                        disabledContainerColor = if (isSelected) bg.copy(alpha = 0.5f) else Color(0x15FFFFFF),
                                        disabledContentColor = colors.textSecondary.copy(alpha = 0.5f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = when(statusKey) {
                                                "Started" -> Icons.Default.RocketLaunch
                                                "Done" -> Icons.Default.CheckCircle
                                                else -> Icons.Default.Public
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = statusKey,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        if (canChangeStatus && idea.executionStatus.isNotBlank() && idea.executionStatus != "Not Started") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.changeIdeaExecutionStatus(idea, "Not Started") { res ->
                                            res.onSuccess {
                                                Toast.makeText(context, "Status reset to Not Started", Toast.LENGTH_SHORT).show()
                                                activeStartIdea = null
                                            }
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Reset to Not Started",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }

                        if (!canChangeStatus) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Only the Creator and Appointed Executors can change execution status.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = colors.textSecondary
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isOwner) SkyBluePrimary.copy(alpha = 0.15f) else MintGreenAccent.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOwner) Icons.Filled.WorkspacePremium else Icons.Filled.Bolt,
                                        contentDescription = null,
                                        tint = if (isOwner) SkyBluePrimary else MintGreenAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isOwner) "Creator privileges: You can change lifecycle stages" else "Appointed Executor privileges: You can change lifecycle stages",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = if (isOwner) SkyBluePrimary else MintGreenAccent
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (colors.isDark) DarkSurfaceElevated else SkyBlueLight,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Next Action Item",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (colors.isDark) SkyBluePrimary else Color(0xFF0369A1)
                            )
                            Text(
                                text = idea.nextAction,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.textPrimary
                            )
                        }
                    }

                    Text(
                        text = "Calculated readiness: ${idea.readinessScore}%. Key requirements scored.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBackground
        )
    }

    // Interactive Daily Feeling Animation Effect Overlay & Popup
    activeEffectOption?.let { option ->
        DailyFeelingEffectOverlay(
            option = option,
            onDismiss = {
                viewModel.completeDailyFeelingSelection(option)
            }
        )
    }
}

@Composable
fun HomeHeader(
    userFirstName: String,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onBellClick: () -> Unit,
    onAuditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.cardBackground)
                    .border(
                        BorderStroke(1.dp, if (colors.isDark) colors.cardBorder else Color.Transparent),
                        RoundedCornerShape(14.dp)
                    )
                    .shadow(elevation = if (colors.isDark) 2.dp else 4.dp, shape = RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_official_logo),
                    contentDescription = "Idea Matrix Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column {
                Text(
                    text = "Hello $userFirstName",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = colors.textPrimary
                )
                Text(
                    text = "Build what's ready. Validate what's next.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Audit Screen Quick Action Button
            Box(
                modifier = Modifier
                    .shadow(elevation = if (isDarkMode) 2.dp else 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(colors.cardBackground)
                    .border(
                        BorderStroke(1.dp, if (isDarkMode) colors.cardBorder else Color.Transparent),
                        CircleShape
                    )
            ) {
                IconButton(
                    onClick = onAuditClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("btn_audit_header")
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Audit Reports",
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Dark Mode Quick Toggle
            Box(
                modifier = Modifier
                    .shadow(elevation = if (isDarkMode) 2.dp else 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(colors.cardBackground)
                    .border(
                        BorderStroke(1.dp, if (isDarkMode) colors.cardBorder else Color.Transparent),
                        CircleShape
                    )
            ) {
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("btn_toggle_dark_mode")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark Mode",
                        tint = if (isDarkMode) MintGreenAccent else colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bell Notifications Button
            Box(
                modifier = Modifier
                    .shadow(elevation = if (isDarkMode) 2.dp else 4.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(colors.cardBackground)
                    .border(
                        BorderStroke(1.dp, if (isDarkMode) colors.cardBorder else Color.Transparent),
                        CircleShape
                    )
            ) {
                IconButton(
                    onClick = onBellClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("btn_notifications")
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        // Notification dot
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(SkyBluePrimary, CircleShape)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchPillBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 2.dp else 6.dp,
                shape = RoundedCornerShape(50),
                spotColor = Color(0x14000000),
                ambientColor = Color(0x0A000000)
            ),
        shape = RoundedCornerShape(50),
        color = colors.searchBarBg,
        border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_pill_input"),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search ideas, tags, requirements...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary.copy(alpha = 0.8f)
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DailyCheckInSection(
    options: List<DailyFeelingOption>,
    selectedOption: DailyFeelingOption?,
    onSelect: (DailyFeelingOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    // Floating animation when user has not selected any option yet
    val infiniteTransition = rememberInfiniteTransition(label = "feeling_float")
    val floatPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "float_phase"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "How ready do you feel today?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = colors.textPrimary
            )
            if (selectedOption != null) {
                Text(
                    text = "${selectedOption.readiness}% logged",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MintGreenDark
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Swipeable / scrollable chip row with float animation
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = selectedOption?.title == option.title

                // Floating bob effect: distinct phase offset per button
                val phaseOffset = index * 0.95f
                val floatY = (sin(floatPhase + phaseOffset) * 5.5f).dp
                val floatScale = 1f + (sin(floatPhase + phaseOffset) * 0.02f)

                val bg by animateColorAsState(
                    targetValue = if (isSelected) SkyBluePrimary else colors.cardBackground,
                    label = "chip_bg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) DarkCardBlack else colors.textPrimary,
                    label = "chip_text"
                )

                Surface(
                    modifier = Modifier
                        .testTag("feeling_chip_${option.readiness}")
                        .offset(y = floatY)
                        .scale(floatScale)
                        .shadow(
                            elevation = if (isSelected) 8.dp else 4.dp,
                            shape = RoundedCornerShape(50),
                            spotColor = if (colors.isDark) Color(0x66000000) else Color(0x1F000000)
                        )
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelect(option) },
                    shape = RoundedCornerShape(50),
                    color = bg,
                    border = if (!isSelected && colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val optionIcon = when (option.effectType) {
                            FeelingEffectType.THUNDER_LIGHTNING -> Icons.Default.Bolt
                            FeelingEffectType.FLAMES -> Icons.Default.LocalFireDepartment
                            FeelingEffectType.ROCKET_TAKEOFF -> Icons.Default.RocketLaunch
                            FeelingEffectType.PLANT_SPURT -> Icons.Default.Spa
                            FeelingEffectType.BATTERY_CHARGING -> Icons.Default.BatteryChargingFull
                        }
                        val optionIconTint = if (isSelected) DarkCardBlack else when (option.effectType) {
                            FeelingEffectType.THUNDER_LIGHTNING -> SkyBluePrimary
                            FeelingEffectType.FLAMES -> Color(0xFFF97316)
                            FeelingEffectType.ROCKET_TAKEOFF -> SkyBluePrimary
                            FeelingEffectType.PLANT_SPURT -> MintGreenDark
                            FeelingEffectType.BATTERY_CHARGING -> MintGreenDark
                        }
                        Icon(
                            imageVector = optionIcon,
                            contentDescription = null,
                            tint = optionIconTint,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = option.title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = textColor
                        )
                        Text(
                            text = "(${option.readiness}%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) DarkCardBlack.copy(alpha = 0.85f) else colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedIdeasSection(
    ideas: List<IdeaEntity>,
    onStartClick: (IdeaEntity) -> Unit,
    onIdeaClick: (IdeaEntity) -> Unit = {},
    onEditClick: (IdeaEntity) -> Unit = onIdeaClick,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Featured Priority Bets",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = colors.textPrimary
            )
            Text(
                text = "Highest Readiness",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // First Featured Card: The Most Ready Idea in Black (#121417)
        val firstIdea = ideas.firstOrNull()
        if (firstIdea != null) {
            DarkFeaturedIdeaCard(
                idea = firstIdea,
                onCardClick = { onIdeaClick(firstIdea) },
                onEditClick = { onEditClick(firstIdea) },
                onStartClick = { onStartClick(firstIdea) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Second Featured Card (if present)
        if (ideas.size > 1) {
            val secondIdea = ideas[1]
            SecondFeaturedIdeaCard(
                idea = secondIdea,
                onCardClick = { onIdeaClick(secondIdea) },
                onEditClick = { onEditClick(secondIdea) },
                onStartClick = { onStartClick(secondIdea) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun DarkFeaturedIdeaCard(
    idea: IdeaEntity,
    onStartClick: () -> Unit,
    onCardClick: () -> Unit = {},
    onEditClick: () -> Unit = onCardClick,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("featured_idea_card_black")
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x3D000000),
                ambientColor = Color(0x1F000000)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBlack),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Top Row: Category Chip & Readiness Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip in mint green pill
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0x3386F5D0), Color(0x3349CBEB))
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = idea.category,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MintGreenAccent
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DaysRemainingChip(idea = idea)
                    val members = idea.getMembersList()
                    if (members.isNotEmpty()) {
                        MemberAvatarsRow(members = members, avatarSize = 24.dp)
                    }

                    CircularReadinessMeter(
                        percentage = idea.readinessScore,
                        size = 52.dp,
                        strokeWidth = 5.dp,
                        textColor = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            Text(
                text = idea.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = idea.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp
                ),
                color = TextWhiteMuted,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Requirements chips
            val reqs = idea.getRequirementsSet()
            if (reqs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reqs.forEach { req ->
                        val score = idea.getScoreFor(req)
                        Box(
                            modifier = Modifier
                                .background(DarkSurfaceElevated, RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${req.title}: $score%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFFD1D5DB)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Bottom Row: Milestone hint & Pill-shaped "Start" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Next step:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextWhiteMuted
                    )
                    Text(
                        text = idea.nextAction,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextWhite,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onEditClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = SkyBluePrimary
                            )
                        }
                    }

                    // Start pill-shaped button
                    Button(
                        onClick = onStartClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreenAccent),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("btn_start_featured")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Start",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = DarkCardBlack
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = DarkCardBlack,
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
fun SecondFeaturedIdeaCard(
    idea: IdeaEntity,
    onStartClick: () -> Unit,
    onCardClick: () -> Unit = {},
    onEditClick: () -> Unit = onCardClick,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .shadow(
                elevation = if (colors.isDark) 3.dp else 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x0A000000)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = idea.category,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (colors.isDark) SkyBluePrimary else Color(0xFF0369A1)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DaysRemainingChip(idea = idea)
                    val members = idea.getMembersList()
                    if (members.isNotEmpty()) {
                        MemberAvatarsRow(members = members, avatarSize = 22.dp)
                    }
                    ReadinessPillBadge(percentage = idea.readinessScore, isDarkCard = colors.isDark)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = idea.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = idea.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Action: ${idea.nextAction}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 1
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextButton(
                        onClick = onEditClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (colors.isDark) SkyBluePrimary else Color(0xFF0369A1),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (colors.isDark) SkyBluePrimary else Color(0xFF0369A1)
                            )
                        }
                    }

                    Button(
                        onClick = onStartClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Advance",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkCardBlack
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReadyToPushHeader(
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ready to push",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = colors.textPrimary
            )
            Text(
                text = "Matrix Queue",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        val filters = listOf("All", "High Readiness", "In Progress", "Validating")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = filter == selectedFilter
                val pillBg = if (isSelected) {
                    if (colors.isDark) SkyBluePrimary else DarkCardBlack
                } else {
                    colors.chipBg
                }
                val pillText = if (isSelected) {
                    if (colors.isDark) DarkCardBlack else Color.White
                } else {
                    colors.chipText
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onFilterSelect(filter) },
                    shape = RoundedCornerShape(50),
                    color = pillBg,
                    border = if (!isSelected && colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                ) {
                    Text(
                        text = filter,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = pillText
                    )
                }
            }
        }
    }
}

@Composable
fun IdeaCard(
    idea: IdeaEntity,
    onStartClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCardClick: () -> Unit = {},
    onEditClick: () -> Unit = onCardClick,
    showDelete: Boolean = true,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .shadow(
                elevation = if (colors.isDark) 2.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x14000000),
                ambientColor = Color(0x08000000)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Category pill, Execution status, Member avatars & Readiness badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(colors.chipBg, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = idea.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.chipText
                        )
                    }

                    if (idea.executionStatus != "Not Started" && idea.executionStatus.isNotBlank()) {
                        val statusBg = when (idea.executionStatus) {
                            "Live" -> Color(0x3386F5D0)
                            "Done" -> Color(0x3349CBEB)
                            else -> Color(0x33F59E0B)
                        }
                        val statusText = when (idea.executionStatus) {
                            "Live" -> MintGreenDark
                            "Done" -> SkyBluePrimary
                            else -> Color(0xFFD97706)
                        }
                        Box(
                            modifier = Modifier
                                .background(statusBg, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = when (idea.executionStatus.lowercase()) {
                                        "started" -> Icons.Default.RocketLaunch
                                        "done" -> Icons.Default.CheckCircle
                                        "live" -> Icons.Default.Public
                                        else -> Icons.Default.PlayArrow
                                    },
                                    contentDescription = null,
                                    tint = statusText,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = idea.executionStatus,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = statusText
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DaysRemainingChip(idea = idea)
                    val members = idea.getMembersList()
                    if (members.isNotEmpty()) {
                        MemberAvatarsRow(members = members, avatarSize = 22.dp)
                    }
                    ReadinessPillBadge(percentage = idea.readinessScore, isDarkCard = colors.isDark)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = idea.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Pitch
            Text(
                text = idea.description,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = colors.textSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Requirement score chips
            val reqs = idea.getRequirementsSet()
            if (reqs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reqs.take(4).forEach { req ->
                        val score = idea.getScoreFor(req)
                        val badgeBg = if (colors.isDark) {
                            if (score >= 80) Color(0x3386F5D0) else Color(0x3349CBEB)
                        } else {
                            if (score >= 80) MintGreenLight else SkyBlueLight
                        }
                        val badgeText = if (score >= 80) MintGreenDark else Color(0xFF0284A5)

                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${req.title}: $score%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = badgeText
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next: ${idea.nextAction}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 1
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onEditClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Edit", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SkyBluePrimary)
                    }

                    if (showDelete) {
                        TextButton(
                            onClick = onDeleteClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Remove", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444))
                        }
                    }

                    Button(
                        onClick = onStartClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Push",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (colors.isDark) DarkCardBlack else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyIdeasState(
    isSearching: Boolean,
    onAddClick: () -> Unit
) {
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.Lightbulb,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.7f),
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = if (isSearching) "No matching ideas found" else "Your idea matrix is empty",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = if (isSearching) "Try another search term or filter" else "Map your first startup concept and score its readiness",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            if (!isSearching) {
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack
                    )
                ) {
                    Text("Add First Idea", color = if (colors.isDark) DarkCardBlack else Color.White)
                }
            }
        }
    }
}
