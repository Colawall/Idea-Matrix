package com.example.ui.screens.resources

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.IdeaEntity
import com.example.data.model.IdeaResource
import com.example.data.model.RequirementType
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.viewmodel.IdeaViewModel

data class ResourceGuide(
    val type: RequirementType,
    val title: String,
    val summary: String,
    val actionTips: List<String>
)

data class AggregatedIdeaResource(
    val resource: IdeaResource,
    val idea: IdeaEntity
)

@Composable
fun ResourcesScreen(
    viewModel: IdeaViewModel? = null,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Playbooks, 1: Idea Resources (PDFs, Docs, URLs)
    var selectedTypeFilter by remember { mutableStateOf("All") } // "All", "PDF", "Doc", "URL"

    val allIdeas by viewModel?.allIdeas?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    val aggregatedResources = remember(allIdeas) {
        val list = mutableListOf<AggregatedIdeaResource>()
        allIdeas.forEach { idea ->
            idea.getResourcesList().forEach { res ->
                list.add(AggregatedIdeaResource(resource = res, idea = idea))
            }
        }
        list
    }

    val filteredResources = remember(aggregatedResources, selectedTypeFilter) {
        if (selectedTypeFilter == "All") {
            aggregatedResources
        } else {
            aggregatedResources.filter { it.resource.type.equals(selectedTypeFilter, ignoreCase = true) }
        }
    }

    val guides = listOf(
        ResourceGuide(
            type = RequirementType.MONEY,
            title = "Capital & Micro-Budgeting",
            summary = "Fund early iterations without diluting ownership or taking on debt.",
            actionTips = listOf(
                "Apply for Google Cloud for Startups & AWS Activate credits ($10k-$100k)",
                "Pre-sell 5 annual licenses with a discounted founder tier",
                "Keep cloud costs below $20/month using serverless free tiers"
            )
        ),
        ResourceGuide(
            type = RequirementType.SKILLS,
            title = "Rapid Execution Stack",
            summary = "Bridge engineering and design gaps with high-leverage frameworks.",
            actionTips = listOf(
                "Use Jetpack Compose and Tailwind UI components for instant polish",
                "Deploy backend endpoints using Firebase Cloud Functions or Supabase",
                "Automate marketing assets with Figma auto-layout templates"
            )
        ),
        ResourceGuide(
            type = RequirementType.TIME,
            title = "10-Hour Founder Cadence",
            summary = "Make substantial weekly progress while balancing work and life.",
            actionTips = listOf(
                "Block two 90-minute deep-work focus sprints on Tue & Thu mornings",
                "Dedicate Saturday morning strictly to user feedback and demos",
                "Cut non-essential features into a 'v1.1' backlog immediately"
            )
        ),
        ResourceGuide(
            type = RequirementType.TOOLS,
            title = "Solo-Builder SaaS Stack",
            summary = "The battle-tested toolset for zero-maintenance startup ops.",
            actionTips = listOf(
                "Payments: Stripe Billing & Customer Portal",
                "Analytics: PostHog or Umami for privacy-friendly tracking",
                "Hosting: Vercel, Supabase & Android Play Console"
            )
        ),
        ResourceGuide(
            type = RequirementType.PEOPLE,
            title = "Finding Allies & Co-Founders",
            summary = "Recruit complementary builders without burning cash.",
            actionTips = listOf(
                "Build in public on X and LinkedIn sharing weekly metrics",
                "Join YC Co-Founder Match with a validated MVP ready",
                "Trade domain expertise with peers on IndieHackers"
            )
        ),
        ResourceGuide(
            type = RequirementType.KNOWLEDGE,
            title = "The Mom Test Validation",
            summary = "How to talk to prospective customers without hearing false praise.",
            actionTips = listOf(
                "Ask about past behavior, not hypothetical future purchases",
                "Find out what software or manual hacks they currently pay for",
                "If they haven't actively tried solving it, it's not a burning problem"
            )
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("resources_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Resources & Playbooks",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Tactical playbooks and attached PDFs, docs, & URLs across your ideas",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            // Tab Selector Pill: [ Tactical Playbooks | Attached Idea Resources (PDFs, Docs, URLs) ]
            item {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (colors.isDark) DarkSurfaceElevated else Color(0x1F000000),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selectedTab == 0) (if (colors.isDark) SkyBluePrimary else Color.White) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedTab = 0 }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) DarkCardBlack else colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Playbooks",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedTab == 0) DarkCardBlack else colors.textSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selectedTab == 1) MintGreenAccent else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedTab = 1 }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderShared,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) DarkCardBlack else colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Idea Files (${aggregatedResources.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedTab == 1) DarkCardBlack else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // Tactical Guide Playbooks
                items(guides) { guide ->
                    ResourceGuideCard(guide = guide)
                }
            } else {
                // Attached Idea Resources (PDFs, Docs, URLs)
                item {
                    // Filter Chips: All, PDF, Doc, URL
                    val filterOptions = listOf("All", "PDF", "Doc", "URL", "Tool", "Note")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterOptions.forEach { filter ->
                            val isSelected = selectedTypeFilter == filter
                            val pillColor = if (isSelected) {
                                when (filter) {
                                    "PDF" -> Color(0xFFEF4444)
                                    "Doc" -> Color(0xFF6366F1)
                                    "URL" -> SkyBluePrimary
                                    "Tool" -> MintGreenAccent
                                    else -> if (colors.isDark) Color.White else DarkCardBlack
                                }
                            } else colors.chipBg

                            val textColor = if (isSelected) {
                                when (filter) {
                                    "PDF", "Doc", "URL", "Tool" -> DarkCardBlack
                                    else -> if (colors.isDark) DarkCardBlack else Color.White
                                }
                            } else colors.textPrimary

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { selectedTypeFilter = filter },
                                shape = RoundedCornerShape(50),
                                color = pillColor
                            ) {
                                Text(
                                    text = filter,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                if (filteredResources.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                            border = if (colors.isDark) BorderStroke(1.dp, colors.cardBorder) else null
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderShared,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = if (aggregatedResources.isEmpty()) "No Resources Added Yet" else "No $selectedTypeFilter resources found",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Attach PDFs, docs, links, and research to any of your ideas via the Idea Detail or Add Idea flow.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(filteredResources) { item ->
                        val res = item.resource
                        val idea = item.idea

                        val badgeBg = when (res.type) {
                            "PDF" -> Color(0x33EF4444)
                            "Doc" -> Color(0x336366F1)
                            "URL", "Link" -> if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight
                            "Tool" -> Color(0x3310B981)
                            else -> colors.chipBg
                        }
                        val badgeColor = when (res.type) {
                            "PDF" -> Color(0xFFEF4444)
                            "Doc" -> Color(0xFF818CF8)
                            "URL", "Link" -> SkyBluePrimary
                            "Tool" -> MintGreenAccent
                            else -> colors.textSecondary
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = if (colors.isDark) 2.dp else 4.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    spotColor = Color(0x12000000)
                                )
                                .clickable {
                                    viewModel?.selectIdeaForDetail(idea)
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
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
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (res.type) {
                                                "PDF", "Doc" -> Icons.Default.Description
                                                "URL", "Link" -> Icons.Default.Link
                                                else -> Icons.Default.OpenInNew
                                            },
                                            contentDescription = res.type,
                                            tint = badgeColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = res.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = colors.textPrimary
                                            )
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

                                        Text(
                                            text = "From idea: ${idea.title}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = SkyBluePrimary
                                        )

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
                                                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Open Link",
                                            tint = badgeColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun ResourceGuideCard(guide: ResourceGuide) {
    val colors = AppTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0x3349CBEB) else SkyBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = guide.type.icon,
                        contentDescription = guide.type.title,
                        tint = if (colors.isDark) SkyBluePrimary else Color(0xFF0369A1),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = guide.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${guide.type.title} Bottleneck",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MintGreenDark
                    )
                }
            }

            Text(
                text = guide.summary,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.chipBg, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                guide.actionTips.forEach { tip ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "•", color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
