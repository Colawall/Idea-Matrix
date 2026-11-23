package com.example.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class IdeaReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val ideaId = inputData.getLong(EXTRA_IDEA_ID, -1L)
        if (ideaId <= 0) return Result.success()

        val db = AppDatabase.getInstance(applicationContext)
        val idea = db.ideaDao().getIdeaById(ideaId).firstOrNull() ?: return Result.success()

        val config = idea.getReminderConfig()
        if (!config.enabled) return Result.success()

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Check quiet hours
        val inQuietHours = isHourInQuietRange(currentHour, config.quietHoursStartHour, config.quietHoursEndHour)
        if (!inQuietHours) {
            postReminderNotification(idea)
        }

        // Schedule next reminder iteration
        ReminderScheduler.scheduleReminder(applicationContext, idea)

        return Result.success()
    }

    private fun postReminderNotification(idea: com.example.data.local.IdeaEntity) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "idea_matrix_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Idea Matrix Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Idea progress, next steps and reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Compute next step from requirement with largest gap weighted by priority
        val largestGap = idea.getLargestGapRequirement()
        val uncompleted = idea.getExecutionSteps().filter { !it.isCompleted }

        val requirementDetail = if (largestGap != null) {
            val (reqType, score) = largestGap
            "Prioritize ${reqType.title} (current: $score%)."
        } else {
            "Keep moving forward."
        }

        val nextStep = if (idea.nextAction.isNotBlank()) {
            idea.nextAction
        } else {
            uncompleted.firstOrNull()?.title ?: "Review execution steps"
        }

        val bodyText = "Next step: $nextStep • $requirementDetail"

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_idea_id", idea.id)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            applicationContext,
            (idea.id * 100 + 1).toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Delete intent to track consecutive dismissals
        val deleteIntent = Intent(applicationContext, ReminderNotificationReceiver::class.java).apply {
            action = ReminderNotificationReceiver.ACTION_REMINDER_DISMISSED
            putExtra(ReminderNotificationReceiver.EXTRA_IDEA_ID, idea.id)
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            (idea.id * 100 + 2).toInt(),
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Idea Matrix: ${idea.title}")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .build()

        notificationManager.notify((idea.id % 10000).toInt() + 100, notification)
    }

    private fun isHourInQuietRange(currentHour: Int, quietStart: Int, quietEnd: Int): Boolean {
        return if (quietStart <= quietEnd) {
            currentHour in quietStart until quietEnd
        } else {
            // Wraps over midnight (e.g. 22:00 to 08:00)
            currentHour >= quietStart || currentHour < quietEnd
        }
    }

    companion object {
        const val EXTRA_IDEA_ID = "idea_id"
    }
}
