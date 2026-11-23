package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "audit_reports")
data class AuditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ideaId: Long = 0,
    val ideaTitle: String = "",
    val periodType: String = "Weekly", // "Weekly" or "Monthly"
    val generatedAtMillis: Long = System.currentTimeMillis(),
    val readinessStart: Int = 0,
    val readinessEnd: Int = 0,
    val readinessDelta: Int = 0,
    val timeElapsedMillis: Long = 0,
    val plannedDurationMillis: Long = 0,
    val elapsedVsPlannedPercent: Int = 0,
    val completedStepsCount: Int = 0,
    val totalStepsCount: Int = 0,
    val completedStepsJson: String = "[]",
    val blockersJson: String = "[]",
    val recommendedNextStepsJson: String = "[]",
    val summaryNote: String = ""
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        return sdf.format(Date(generatedAtMillis))
    }

    fun getBlockersList(): List<String> {
        return parseStringList(blockersJson)
    }

    fun getRecommendedStepsList(): List<String> {
        return parseStringList(recommendedNextStepsJson)
    }

    fun getCompletedStepsList(): List<String> {
        return parseStringList(completedStepsJson)
    }

    companion object {
        fun stringListToJson(items: List<String>): String {
            val array = JSONArray()
            items.forEach { array.put(it) }
            return array.toString()
        }

        fun parseStringList(jsonStr: String): List<String> {
            if (jsonStr.isBlank()) return emptyList()
            val list = mutableListOf<String>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
            } catch (_: Exception) {}
            return list
        }
    }
}
