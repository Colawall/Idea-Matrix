package com.example.ui.screens.ideas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.IdeaEntity
import com.example.data.model.CollaboratorConnection
import com.example.data.model.IdeaActivity
import com.example.data.model.IdeaMember
import com.example.data.model.IdeaRole
import com.example.data.model.UserProfile
import com.example.ui.components.MemberRoleBadge
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExecutionStatusCard(
    currentStatus: String,
    canChangeStatus: Boolean,
    isOwner: Boolean = false,
    isExecutor: Boolean = false,
    onStatusChange: (String) -> Unit
) {
    val colors = AppTheme.colors

    val statusOptions = listOf(
        Triple("Started", Icons.Default.RocketLaunch, "In development & execution"),
        Triple("Done", Icons.Default.CheckCircle, "Milestones satisfied"),
        Triple("Live", Icons.Default.Public, "Launched in the market")
    )

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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) Color(0x3386F5D0) else MintGreenAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = if (colors.isDark) MintGreenAccent else MintGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Execution Lifecycle",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.textPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isOwner) {
                                Icon(
                                    imageVector = Icons.Filled.WorkspacePremium,
                                    contentDescription = null,
                                    tint = if (colors.isDark) MintGreenAccent else MintGreenDark,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else if (isExecutor) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    tint = if (colors.isDark) MintGreenAccent else MintGreenDark,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = when {
                                    isOwner -> "Creator privileges: You can change lifecycle stages"
                                    isExecutor -> "Appointed Executor privileges: You can change lifecycle stages"
                                    canChangeStatus -> "Creator & appointed Executors can change status"
                                    else -> "View only (Creator & Appointed Executors lead)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (canChangeStatus) (if (colors.isDark) MintGreenAccent else MintGreenDark) else colors.textSecondary
                            )
                        }
                    }
                }

                // Current status pill
                val currentPillBg = when (currentStatus) {
                    "Live" -> Color(0x3386F5D0)
                    "Done" -> Color(0x3349CBEB)
                    "Started" -> Color(0x33F59E0B)
                    else -> if (colors.isDark) Color(0x22FFFFFF) else Color(0xFFF1F5F9)
                }
                val currentPillText = when (currentStatus) {
                    "Live" -> MintGreenDark
                    "Done" -> SkyBluePrimary
                    "Started" -> Color(0xFFD97706)
                    else -> colors.textSecondary
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = currentPillBg
                ) {
                    Text(
                        text = if (currentStatus.isNotBlank()) currentStatus else "Not Started",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = currentPillText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Status Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusOptions.forEach { (statusKey, statusIcon, _) ->
                    val isSelected = currentStatus.equals(statusKey, ignoreCase = true)
                    val activeBg = when (statusKey) {
                        "Live" -> MintGreenAccent
                        "Done" -> SkyBluePrimary
                        else -> Color(0xFFF59E0B)
                    }

                    Button(
                        onClick = { if (canChangeStatus) onStatusChange(statusKey) },
                        enabled = canChangeStatus,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) activeBg else (if (colors.isDark) DarkCardBlack else Color(0xFFF1F5F9)),
                            contentColor = if (isSelected) DarkCardBlack else colors.textPrimary,
                            disabledContainerColor = if (isSelected) activeBg.copy(alpha = 0.5f) else (if (colors.isDark) Color(0x15FFFFFF) else Color(0xFFF1F5F9)),
                            disabledContentColor = colors.textSecondary.copy(alpha = 0.6f)
                        ),
                        border = if (isSelected) null else BorderStroke(1.dp, if (colors.isDark) DarkBorder else Color(0xFFE2E8F0)),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
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

            // Reset option for authorized roles if in a lifecycle status
            if (canChangeStatus && currentStatus.isNotBlank() && currentStatus != "Not Started") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onStatusChange("Not Started") },
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Restricted",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Buttons are disabled for contributors. Only the Creator and appointed Executors can change execution status.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = colors.textSecondary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isOwner) Icons.Filled.WorkspacePremium else Icons.Filled.Bolt,
                        contentDescription = "Permitted",
                        tint = if (colors.isDark) MintGreenAccent else MintGreenDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isOwner) "You have Owner permissions to advance the execution lifecycle" else "You have Executor permissions to advance the execution lifecycle",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = if (colors.isDark) MintGreenAccent else MintGreenDark
                    )
                }
            }
        }
    }
}

