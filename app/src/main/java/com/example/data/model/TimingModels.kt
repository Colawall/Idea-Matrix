package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

enum class TimingStatus(val label: String) {
    PLANNING("Planning"),
    ACTIVE("Active"),
    PAUSED("Paused"),
    DONE("Done");

    companion object {
        fun fromString(value: String): TimingStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } ?: PLANNING
        }
    }
}

enum class ReminderFrequency(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    CUSTOM("Custom Days");

    companion object {
        fun fromString(value: String): ReminderFrequency {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } ?: DAILY
        }
    }
}

data class ReminderConfig(
    val enabled: Boolean = false,
    val frequency: ReminderFrequency = ReminderFrequency.DAILY,
    val customDaysOfWeek: List<Int> = listOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY),
    val timeHour: Int = 9,
    val timeMinute: Int = 0,
    val quietHoursStartHour: Int = 22, // 10 PM
    val quietHoursEndHour: Int = 8,    // 8 AM
    val consecutiveDismissals: Int = 0
) {
    fun toJsonObject(): JSONObject {
        val daysArray = JSONArray()
        customDaysOfWeek.forEach { daysArray.put(it) }

        return JSONObject().apply {
            put("enabled", enabled)
            put("frequency", frequency.name)
            put("customDaysOfWeek", daysArray)
            put("timeHour", timeHour)
            put("timeMinute", timeMinute)
            put("quietHoursStartHour", quietHoursStartHour)
            put("quietHoursEndHour", quietHoursEndHour)
            put("consecutiveDismissals", consecutiveDismissals)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): ReminderConfig {
            val daysList = mutableListOf<Int>()
            val array = obj.optJSONArray("customDaysOfWeek")
            if (array != null) {
                for (i in 0 until array.length()) {
                    daysList.add(array.getInt(i))
                }
            } else {
                daysList.addAll(listOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY))
            }

            return ReminderConfig(
                enabled = obj.optBoolean("enabled", false),
                frequency = ReminderFrequency.fromString(obj.optString("frequency", "DAILY")),
                customDaysOfWeek = if (daysList.isEmpty()) listOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY) else daysList,
                timeHour = obj.optInt("timeHour", 9),
                timeMinute = obj.optInt("timeMinute", 0),
                quietHoursStartHour = obj.optInt("quietHoursStartHour", 22),
                quietHoursEndHour = obj.optInt("quietHoursEndHour", 8),
                consecutiveDismissals = obj.optInt("consecutiveDismissals", 0)
            )
        }

        fun fromJson(jsonStr: String): ReminderConfig {
            if (jsonStr.isBlank()) return ReminderConfig()
            return try {
                fromJsonObject(JSONObject(jsonStr))
            } catch (_: Exception) {
                ReminderConfig()
            }
        }
    }
}

data class WorkSessionLog(
    val id: String = UUID.randomUUID().toString(),
    val startTimeMillis: Long = System.currentTimeMillis(),
    val endTimeMillis: Long = System.currentTimeMillis(),
    val durationMillis: Long = 0,
    val notes: String = ""
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("startTimeMillis", startTimeMillis)
            put("endTimeMillis", endTimeMillis)
            put("durationMillis", durationMillis)
            put("notes", notes)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): WorkSessionLog {
            return WorkSessionLog(
                id = obj.optString("id", UUID.randomUUID().toString()),
                startTimeMillis = obj.optLong("startTimeMillis", System.currentTimeMillis()),
                endTimeMillis = obj.optLong("endTimeMillis", System.currentTimeMillis()),
                durationMillis = obj.optLong("durationMillis", 0L),
                notes = obj.optString("notes", "")
            )
        }

        fun listToJson(sessions: List<WorkSessionLog>): String {
            val array = JSONArray()
            sessions.forEach { array.put(it.toJsonObject()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String): List<WorkSessionLog> {
            if (jsonStr.isBlank()) return emptyList()
            val list = mutableListOf<WorkSessionLog>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(fromJsonObject(array.getJSONObject(i)))
                }
            } catch (_: Exception) {}
            return list
        }
    }
}
