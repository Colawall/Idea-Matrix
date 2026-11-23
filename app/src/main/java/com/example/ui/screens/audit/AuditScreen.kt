package com.example.ui.screens.audit

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuditEntity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.viewmodel.IdeaViewModel

@Composable
fun AuditScreen(
    viewModel: IdeaViewModel,
    onBack: () -> Unit,
    preselectedIdeaId: Long? = null,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val allIdeas by viewModel.allIdeas.collectAsState()
    val allAudits by viewModel.allAudits.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Weekly") }
    var selectedIdeaId by remember { mutableLongStateOf(preselectedIdeaId ?: 0L) }
    var activeAudit by remember { mutableStateOf<AuditEntity?>(null) }
    var viewingPastAudit by remember { mutableStateOf<AuditEntity?>(null) }

    BackHandler(onBack = onBack)

    // Modal to view a past audit in full detail
    if (viewingPastAudit != null) {
        val audit = viewingPastAudit!!
        AlertDialog(
            onDismissRequest = { viewingPastAudit = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = SkyBluePrimary
                    )
                    Text(
                        text = "${audit.periodType} Audit: ${audit.ideaTitle}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Generated on ${audit.getFormattedDate()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }

                    item {
                        AuditReadinessCard(audit = audit)
                    }

                    item {
                        AuditTimelineProgressCard(audit = audit)
                    }

                    item {
                        AuditStepsCard(audit = audit)
                    }

                    item {
                        AuditBlockersCard(audit = audit)
                    }

                    item {
                        AuditRecommendationsCard(audit = audit)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewingPastAudit = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Close", color = DarkCardBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAudit(audit.id)
                        viewingPastAudit = null
                        Toast.makeText(context, "Audit deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444))
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("audit_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Idea Matrix Audit",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            ),
                            color = colors.textPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SkyBluePrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${allAudits.size} stored",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (colors.isDark) SkyBluePrimary else Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Controls Card: Period Selector + Idea Selector + Generate Button
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Generate Audit Report",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )

                        // Period Tabs [Weekly | Monthly]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Weekly", "Monthly").forEach { period ->
                                val isSelected = selectedPeriod == period
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedPeriod = period },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) SkyBluePrimary else colors.chipBg
                                ) {
                                    Text(
                                        text = "$period (Last ${if (period == "Weekly") "7" else "30"} days)",
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) DarkCardBlack else colors.textPrimary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Idea Filter Pills
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "AUDIT TARGET",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textSecondary
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // All ideas option
                                val isAllSelected = selectedIdeaId == 0L
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isAllSelected) MintGreenAccent else colors.chipBg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { selectedIdeaId = 0L }
                                ) {
                                    Text(
                                        text = "All Ideas (Portfolio)",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isAllSelected) DarkCardBlack else colors.textPrimary
                                    )
                                }

                                allIdeas.forEach { idea ->
                                    val isSelected = selectedIdeaId == idea.id
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isSelected) SkyBluePrimary else colors.chipBg,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable { selectedIdeaId = idea.id }
                                    ) {
                                        Text(
                                            text = idea.title,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) DarkCardBlack else colors.textPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Run Audit Action Button
                        Button(
                            onClick = {
                                viewModel.generateAudit(
                                    targetIdeaId = if (selectedIdeaId == 0L) null else selectedIdeaId,
                                    periodType = selectedPeriod
                                ) { generated ->
                                    activeAudit = generated
                                    Toast.makeText(context, "Audit generated and stored!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = MintGreenAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_run_audit")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = DarkCardBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Calculate & Save $selectedPeriod Audit",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    ),
                                    color = DarkCardBlack
                                )
                            }
                        }
                    }
                }
            }

            // Display Active Generated Audit Report
            val displayedAudit = activeAudit ?: allAudits.firstOrNull()
            if (displayedAudit != null) {
                item {
                    Text(
                        text = if (activeAudit != null) "LATEST AUDIT RESULTS" else "MOST RECENT SAVED AUDIT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SkyBluePrimary
                    )
                }

                item {
                    AuditReportDetailCard(audit = displayedAudit)
                }
            }

            // Past Audits History Section
            if (allAudits.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PAST AUDITS (${allAudits.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textSecondary
                        )
                        Text(
                            text = "Tap to view breakdown",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                items(allAudits) { pastAudit ->
                    PastAuditItem(
                        audit = pastAudit,
                        onClick = { viewingPastAudit = pastAudit },
                        onDelete = {
                            viewModel.deleteAudit(pastAudit.id)
                            if (activeAudit?.id == pastAudit.id) {
                                activeAudit = null
                            }
                            Toast.makeText(context, "Audit removed", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AuditReportDetailCard(
    audit: AuditEntity,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audit_report_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = audit.ideaTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${audit.periodType} Summary • ${audit.getFormattedDate()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (audit.readinessDelta >= 0) MintGreenAccent.copy(alpha = 0.2f) else Color(0x33EF4444)
                ) {
                    val deltaPrefix = if (audit.readinessDelta > 0) "+" else ""
                    Text(
                        text = "$deltaPrefix${audit.readinessDelta}% readiness",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (audit.readinessDelta >= 0) MintGreenDark else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Summary text
            if (audit.summaryNote.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.chipBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = audit.summaryNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Breakdown Cards
            AuditReadinessCard(audit = audit)
            AuditTimelineProgressCard(audit = audit)
            AuditStepsCard(audit = audit)
            AuditBlockersCard(audit = audit)
            AuditRecommendationsCard(audit = audit)
        }
    }
}

@Composable
fun AuditReadinessCard(audit: AuditEntity) {
    val colors = AppTheme.colors
    val delta = audit.readinessDelta
    val isPositive = delta >= 0

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.chipBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "READINESS CHANGE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary
                )
                Text(
                    text = "${audit.readinessStart}% → ${audit.readinessEnd}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (isPositive) MintGreenDark else Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${if (delta > 0) "+" else ""}$delta%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isPositive) MintGreenDark else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
fun AuditTimelineProgressCard(audit: AuditEntity) {
    val colors = AppTheme.colors
    val elapsedDays = (audit.timeElapsedMillis / (1000L * 60 * 60 * 24)).toInt()
    val elapsedHours = (audit.timeElapsedMillis / (1000L * 60 * 60)).toInt()
    val plannedDays = (audit.plannedDurationMillis / (1000L * 60 * 60 * 24)).toInt()
    val progressFloat = (audit.elapsedVsPlannedPercent / 100f).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.chipBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIME ELAPSED VS. PLANNED",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary
                )
                Text(
                    text = "${audit.elapsedVsPlannedPercent}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SkyBluePrimary
                )
            }

            LinearProgressIndicator(
                progress = { progressFloat },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = SkyBluePrimary,
                trackColor = if (colors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val elapsedLabel = if (elapsedDays > 0) "${elapsedDays}d active (${elapsedHours}h)" else "${elapsedHours}h active"
                Text(
                    text = "Elapsed: $elapsedLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                Text(
                    text = "Planned: ${plannedDays}d box",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun AuditStepsCard(audit: AuditEntity) {
    val colors = AppTheme.colors
    val completedSteps = audit.getCompletedStepsList()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.chipBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MintGreenAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "COMPLETED STEPS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textSecondary
                    )
                }

                Text(
                    text = "${audit.completedStepsCount} of ${audit.totalStepsCount} done",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MintGreenDark
                )
            }

            if (completedSteps.isNotEmpty()) {
                completedSteps.forEach { stepTitle ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MintGreenDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stepTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                }
            } else {
                Text(
                    text = "No execution steps completed in this period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun AuditBlockersCard(audit: AuditEntity) {
    val colors = AppTheme.colors
    val blockers = audit.getBlockersList()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.chipBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "DETECTED BLOCKERS & GAPS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary
                )
            }

            blockers.forEach { blocker ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = blocker,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AuditRecommendationsCard(audit: AuditEntity) {
    val colors = AppTheme.colors
    val recommendations = audit.getRecommendedStepsList()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.chipBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = SkyBluePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "RECOMMENDED NEXT STEPS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary
                )
            }

            recommendations.forEachIndexed { index, rec ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SkyBluePrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (colors.isDark) SkyBluePrimary else Color(0xFF0284C7)
                            )
                        }
                    }
                    Text(
                        text = rec,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun PastAuditItem(
    audit: AuditEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("past_audit_item_${audit.id}"),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBackground,
        border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = SkyBluePrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = audit.ideaTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${audit.periodType} • ${audit.getFormattedDate()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (audit.readinessDelta >= 0) MintGreenAccent.copy(alpha = 0.2f) else Color(0x33EF4444)
                ) {
                    val prefix = if (audit.readinessDelta > 0) "+" else ""
                    Text(
                        text = "$prefix${audit.readinessDelta}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (audit.readinessDelta >= 0) MintGreenDark else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete audit",
                        tint = colors.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
