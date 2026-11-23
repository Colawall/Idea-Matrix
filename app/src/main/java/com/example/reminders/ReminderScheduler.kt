package com.example.reminders

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.IdeaEntity
import com.example.data.model.ReminderFrequency
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun getWorkName(ideaId: Long): String = "idea_reminder_$ideaId"

    fun scheduleReminder(context: Context, idea: IdeaEntity) {
        val config = idea.getReminderConfig()
        if (!config.enabled) {
            cancelReminder(context, idea.id)
            return
        }

        val delayMillis = calculateInitialDelayMillis(config)
        val data = Data.Builder()
            .putLong(IdeaReminderWorker.EXTRA_IDEA_ID, idea.id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<IdeaReminderWorker>()
            .setInitialDelay(delayMillis.coerceAtLeast(10_000L), TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder_idea_${idea.id}")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            getWorkName(idea.id),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelReminder(context: Context, ideaId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(getWorkName(ideaId))
    }

    private fun calculateInitialDelayMillis(config: com.example.data.model.ReminderConfig): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, config.timeHour)
            set(Calendar.MINUTE, config.timeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (config.frequency) {
            ReminderFrequency.DAILY -> {
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            ReminderFrequency.WEEKLY -> {
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            ReminderFrequency.CUSTOM -> {
                val allowedDays = config.customDaysOfWeek.ifEmpty {
                    listOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)
                }
                var daysChecked = 0
                while (daysChecked < 14) {
                    val currentDay = target.get(Calendar.DAY_OF_WEEK)
                    if (allowedDays.contains(currentDay) && target.timeInMillis > now.timeInMillis) {
                        break
                    }
                    target.add(Calendar.DAY_OF_YEAR, 1)
                    daysChecked++
                }
            }
        }

        // Adjust if target falls into quiet hours
        val targetHour = target.get(Calendar.HOUR_OF_DAY)
        if (isHourInQuietRange(targetHour, config.quietHoursStartHour, config.quietHoursEndHour)) {
            // Push forward to quietHoursEndHour
            target.set(Calendar.HOUR_OF_DAY, config.quietHoursEndHour)
            target.set(Calendar.MINUTE, (config.timeMinute + 5) % 60)
            if (target.timeInMillis <= now.timeInMillis) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return target.timeInMillis - now.timeInMillis
    }

    private fun isHourInQuietRange(hour: Int, quietStart: Int, quietEnd: Int): Boolean {
        return if (quietStart <= quietEnd) {
            hour in quietStart until quietEnd
        } else {
            hour >= quietStart || hour < quietEnd
        }
    }
}
