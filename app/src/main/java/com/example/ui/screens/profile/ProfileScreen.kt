package com.example.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import com.example.data.model.CollaboratorConnection
import com.example.data.model.ShareCodeUtils
import com.example.data.model.UserProfile
import com.example.ui.components.QrCodeView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ReadinessLineChart
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePill
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.viewmodel.IdeaViewModel
import com.example.ui.viewmodel.ThemeMode

@Composable
fun ProfileScreen(
    viewModel: IdeaViewModel,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val checkIns by viewModel.allCheckIns.collectAsState()
    val allIdeas by viewModel.allIdeas.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val shouldShowDailyFeelingButtons by viewModel.shouldShowDailyFeelingButtons.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val uid = currentUser?.uid ?: ""

    val incomingRequests = connections.filter { it.status == "pending" && it.receiverId == uid }
    val acceptedConnections = connections.filter { it.status == "accepted" }

    val totalIdeas = allIdeas.size
    val avgReadiness = if (allIdeas.isNotEmpty()) {
        (allIdeas.map { it.readinessScore }.average()).toInt()
    } else 0
    val maxReadiness = allIdeas.maxOfOrNull { it.readinessScore } ?: 0

    val quotes = listOf(
        "Ideas are cheap. Execution is everything. Pick your highest readiness bet and ship today.",
        "Momentum is created when preparation meets clarity. Focus on what you can build this week.",
        "The fastest way to de-risk an idea is talking directly to the person who has the pain point.",
        "Don't build everything upfront. Satisfy your primary requirement bottleneck first."
    )
    var quoteIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("profile_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Profile Header
            item {
                ProfileHeaderSection(
                    userName = if (!userProfile?.displayName.isNullOrBlank()) userProfile!!.displayName else userName,
                    onUpdateName = viewModel::updateUserName
                )
            }

            // Collaborator Code, Copy, QR and Connect
            item {
                CollaboratorCodeCard(
                    userProfile = userProfile,
                    onSendRequest = { code, onDone ->
                        viewModel.sendConnectionRequest(code, onDone)
                    }
                )
            }

            // Pending Collaborator Requests Received
            if (incomingRequests.isNotEmpty()) {
                item {
                    CollaboratorRequestsSection(
                        requests = incomingRequests,
                        onAccept = { conn -> viewModel.acceptConnection(conn) },
                        onDecline = { connId -> viewModel.declineConnection(connId) }
                    )
                }
            }

            // Connected Collaborators
            if (acceptedConnections.isNotEmpty()) {
                item {
                    ConnectedCollaboratorsCard(
                        currentUserId = uid,
                        connections = acceptedConnections
                    )
                }
            }

            // Daily Check-in Status Card
            item {
                DailyCheckInStatusCard(
                    isCompletedToday = !shouldShowDailyFeelingButtons,
                    onResetCheckIn = viewModel::resetTodayCheckIn
                )
            }

            // Appearance & Dark Mode Card
            item {
                AppearanceSettingsCard(
                    themeMode = themeMode,
                    onThemeModeChange = viewModel::setThemeMode
                )
            }

            // Stats Matrix Row
            item {
                FounderStatsRow(
                    totalIdeas = totalIdeas,
                    avgReadiness = avgReadiness,
                    topScore = maxReadiness
                )
            }

            // Readiness Over Time Line Chart Card
            item {
                ReadinessChartCard(checkIns = checkIns)
            }

            // Dark Daily-Reminder Quote Card
            item {
                DarkQuoteCard(
                    quote = quotes[quoteIndex],
                    onNextQuote = {
                        quoteIndex = (quoteIndex + 1) % quotes.size
                    }
                )
            }

            // Founder Principles Card
            item {
                FounderPrinciplesCard()
            }

            // Google Account & Sign Out
            item {
                AccountActionsCard(
                    userProfile = userProfile,
                    onSignOut = viewModel::signOut,
                    onSignInRequested = { viewModel.setGuestMode(false) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(
    userName: String,
    onUpdateName: (String) -> Unit
) {
    val colors = AppTheme.colors
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }

    val initials = remember(userName) {
        val parts = userName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].take(1).uppercase()}${parts[1].take(1).uppercase()}"
        } else if (parts.isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else {
            "SK"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable {
                    editedName = userName
                    showEditDialog = true
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(SkyBluePrimary, MintGreenAccent)
                        )
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(DarkCardBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                }
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit name",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Solo Founder & Product Builder",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        // Streak badge pill
        Box(
            modifier = Modifier
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(50))
                .background(MintGreenAccent, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = DarkCardBlack,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "14d Streak",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = DarkCardBlack
                )
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Edit Profile Name",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your first name is used in the greeting on the Home screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.cardBackground,
                            unfocusedContainerColor = colors.cardBackground,
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            onUpdateName(editedName)
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Save", color = DarkCardBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBackground
        )
    }
}

@Composable
fun AppearanceSettingsCard(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("appearance_card")
            .shadow(
                elevation = if (colors.isDark) 2.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x12000000)
            ),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = if (colors.isDark) SkyBluePrimary else Color(0xFF0369A1),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Appearance & Theme",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Toggle light or dark interface styling",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                // Switch for quick dark toggle
                Switch(
                    checked = colors.isDark,
                    onCheckedChange = { isChecked ->
                        onThemeModeChange(if (isChecked) ThemeMode.DARK else ThemeMode.LIGHT)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SkyBluePrimary,
                        uncheckedThumbColor = colors.textSecondary,
                        uncheckedTrackColor = colors.chipBg
                    )
                )
            }

            // 3-Option Segmented Pill Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.chipBg, RoundedCornerShape(50))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val options = listOf(
                    Triple(ThemeMode.SYSTEM, "System", Icons.Default.BrightnessAuto),
                    Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                    Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode)
                )

                options.forEach { (mode, label, icon) ->
                    val isSelected = themeMode == mode
                    val itemBg = if (isSelected) {
                        if (colors.isDark) SkyBluePrimary else DarkCardBlack
                    } else {
                        Color.Transparent
                    }
                    val itemTextColor = if (isSelected) {
                        if (colors.isDark) DarkCardBlack else Color.White
                    } else {
                        colors.textSecondary
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .clickable { onThemeModeChange(mode) },
                        shape = RoundedCornerShape(50),
                        color = itemBg
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = itemTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = itemTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FounderStatsRow(
    totalIdeas: Int,
    avgReadiness: Int,
    topScore: Int
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(
            label = "Total Ideas",
            value = totalIdeas.toString(),
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        StatBox(
            label = "Avg Readiness",
            value = "$avgReadiness%",
            color = if (colors.isDark) SkyBluePrimary else Color(0xFF0284A5),
            modifier = Modifier.weight(1f)
        )
        StatBox(
            label = "Top Readiness",
            value = "$topScore%",
            color = MintGreenDark,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier.shadow(
            elevation = if (colors.isDark) 2.dp else 3.dp,
            shape = RoundedCornerShape(20.dp),
            spotColor = Color(0x10000000)
        ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = colors.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ReadinessChartCard(
    checkIns: List<com.example.data.local.DailyCheckInEntity>,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("readiness_chart_card")
            .shadow(
                elevation = if (colors.isDark) 2.dp else 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x12000000)
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
                Column {
                    Text(
                        text = "Readiness Over Time",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Daily check-in sentiment & execution readiness",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "7 Days",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (colors.isDark) SkyBluePrimary else Color(0xFF0369A1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Line Chart with gradient fill and day markers
            ReadinessLineChart(
                checkIns = checkIns,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DarkQuoteCard(
    quote: String,
    onNextQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dark_quote_card")
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x33000000)
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
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = MintGreenAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "DAILY REMINDER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = MintGreenAccent
                    )
                }

                IconButton(
                    onClick = onNextQuote,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "New quote",
                        tint = TextWhiteMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "“$quote”",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    lineHeight = 25.sp
                ),
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— Idea Matrix Daily Spark",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextWhiteMuted
                )

                Box(
                    modifier = Modifier
                        .background(Color(0x3386F5D0), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Mindset",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MintGreenAccent
                    )
                }
            }
        }
    }
}

@Composable
fun FounderPrinciplesCard(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = if (colors.isDark) 2.dp else 3.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Execution Framework",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary
            )
            Text(
                text = "Idea Matrix scores are weighted based on your bottlenecks: Money, Skills, Time, Tools, People, and Knowledge. A score above 75% indicates immediate validation readiness.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun DailyCheckInStatusCard(
    isCompletedToday: Boolean,
    onResetCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("checkin_status_card")
            .shadow(elevation = if (colors.isDark) 2.dp else 3.dp, shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Daily Readiness Check-in",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = colors.textPrimary
                )
                Text(
                    text = if (isCompletedToday) "Completed for today (timezone tracked)" else "Awaiting response on Home screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Button(
                onClick = onResetCheckIn,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack,
                    contentColor = if (colors.isDark) DarkCardBlack else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isCompletedToday) "Retake" else "Test Float",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun CollaboratorCodeCard(
    userProfile: UserProfile?,
    onSendRequest: (String, (Result<String>) -> Unit) -> Unit
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val code = userProfile?.shareCode?.ifBlank { "IM-XXXXXX" } ?: "IM-XXXXXX"
    var showQrCode by remember { mutableStateOf(false) }
    var showConnectDialog by remember { mutableStateOf(false) }
    var inputCode by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 2.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x14000000)
            )
            .testTag("collaborator_code_card"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Collaborator Code",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Share to connect with co-founders & executors",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Big Prominent Share Code Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (colors.isDark) DarkCardBlack else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, if (colors.isDark) DarkBorder else Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YOUR UNIQUE CODE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            ),
                            color = SkyBluePrimary
                        )
                        Text(
                            text = code,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 24.sp,
                                letterSpacing = 3.sp
                            ),
                            color = colors.textPrimary
                        )
                    }

                    // Copy Button
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Idea Matrix Code", code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied code $code to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack,
                            contentColor = if (colors.isDark) DarkCardBlack else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Code",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Copy",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Action row: QR Code toggle & Connect button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showQrCode = !showQrCode },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (colors.isDark) Color(0x2E49CBEB) else SkyBlueLight,
                        contentColor = SkyBluePrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (showQrCode) "Hide QR" else "Show QR",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Button(
                    onClick = { showConnectDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (colors.isDark) MintGreenAccent else MintGreenDark,
                        contentColor = if (colors.isDark) DarkCardBlack else Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = "Connect",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Connect",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Expandable QR Code View
            if (showQrCode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QrCodeView(
                        content = code,
                        size = 180.dp
                    )
                    Text(
                        text = "Scan with Idea Matrix to connect",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }

    // Connect Collaborator Dialog
    if (showConnectDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSending) showConnectDialog = false },
            title = {
                Text(
                    text = "Connect with Collaborator",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter your teammate's 9-character code (format: IM-XXXXXX) to send a connection request:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it.uppercase() },
                        label = { Text("Code") },
                        placeholder = { Text("e.g. IM-8K2YZ5") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = colors.cardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = inputCode.trim()
                        if (!clean.startsWith("IM-") || clean.length != 9) {
                            Toast.makeText(context, "Code must be in format IM-XXXXXX", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSending = true
                        onSendRequest(clean) { res ->
                            isSending = false
                            res.onSuccess { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                showConnectDialog = false
                                inputCode = ""
                            }.onFailure { err ->
                                Toast.makeText(context, err.message ?: "Failed to connect", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyBluePrimary,
                        contentColor = DarkCardBlack
                    ),
                    enabled = !isSending && inputCode.isNotBlank()
                ) {
                    Text(
                        text = if (isSending) "Sending..." else "Send Request",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConnectDialog = false },
                    enabled = !isSending
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CollaboratorRequestsSection(
    requests: List<CollaboratorConnection>,
    onAccept: (CollaboratorConnection) -> Unit,
    onDecline: (String) -> Unit
) {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 2.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x14000000)
            ),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0x33F59E0B) else Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Pending Requests (${requests.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Accept to start collaborating on ideas",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            requests.forEach { req ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (colors.isDark) DarkCardBlack else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (colors.isDark) DarkBorder else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SkyBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = req.senderName.take(2).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkCardBlack
                                )
                            }
                            Column {
                                Text(
                                    text = req.senderName.ifBlank { "Collaborator" },
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Wants to connect",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { onAccept(req) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (colors.isDark) MintGreenAccent else MintGreenDark)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Accept",
                                    tint = if (colors.isDark) DarkCardBlack else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDecline(req.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (colors.isDark) Color(0x33EF4444) else Color(0xFFFEE2E2))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Decline",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectedCollaboratorsCard(
    currentUserId: String,
    connections: List<CollaboratorConnection>
) {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 2.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x14000000)
            ),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0x3386F5D0) else Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = if (colors.isDark) MintGreenAccent else MintGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Connected Collaborators (${connections.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Can be invited into any of your ideas",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            connections.forEach { conn ->
                val otherName = if (conn.senderId == currentUserId) conn.receiverName else conn.senderName
                val otherEmail = if (conn.senderId == currentUserId) conn.receiverEmail else conn.senderEmail

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (colors.isDark) DarkCardBlack else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (colors.isDark) DarkBorder else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(SkyBluePrimary, MintGreenAccent)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = otherName.take(2).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkCardBlack
                                )
                            }
                            Column {
                                Text(
                                    text = otherName.ifBlank { "Teammate" },
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                if (otherEmail.isNotBlank()) {
                                    Text(
                                        text = otherEmail,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(if (colors.isDark) Color(0x3386F5D0) else Color(0xFFD1FAE5), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Connected",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (colors.isDark) MintGreenAccent else MintGreenDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountActionsCard(
    userProfile: UserProfile?,
    onSignOut: () -> Unit,
    onSignInRequested: () -> Unit = {}
) {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (colors.isDark) 2.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x14000000)
            ),
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
                if (userProfile == null) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Local-Only Mode",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Sign in to sync ideas and invite collaborators.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    Button(
                        onClick = onSignInRequested,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SkyBluePrimary,
                            contentColor = DarkCardBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Google Account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = userProfile.email.ifBlank { "Signed in with Google" },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    Button(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) Color(0x33EF4444) else Color(0xFFFEE2E2),
                            contentColor = Color(0xFFEF4444)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}


