package com.example.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.ReminderFrequency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ReminderNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val ideaId = intent.getLongExtra(EXTRA_IDEA_ID, -1L)
        if (ideaId <= 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val idea = db.ideaDao().getIdeaById(ideaId).firstOrNull()
                if (idea != null) {
                    when (action) {
                        ACTION_REMINDER_DISMISSED -> {
                            val config = idea.getReminderConfig()
                            val updatedDismissals = config.consecutiveDismissals + 1
                            val updatedConfig = config.copy(consecutiveDismissals = updatedDismissals)
                            val updatedIdea = idea.copy(
                                reminderConfigJson = updatedConfig.toJsonObject().toString(),
                                updatedAt = System.currentTimeMillis()
                            )
                            db.ideaDao().updateIdea(updatedIdea)

                            // If dismissed 5 times in a row, offer to reduce frequency
                            if (updatedDismissals >= 5) {
                                postReduceFrequencyNotification(context, updatedIdea)
                            }
                        }

                        ACTION_REDUCE_FREQUENCY -> {
                            val config = idea.getReminderConfig()
                            val updatedConfig = config.copy(
                                frequency = ReminderFrequency.WEEKLY,
                                consecutiveDismissals = 0
                            )
                            val updatedIdea = idea.copy(
                                reminderConfigJson = updatedConfig.toJsonObject().toString(),
                                updatedAt = System.currentTimeMillis()
                            )
                            db.ideaDao().updateIdea(updatedIdea)

                            // Cancel reduce prompt notification
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(NOTIFICATION_ID_REDUCE_OFFER_BASE + (ideaId % 1000).toInt())

                            // Reschedule with weekly frequency
                            ReminderScheduler.scheduleReminder(context, updatedIdea)
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postReduceFrequencyNotification(context: Context, idea: com.example.data.local.IdeaEntity) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "idea_matrix_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Idea Matrix Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Idea progress, next steps and cadence adjustments"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open app action
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_idea_id", idea.id)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            (idea.id * 10 + 1).toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Reduce frequency one-tap action
        val reduceIntent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            action = ACTION_REDUCE_FREQUENCY
            putExtra(EXTRA_IDEA_ID, idea.id)
        }
        val reducePendingIntent = PendingIntent.getBroadcast(
            context,
            (idea.id * 10 + 2).toInt(),
            reduceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Reduce reminder frequency?")
            .setContentText("You've dismissed 5 reminders in a row for \"${idea.title}\". Tap to switch to Weekly.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "You've dismissed 5 reminders in a row for \"${idea.title}\". Would you like to switch to Weekly reminders or adjust quiet hours?"
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Switch to Weekly", reducePendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_REDUCE_OFFER_BASE + (idea.id % 1000).toInt(), notification)
    }

    companion object {
        const val ACTION_REMINDER_DISMISSED = "com.example.ACTION_REMINDER_DISMISSED"
        const val ACTION_REDUCE_FREQUENCY = "com.example.ACTION_REDUCE_FREQUENCY"
        const val EXTRA_IDEA_ID = "extra_idea_id"
        const val NOTIFICATION_ID_REDUCE_OFFER_BASE = 50000
    }
}
