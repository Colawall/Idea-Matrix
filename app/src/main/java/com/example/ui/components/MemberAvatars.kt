package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IdeaMember
import com.example.data.model.IdeaRole
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.SkyBluePrimary

@Composable
fun MemberAvatarsRow(
    members: List<IdeaMember>,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 26.dp,
    maxVisible: Int = 3,
    showRoleBadges: Boolean = true,
    onAvatarClick: ((IdeaMember) -> Unit)? = null
) {
    if (members.isEmpty()) return

    val visibleMembers = members.take(maxVisible)
    val overflowCount = members.size - maxVisible

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleMembers.forEachIndexed { index, member ->
            val offset = if (index > 0) (-6).dp else 0.dp
            Box(
                modifier = Modifier
                    .offset(x = offset * index)
                    .size(avatarSize + 4.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, DarkCardBlack, CircleShape)
                    .clickable(enabled = onAvatarClick != null) { onAvatarClick?.invoke(member) }
            ) {
                // Background gradient based on role
                val bgBrush = when (member.role) {
                    IdeaRole.OWNER -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                    IdeaRole.EXECUTOR -> Brush.linearGradient(listOf(MintGreenAccent, Color(0xFF10B981)))
                    IdeaRole.CONTRIBUTOR -> Brush.linearGradient(listOf(SkyBluePrimary, Color(0xFF0284A5)))
                    IdeaRole.NONE -> Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF475569)))
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(bgBrush, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = member.displayName.trim().split(Regex("\\s+"))
                        .filter { it.isNotBlank() }
                        .let { parts ->
                            if (parts.size >= 2) "${parts[0].take(1).uppercase()}${parts[1].take(1).uppercase()}"
                            else if (parts.isNotEmpty()) parts[0].take(2).uppercase()
                            else "U"
                        }

                    Text(
                        text = initials,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (avatarSize.value * 0.42f).sp
                        ),
                        color = Color.White
                    )
                }

                // Mini role badge indicator
                if (showRoleBadges) {
                    val badgeIcon = when (member.role) {
                        IdeaRole.OWNER -> Icons.Default.Star
                        IdeaRole.EXECUTOR -> Icons.Default.Bolt
                        else -> null
                    }

                    val badgeColor = when (member.role) {
                        IdeaRole.OWNER -> Color(0xFFFBBF24)
                        IdeaRole.EXECUTOR -> MintGreenAccent
                        else -> null
                    }

                    if (badgeIcon != null && badgeColor != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size((avatarSize.value * 0.48f).dp)
                                .clip(CircleShape)
                                .background(DarkCardBlack)
                                .border(0.5.dp, badgeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = member.role.label,
                                tint = badgeColor,
                                modifier = Modifier.size((avatarSize.value * 0.35f).dp)
                            )
                        }
                    }
                }
            }
        }

        if (overflowCount > 0) {
            val offset = (-6).dp * maxVisible
            Box(
                modifier = Modifier
                    .offset(x = offset)
                    .size(avatarSize + 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155))
                    .border(1.5.dp, DarkCardBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$overflowCount",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (avatarSize.value * 0.4f).sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MemberRoleBadge(
    role: IdeaRole,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = when (role) {
        IdeaRole.OWNER -> Quadruple(Color(0x33F59E0B), Color(0xFFFBBF24), "Owner", Icons.Filled.WorkspacePremium)
        IdeaRole.EXECUTOR -> Quadruple(Color(0x3386F5D0), MintGreenAccent, "Executor", Icons.Filled.Bolt)
        IdeaRole.CONTRIBUTOR -> Quadruple(Color(0x3349CBEB), SkyBluePrimary, "Contributor", Icons.Filled.Person)
        IdeaRole.NONE -> Quadruple(Color(0x22FFFFFF), Color(0xFF94A3B8), "Viewer", null)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

