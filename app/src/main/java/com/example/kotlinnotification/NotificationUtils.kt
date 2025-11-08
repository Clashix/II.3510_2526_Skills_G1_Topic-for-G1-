package com.example.kotlinnotification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder

/**
 * Constants used for notifications throughout the app
 */
object NotifIds {
    // Notification channel ID (required for Android 8.0+)
    const val CHANNEL_ID = "demo_notifications_channel"
    
    // Unique IDs for different notification types
    const val SIMPLE_ID = 1001
    const val INTERACTIVE_ID = 1002

    // Action strings for notification buttons (must be unique)
    const val ACTION_YES = "com.example.kotlinnotification.ACTION_YES"
    const val ACTION_NO = "com.example.kotlinnotification.ACTION_NO"
}

// Create notification channel (required for Android 8.0+)
// Channels let users control notification settings per category
fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val chan = NotificationChannel(
            NotifIds.CHANNEL_ID,
            "Demo Notifications",
            NotificationManager.IMPORTANCE_HIGH // High importance = heads-up display
        ).apply {
            description = "Channel for demo notifications"
            enableLights(true)
            lightColor = Color.BLUE
            enableVibration(true)
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(chan)
    }
}

// Check if app can post notifications
fun canPostNotifications(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    val pm = NotificationManagerCompat.from(context)
    return pm.areNotificationsEnabled()
}

// Create and show a simple notification
@SuppressLint("MissingPermission")
fun simpleNotification(context: Context) {
    if (!canPostNotifications(context)) return
    ensureNotificationChannel(context)

    // Intent to open app when notification is tapped
    val contentIntent = Intent(context, MainActivity::class.java)
    val contentPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(contentIntent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }!!

    // Build the notification
    val builder = NotificationCompat.Builder(context, NotifIds.CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Notification simple")
        .setContentText("Ceci est une notification classique.")
        .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority = heads-up display
        .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound + vibration
        .setAutoCancel(true) // Auto-dismiss when tapped
        .setContentIntent(contentPendingIntent) // Open app when tapped

    // Show the notification
    with(NotificationManagerCompat.from(context)) {
        notify(NotifIds.SIMPLE_ID, builder.build())
    }
}

// Create and show an interactive notification with action buttons
@SuppressLint("MissingPermission")
fun interactiveNotification(context: Context) {
    if (!canPostNotifications(context)) return
    ensureNotificationChannel(context)

    // Create intent for "YES" button - sends broadcast to NotificationActionReceiver
    val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = NotifIds.ACTION_YES
        putExtra("notif_id", NotifIds.INTERACTIVE_ID)
    }
    val yesPending = PendingIntent.getBroadcast(
        context, 1, yesIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Create intent for "NO" button - sends broadcast to NotificationActionReceiver
    val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = NotifIds.ACTION_NO
        putExtra("notif_id", NotifIds.INTERACTIVE_ID)
    }
    val noPending = PendingIntent.getBroadcast(
        context, 2, noIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Intent to open app when notification body is tapped
    val contentIntent = Intent(context, MainActivity::class.java)
    val contentPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(contentIntent)
        getPendingIntent(3, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }!!

    // Build the notification with action buttons
    val builder = NotificationCompat.Builder(context, NotifIds.CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_email)
        .setContentTitle("Notification interactive")
        .setContentText("Choisis une action : OUI ou NON.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)
        .addAction(0, "OUI", yesPending) // Add "YES" button
        .addAction(0, "NON", noPending)  // Add "NO" button

    // Show the notification
    with(NotificationManagerCompat.from(context)) {
        notify(NotifIds.INTERACTIVE_ID, builder.build())
    }
}