@Composable
fun IdeaTeamCollaboratorsCard(
    idea: IdeaEntity,
    isOwner: Boolean,
    connections: List<CollaboratorConnection>,
    currentUserId: String,
    onInviteCollaborator: (collaborator: UserProfile, asExecutor: Boolean) -> Unit,
    onSetExecutor: (memberUid: String) -> Unit,
    onRemoveMember: (memberUid: String) -> Unit,
    onDemoteExecutor: ((memberUid: String) -> Unit)? = null
) {
    val colors = AppTheme.colors
    var showInviteDialog by remember { mutableStateOf(false) }

    val members = remember(idea.membersJson, idea.ownerId, idea.executorId) {
        val list = idea.getMembersList().toMutableList()
        if (list.isEmpty() && idea.ownerId.isNotBlank()) {
            list.add(
                IdeaMember(
                    uid = idea.ownerId,
                    displayName = idea.ownerName.ifBlank { "Owner" },
                    role = IdeaRole.OWNER
                )
            )
        }
        list
    }

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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Collaborators & Roles",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${members.size} team member${if (members.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (isOwner) {
                    Button(
                        onClick = { showInviteDialog = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) MintGreenAccent else MintGreenDark,
                            contentColor = if (colors.isDark) DarkCardBlack else Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Invite",
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Invite",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Member rows
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                members.forEach { member ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (colors.isDark) DarkCardBlack else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, if (colors.isDark) DarkBorder else Color(0xFFE2E8F0))
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Avatar circle
                                val initials = member.displayName.trim().split(Regex("\\s+"))
                                    .filter { it.isNotBlank() }
                                    .let { parts ->
                                        if (parts.size >= 2) "${parts[0].take(1).uppercase()}${parts[1].take(1).uppercase()}"
                                        else if (parts.isNotEmpty()) parts[0].take(2).uppercase()
                                        else "U"
                                    }

                                val avatarBg = when (member.role) {
                                    IdeaRole.OWNER -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                                    IdeaRole.EXECUTOR -> Brush.linearGradient(listOf(MintGreenAccent, Color(0xFF10B981)))
                                    else -> Brush.linearGradient(listOf(SkyBluePrimary, Color(0xFF0284A5)))
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(avatarBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (member.displayName.isNotBlank()) member.displayName else "Team Member",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary
                                        )
                                        if (member.uid == currentUserId) {
                                            Text(
                                                text = "(You)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SkyBluePrimary
                                            )
                                        }
                                    }

                                    MemberRoleBadge(role = member.role)
                                }
                            }

                            // Owner controls: Set as Executor or Remove
                            if (isOwner && member.role != IdeaRole.OWNER) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (member.role != IdeaRole.EXECUTOR) {
                                        TextButton(
                                            onClick = { onSetExecutor(member.uid) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Appoint Executor",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MintGreenAccent
                                            )
                                        }
                                    } else if (onDemoteExecutor != null) {
                                        TextButton(
                                            onClick = { onDemoteExecutor(member.uid) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Demote",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = SkyBluePrimary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onRemoveMember(member.uid) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove member",
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

    // Invite Collaborator Dialog
    if (showInviteDialog) {
        var selectedConnection by remember { mutableStateOf<CollaboratorConnection?>(connections.firstOrNull()) }
        var asExecutor by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = {
                Text(
                    text = "Invite Connected Teammate",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (connections.isEmpty()) {
                        Text(
                            text = "You don't have any connected collaborators yet. Go to the Profile tab to share your unique code or connect with teammates.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    } else {
                        Text(
                            text = "Select a teammate to join this idea:",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            connections.forEach { conn ->
                                val targetName = if (conn.senderId == currentUserId) conn.receiverName else conn.senderName
                                val targetId = if (conn.senderId == currentUserId) conn.receiverId else conn.senderId
                                val targetPhoto = if (conn.senderId == currentUserId) conn.receiverPhoto else conn.senderPhoto
                                val isSelected = selectedConnection?.id == conn.id

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedConnection = conn },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) (if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight) else (if (colors.isDark) DarkCardBlack else Color(0xFFF1F5F9)),
                                    border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else (if (colors.isDark) DarkBorder else Color(0xFFE2E8F0)))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(SkyBluePrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = targetName.take(1).uppercase(),
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Text(
                                            text = targetName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = SkyBluePrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Role selection
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Assign Role:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { asExecutor = false }
                            ) {
                                RadioButton(
                                    selected = !asExecutor,
                                    onClick = { asExecutor = false },
                                    colors = RadioButtonDefaults.colors(selectedColor = SkyBluePrimary)
                                )
                                Column {
                                    Text(
                                        text = "Contributor",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Can add/edit requirements, steps, notes, and comment",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { asExecutor = true }
                            ) {
                                RadioButton(
                                    selected = asExecutor,
                                    onClick = { asExecutor = true },
                                    colors = RadioButtonDefaults.colors(selectedColor = MintGreenAccent)
                                )
                                Column {
                                    Text(
                                        text = "Executor (Lead)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Lead executor; can also change execution lifecycle status",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (connections.isNotEmpty()) {
                    Button(
                        onClick = {
                            val conn = selectedConnection ?: return@Button
                            val targetId = if (conn.senderId == currentUserId) conn.receiverId else conn.senderId
                            val targetName = if (conn.senderId == currentUserId) conn.receiverName else conn.senderName
                            val targetPhoto = if (conn.senderId == currentUserId) conn.receiverPhoto else conn.senderPhoto

                            val user = UserProfile(
                                uid = targetId,
                                displayName = targetName,
                                photoUrl = targetPhoto
                            )
                            onInviteCollaborator(user, asExecutor)
                            showInviteDialog = false
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack,
                            contentColor = if (colors.isDark) DarkCardBlack else Color.White
                        )
                    ) {
                        Text("Send Invite")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
fun IdeaActivityFeedCard(
    activities: List<IdeaActivity>,
    onPostComment: (String) -> Unit
) {
    val colors = AppTheme.colors
    var commentText by remember { mutableStateOf("") }

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
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0x33F59E0B) else Color(0x22F59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Activity Feed & Discussion",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Real-time updates and team notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            // Comment input box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Write a comment or note...", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBluePrimary,
                        unfocusedBorderColor = if (colors.isDark) DarkBorder else Color(0xFFE2E8F0)
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        val clean = commentText.trim()
                        if (clean.isNotBlank()) {
                            onPostComment(clean)
                            commentText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) SkyBluePrimary else DarkCardBlack)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Comment",
                        tint = if (colors.isDark) DarkCardBlack else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Timeline items
            if (activities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No activities or comments yet. Post the first update!",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

                    activities.forEach { activity ->
                        val (icon, iconTint, bg) = when (activity.type) {
                            "status_change" -> Triple(Icons.Default.RocketLaunch, MintGreenDark, if (colors.isDark) Color(0x3386F5D0) else MintGreenAccent.copy(alpha = 0.2f))
                            "executor_assigned" -> Triple(Icons.Default.Bolt, Color(0xFFF59E0B), if (colors.isDark) Color(0x33F59E0B) else Color(0x22F59E0B))
                            "member_joined" -> Triple(Icons.Default.PersonAdd, SkyBluePrimary, if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight)
                            else -> Triple(Icons.Default.Comment, colors.textPrimary, if (colors.isDark) DarkCardBlack else Color(0xFFF1F5F9))
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (colors.isDark) DarkCardBlack else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (colors.isDark) DarkBorder else Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconTint,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (activity.userName.isNotBlank()) activity.userName else "Collaborator",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary
                                        )

                                        Text(
                                            text = dateFormat.format(Date(activity.timestamp)),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = colors.textSecondary
                                        )
                                    }

                                    Text(
                                        text = activity.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
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
