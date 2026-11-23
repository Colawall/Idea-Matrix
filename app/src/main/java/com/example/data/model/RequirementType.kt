package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.vector.ImageVector

enum class RequirementType(
    val title: String,
    val description: String,
    val defaultWeight: Float = 1.0f
) {
    MONEY(
        title = "Money",
        description = "Budget, initial capital, runway",
        defaultWeight = 1.2f
    ),
    SKILLS(
        title = "Skills",
        description = "Technical, design & marketing expertise",
        defaultWeight = 1.3f
    ),
    TIME(
        title = "Time",
        description = "Weekly bandwidth & dedicated focus",
        defaultWeight = 1.1f
    ),
    TOOLS(
        title = "Tools",
        description = "Software licenses, APIs & hosting",
        defaultWeight = 0.9f
    ),
    PEOPLE(
        title = "People",
        description = "Co-founders, contractors & mentors",
        defaultWeight = 1.0f
    ),
    KNOWLEDGE(
        title = "Knowledge",
        description = "Customer validation & domain insight",
        defaultWeight = 1.2f
    );

    val icon: ImageVector
        get() = when (this) {
            MONEY -> Icons.Default.AttachMoney
            SKILLS -> Icons.Default.Psychology
            TIME -> Icons.Default.Schedule
            TOOLS -> Icons.Default.Build
            PEOPLE -> Icons.Default.Group
            KNOWLEDGE -> Icons.Default.AutoStories
        }

    companion object {
        fun fromTitle(title: String): RequirementType? {
            return entries.firstOrNull { it.title.equals(title, ignoreCase = true) }
        }

        fun parseCommaSeparated(csv: String): Set<RequirementType> {
            if (csv.isBlank()) return emptySet()
            return csv.split(",")
                .mapNotNull { fromTitle(it.trim()) }
                .toSet()
        }

        fun toCommaSeparated(set: Set<RequirementType>): String {
            return set.joinToString(",") { it.title }
        }
    }
}
