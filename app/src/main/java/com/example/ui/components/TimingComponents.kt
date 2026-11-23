package com.example.ui.components

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IdeaEntity
import com.example.data.model.ReminderConfig
import com.example.data.model.ReminderFrequency
import com.example.data.model.TimingStatus
import com.example.data.model.WorkSessionLog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.viewmodel.IdeaViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DaysRemainingChip(
    idea: IdeaEntity,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val timingStatus = idea.getTimingStatusEnum()
    val daysRemaining = idea.getDaysRemaining()
    val isExpired = idea.isTimeExpired()

    val (chipText, chipIcon, bgTint, textTint) = when {
        timingStatus == TimingStatus.DONE -> {
            Quad("Done", Icons.Default.CheckCircle, MintGreenAccent.copy(alpha = 0.2f), MintGreenDark)
        }
        isExpired -> {
            val label = if (daysRemaining != null && daysRemaining < 0) "${-daysRemaining}d overdue" else "Time Expired"
            Quad(label, Icons.Default.Warning, Color(0x33EF4444), Color(0xFFEF4444))
        }
        timingStatus == TimingStatus.PAUSED -> {
            val label = if (daysRemaining != null) "Paused • ${daysRemaining}d left" else "Paused"
            Quad(label, Icons.Default.Pause, Color(0x22F59E0B), Color(0xFFF59E0B))
        }
        timingStatus == TimingStatus.ACTIVE -> {
            if (daysRemaining != null) {
                if (daysRemaining <= 5) {
                    Quad("${daysRemaining}d left", Icons.Default.HourglassBottom, Color(0x33F97316), Color(0xFFF97316))
                } else {
                    Quad("${daysRemaining}d left", Icons.Default.Schedule, SkyBluePrimary.copy(alpha = 0.2f), if (colors.isDark) SkyBluePrimary else Color(0xFF0284C7))
                }
            } else {
                Quad("Active Clock", Icons.Default.PlayArrow, SkyBluePrimary.copy(alpha = 0.2f), if (colors.isDark) SkyBluePrimary else Color(0xFF0284C7))
            }
        }
        else -> {
            // Planning
            if (idea.timeBoxDays != null) {
                Quad("Plan: ${idea.timeBoxDays}d box", Icons.Default.HourglassEmpty, colors.chipBg, colors.textSecondary)
            } else {
                Quad("Planning", Icons.Default.HourglassEmpty, colors.chipBg, colors.textSecondary)
            }
        }
    }

    Surface(
        modifier = modifier.testTag("days_remaining_chip_${idea.id}"),
        shape = RoundedCornerShape(50),
        color = bgTint,
        border = if (colors.isDark) BorderStroke(1.dp, textTint.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = chipIcon,
                contentDescription = null,
                tint = textTint,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = chipText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = textTint
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun TimeExpiredDialog(
    idea: IdeaEntity,
    onExtend: (days: Int) -> Unit,
    onPause: () -> Unit,
    onShelve: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var extendDays by remember { mutableIntStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = Color(0xFFEF4444)
                )
                Text(
                    text = "Time Box Expired",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "The planned time box for \"${idea.title}\" has run out! How would you like to handle this idea?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                Text(
                    text = "Choose an action:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )

                // Quick Extend options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(14, 30, 60).forEach { days ->
                        val isSelected = extendDays == days
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SkyBluePrimary else colors.chipBg,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { extendDays = days }
                        ) {
                            Text(
                                text = "+$days days",
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) DarkCardBlack else colors.textPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onExtend(extendDays) },
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("btn_extend_time")
            ) {
                Text("Extend +${extendDays}d", color = DarkCardBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onPause,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("btn_pause_expired")
                ) {
                    Text("Pause", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick = onShelve,
                    modifier = Modifier.testTag("btn_shelve_expired")
                ) {
                    Text("Shelve", color = colors.textSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        containerColor = colors.cardBackground,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun TimelineSection(
    idea: IdeaEntity,
    viewModel: IdeaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    var showExpiredDialog by remember { mutableStateOf(false) }

    val timingStatus = idea.getTimingStatusEnum()
    val isExpired = idea.isTimeExpired()
    val elapsedMillis = idea.getTotalActiveElapsedMillis()
    val plannedMillis = idea.getPlannedDurationMillis()
    val progress = idea.getElapsedVsPlannedProgress()
    val daysRemaining = idea.getDaysRemaining()

    val elapsedHours = (elapsedMillis / (1000L * 60 * 60)).toInt()
    val elapsedDays = (elapsedMillis / (1000L * 60 * 60 * 24)).toInt()

    if (showExpiredDialog) {
        TimeExpiredDialog(
            idea = idea,
            onExtend = { days ->
                viewModel.extendIdeaTime(idea, days)
                showExpiredDialog = false
                Toast.makeText(context, "Extended time by $days days!", Toast.LENGTH_SHORT).show()
            },
            onPause = {
                viewModel.pauseIdeaTimingClock(idea)
                showExpiredDialog = false
                Toast.makeText(context, "Idea clock paused.", Toast.LENGTH_SHORT).show()
            },
            onShelve = {
                viewModel.completeIdeaTimingClock(idea)
                showExpiredDialog = false
                Toast.makeText(context, "Idea shelved/completed.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showExpiredDialog = false }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_section_card"),
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
            // Header: Title & Status Chip
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
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Timeline & Clock",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }

                DaysRemainingChip(idea = idea)
            }

            // Status Selector: Planning | Active | Paused | Done
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "LIFECYCLE STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimingStatus.entries.forEach { status ->
                        val isSelected = timingStatus == status
                        val statusBg = when (status) {
                            TimingStatus.ACTIVE -> if (isSelected) SkyBluePrimary else colors.chipBg
                            TimingStatus.PAUSED -> if (isSelected) Color(0xFFF59E0B) else colors.chipBg
                            TimingStatus.DONE -> if (isSelected) MintGreenAccent else colors.chipBg
                            TimingStatus.PLANNING -> if (isSelected) (if (colors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)) else colors.chipBg
                        }
                        val statusText = if (isSelected) DarkCardBlack else colors.textPrimary

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    when (status) {
                                        TimingStatus.ACTIVE -> viewModel.startIdeaTimingClock(idea)
                                        TimingStatus.PAUSED -> viewModel.pauseIdeaTimingClock(idea)
                                        TimingStatus.DONE -> viewModel.completeIdeaTimingClock(idea)
                                        TimingStatus.PLANNING -> viewModel.setIdeaPlanningStatus(idea)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = statusBg
                        ) {
                            Text(
                                text = status.label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = statusText,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Time Box Picker (30, 60, 90 days or Custom Target Date)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PLANNED TIME BOX",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30, 60, 90).forEach { days ->
                        val isSelected = idea.timeBoxDays == days && idea.targetDateMillis == null
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) SkyBluePrimary else colors.chipBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    viewModel.updateTimeBox(idea, days, null)
                                    Toast.makeText(context, "$days-day box set", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Text(
                                text = "$days Days",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) DarkCardBlack else colors.textPrimary
                            )
                        }
                    }

                    // 120 Days / Custom Option
                    val isCustom = idea.targetDateMillis != null || (idea.timeBoxDays != null && idea.timeBoxDays !in listOf(30, 60, 90))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isCustom) SkyBluePrimary else colors.chipBg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                // Default custom target: 120 days
                                viewModel.updateTimeBox(idea, 120, null)
                                Toast.makeText(context, "120-day box set", Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Text(
                            text = if (isCustom) "${idea.timeBoxDays ?: 120}d Custom" else "Custom Date",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isCustom) DarkCardBlack else colors.textPrimary
                        )
                    }
                }
            }

            // Elapsed vs Planned Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Elapsed vs. Planned Progress",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isExpired) Color(0xFFEF4444) else SkyBluePrimary
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (isExpired) Color(0xFFEF4444) else (if (progress >= 0.85f) Color(0xFFF59E0B) else SkyBluePrimary),
                    trackColor = if (colors.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeLabel = if (elapsedDays > 0) "${elapsedDays}d active (${elapsedHours}h)" else "${elapsedHours}h elapsed"
                    val plannedLabel = if (plannedMillis != null) "${(plannedMillis / (1000L * 60 * 60 * 24)).toInt()}d planned" else "No time box"

                    Text(
                        text = activeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = plannedLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            // Time Expired Alert Banner
            if (isExpired) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x22EF4444),
                    border = BorderStroke(1.dp, Color(0x66EF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Deadline reached!",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFEF4444)
                                )
                                Text(
                                    text = "Extend time box, pause, or shelve this idea.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Button(
                            onClick = { showExpiredDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Resolve", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkSessionTimerSection(
    idea: IdeaEntity,
    viewModel: IdeaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val activeIdeaId by viewModel.activeSessionIdeaId.collectAsState()
    val activeElapsedSeconds by viewModel.activeSessionElapsedSeconds.collectAsState()
    val isTimingCurrentIdea = activeIdeaId == idea.id

    var sessionNote by remember { mutableStateOf("") }
    val sessionLogs = remember(idea.sessionLogsJson) { idea.getSessionLogs() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("work_session_timer_card"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MintGreenAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Work Session Log",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }

                Text(
                    text = "${sessionLogs.size} logs",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary
                )
            }

            // Live Timer Area
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isTimingCurrentIdea) MintGreenAccent.copy(alpha = 0.12f) else colors.chipBg,
                border = if (isTimingCurrentIdea) BorderStroke(1.5.dp, MintGreenAccent) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val minutes = (activeElapsedSeconds / 60)
                    val seconds = (activeElapsedSeconds % 60)
                    val hours = (minutes / 60)
                    val formattedTime = if (hours > 0) {
                        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds)
                    } else {
                        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                    }

                    Text(
                        text = if (isTimingCurrentIdea) formattedTime else "00:00",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            letterSpacing = 2.sp
                        ),
                        color = if (isTimingCurrentIdea) MintGreenDark else colors.textPrimary
                    )

                    if (isTimingCurrentIdea) {
                        OutlinedTextField(
                            value = sessionNote,
                            onValueChange = { sessionNote = it },
                            placeholder = { Text("What did you focus on in this session?") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isTimingCurrentIdea) {
                            Button(
                                onClick = {
                                    viewModel.startWorkSession(idea.id)
                                    // Also transition idea to Active clock if in planning
                                    if (idea.getTimingStatusEnum() == TimingStatus.PLANNING || idea.getTimingStatusEnum() == TimingStatus.PAUSED) {
                                        viewModel.startIdeaTimingClock(idea)
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreenAccent),
                                modifier = Modifier.testTag("btn_start_work_session")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = DarkCardBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("Start Session", color = DarkCardBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.stopWorkSession(notes = sessionNote) {
                                        sessionNote = ""
                                        Toast.makeText(context, "Session saved!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = MintGreenAccent),
                                modifier = Modifier.testTag("btn_stop_work_session")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = null,
                                        tint = DarkCardBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("Stop & Log", color = DarkCardBlack, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.cancelWorkSession()
                                    sessionNote = ""
                                },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Discard", color = colors.textSecondary)
                            }
                        }
                    }
                }
            }

            // Past Session History List
            if (sessionLogs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RECENT SESSIONS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textSecondary
                    )

                    sessionLogs.take(5).forEach { session ->
                        val durationMinutes = (session.durationMillis / (1000L * 60)).coerceAtLeast(1)
                        val dateFormatted = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(session.startTimeMillis))

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.chipBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (session.notes.isNotBlank()) session.notes else "Focused work sprint",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = dateFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MintGreenAccent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${durationMinutes}m",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MintGreenDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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

@Composable
fun ReminderSettingsSection(
    idea: IdeaEntity,
    viewModel: IdeaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val currentConfig = remember(idea.reminderConfigJson) { idea.getReminderConfig() }

    var isEnabled by remember(currentConfig) { mutableStateOf(currentConfig.enabled) }
    var frequency by remember(currentConfig) { mutableStateOf(currentConfig.frequency) }
    var hour by remember(currentConfig) { mutableIntStateOf(currentConfig.timeHour) }
    var minute by remember(currentConfig) { mutableIntStateOf(currentConfig.timeMinute) }
    var quietStart by remember(currentConfig) { mutableIntStateOf(currentConfig.quietHoursStartHour) }
    var quietEnd by remember(currentConfig) { mutableIntStateOf(currentConfig.quietHoursEndHour) }
    val customDays = remember(currentConfig) { mutableStateListOf<Int>().apply { addAll(currentConfig.customDaysOfWeek) } }

    // On-demand notification permission launcher (asked ONLY when user enables reminders)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val updated = currentConfig.copy(
                enabled = true,
                frequency = frequency,
                timeHour = hour,
                timeMinute = minute,
                quietHoursStartHour = quietStart,
                quietHoursEndHour = quietEnd,
                customDaysOfWeek = customDays.toList()
            )
            viewModel.updateReminderSettings(idea, updated)
            isEnabled = true
            Toast.makeText(context, "Reminders scheduled!", Toast.LENGTH_SHORT).show()
        } else {
            isEnabled = false
            Toast.makeText(context, "Notification permission needed for reminders.", Toast.LENGTH_SHORT).show()
        }
    }

    fun applyConfig(newEnabled: Boolean) {
        val updated = currentConfig.copy(
            enabled = newEnabled,
            frequency = frequency,
            timeHour = hour,
            timeMinute = minute,
            quietHoursStartHour = quietStart,
            quietHoursEndHour = quietEnd,
            customDaysOfWeek = customDays.toList()
        )
        viewModel.updateReminderSettings(idea, updated)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reminder_settings_card"),
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
            // Header with toggle
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
                        imageVector = if (isEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = if (isEnabled) SkyBluePrimary else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Smart Reminders",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Targets largest gap requirement",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                isEnabled = true
                                applyConfig(true)
                                Toast.makeText(context, "Reminders enabled!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            isEnabled = false
                            applyConfig(false)
                            Toast.makeText(context, "Reminders paused.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkCardBlack,
                        checkedTrackColor = SkyBluePrimary
                    ),
                    modifier = Modifier.testTag("switch_enable_reminders")
                )
            }

            // 5-Dismissal reduction offer banner
            if (currentConfig.consecutiveDismissals >= 5) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x22F59E0B),
                    border = BorderStroke(1.dp, Color(0x66F59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Notice: 5 consecutive dismissals",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF59E0B)
                            )
                        }
                        Text(
                            text = "You've dismissed 5 reminders in a row. Would you like to reduce cadence to Weekly?",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.reduceReminderFrequency(idea)
                                    frequency = ReminderFrequency.WEEKLY
                                    Toast.makeText(context, "Reduced frequency to Weekly!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Switch to Weekly", color = DarkCardBlack, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            TextButton(
                                onClick = {
                                    viewModel.resetDismissalCount(idea)
                                }
                            ) {
                                Text("Keep Schedule", color = colors.textSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Frequency selector: Daily, Weekly, Custom
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "FREQUENCY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderFrequency.entries.forEach { freq ->
                                val isSelected = frequency == freq
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            frequency = freq
                                            applyConfig(true)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) SkyBluePrimary else colors.chipBg
                                ) {
                                    Text(
                                        text = freq.label,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) DarkCardBlack else colors.textPrimary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Custom days selector if CUSTOM selected
                    if (frequency == ReminderFrequency.CUSTOM) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CUSTOM DAYS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textSecondary
                            )

                            val daysMap = listOf(
                                "M" to Calendar.MONDAY,
                                "T" to Calendar.TUESDAY,
                                "W" to Calendar.WEDNESDAY,
                                "T" to Calendar.THURSDAY,
                                "F" to Calendar.FRIDAY,
                                "S" to Calendar.SATURDAY,
                                "S" to Calendar.SUNDAY
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                daysMap.forEach { (label, dayConst) ->
                                    val isDaySelected = customDays.contains(dayConst)
                                    Surface(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (isDaySelected) customDays.remove(dayConst) else customDays.add(dayConst)
                                                applyConfig(true)
                                            },
                                        shape = CircleShape,
                                        color = if (isDaySelected) SkyBluePrimary else colors.chipBg
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isDaySelected) DarkCardBlack else colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Time of Day selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TIME OF DAY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textSecondary
                            )
                            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                        }

                        // Preset Time Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(Pair(9, 0) to "9 AM", Pair(13, 0) to "1 PM", Pair(18, 0) to "6 PM").forEach { (time, label) ->
                                val isSelected = hour == time.first && minute == time.second
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) SkyBluePrimary else colors.chipBg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            hour = time.first
                                            minute = time.second
                                            applyConfig(true)
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) DarkCardBlack else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Quiet Hours setting
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
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "QUIET HOURS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "${quietStart}:00 – ${quietEnd}:00",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(Pair(22, 8) to "10PM-8AM", Pair(23, 9) to "11PM-9AM").forEach { (range, label) ->
                                val isSelected = quietStart == range.first && quietEnd == range.second
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) Color(0xFFA78BFA) else colors.chipBg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            quietStart = range.first
                                            quietEnd = range.second
                                            applyConfig(true)
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) DarkCardBlack else colors.textPrimary
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
