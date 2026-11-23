package com.example.ui.screens.ideas

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.DaysRemainingChip
import com.example.ui.components.MemberAvatarsRow
import com.example.ui.components.ReadinessPillBadge
import com.example.ui.screens.home.SearchPillBar
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.MintGreenLight
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.viewmodel.IdeaViewModel

@Composable
fun IdeasScreen(
    viewModel: IdeaViewModel,
    onNavigateToAddIdea: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredIdeas by viewModel.filteredIdeas.collectAsState()
    val allIdeas by viewModel.allIdeas.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("ideas_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Idea Matrix",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${allIdeas.size} concepts tracked & scored",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    // New Idea Pill Button
                    Button(
                        onClick = onNavigateToAddIdea,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (colors.isDark) SkyBluePrimary else DarkCardBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = if (colors.isDark) DarkCardBlack else MintGreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (colors.isDark) DarkCardBlack else Color.White
                            )
                        }
                    }
                }
            }

            // Search pill
            item {
                SearchPillBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )
            }

            // Category & Status Chips
            item {
                val filters = listOf("All", "High Readiness", "In Progress", "B2B SaaS", "Mobile App", "Creator Tool", "Fintech")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        val chipBg = if (isSelected) {
                            if (colors.isDark) SkyBluePrimary else DarkCardBlack
                        } else {
                            colors.cardBackground
                        }
                        val chipText = if (isSelected) {
                            if (colors.isDark) DarkCardBlack else Color.White
                        } else {
                            colors.textPrimary
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { viewModel.onFilterSelected(filter) },
                            shape = RoundedCornerShape(50),
                            color = chipBg,
                            border = if (!isSelected && colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                        ) {
                            Text(
                                text = filter,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = chipText
                            )
                        }
                    }
                }
            }

            // Ideas List
            if (filteredIdeas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ideas found in this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                items(filteredIdeas, key = { it.id }) { idea ->
                    val isOwner = idea.isOwner(currentUser?.uid) || idea.ownerId.isBlank()
                    IdeaMatrixItemCard(
                        idea = idea,
                        onCardClick = { viewModel.selectIdeaForDetail(idea) },
                        onEditClick = { viewModel.selectIdeaForDetail(idea, editMode = true) },
                        onDelete = { viewModel.deleteIdea(idea.id) },
                        showDelete = isOwner
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
fun IdeaMatrixItemCard(
    idea: IdeaEntity,
    onCardClick: () -> Unit = {},
    onEditClick: () -> Unit = onCardClick,
    onDelete: () -> Unit,
    showDelete: Boolean = true
) {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
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

            Text(
                text = idea.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = colors.textPrimary
            )

            Text(
                text = idea.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )

            // Requirement breakdown badges
            val reqs = idea.getRequirementsSet()
            if (reqs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    reqs.forEach { req ->
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
                                .padding(horizontal = 8.dp, vertical = 4.dp)
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
            }

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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SkyBluePrimary,
                        modifier = Modifier.clickable { onEditClick() }
                    )

                    if (showDelete) {
                        Text(
                            text = "Remove",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF4444),
                            modifier = Modifier.clickable { onDelete() }
                        )
                    }
                }
            }
        }
    }
}
