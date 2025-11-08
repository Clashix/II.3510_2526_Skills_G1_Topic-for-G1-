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

object NotifIds {
    const val CHANNEL_ID = "demo_notifications_channel"
    const val SIMPLE_ID = 1001
    const val INTERACTIVE_ID = 1002

    const val ACTION_YES = "com.example.kotlinnotification.ACTION_YES"
    const val ACTION_NO = "com.example.kotlinnotification.ACTION_NO"
}

fun ensureNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val chan = NotificationChannel(
            NotifIds.CHANNEL_ID,
            "Demo Notifications",
            NotificationManager.IMPORTANCE_HIGH // HIGH pour affichage heads-up
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

fun canPostNotifications(context: Context): Boolean {
    // Si tu as déjà la gestion de permission ailleurs, on s'aligne dessus :
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    val pm = NotificationManagerCompat.from(context)
    return pm.areNotificationsEnabled()
}

@SuppressLint("MissingPermission")
fun simpleNotification(context: Context) {
    if (!canPostNotifications(context)) return
    ensureNotificationChannel(context)

    // Intent qui ouvre l'app (MainActivity)
    val contentIntent = Intent(context, MainActivity::class.java)
    val contentPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(contentIntent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }!!

    val builder = NotificationCompat.Builder(context, NotifIds.CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Notification simple")
        .setContentText("Ceci est une notification classique.")
        .setPriority(NotificationCompat.PRIORITY_HIGH) // HIGH pour heads-up
        .setDefaults(NotificationCompat.DEFAULT_ALL) // Son + vibration
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)

    with(NotificationManagerCompat.from(context)) {
        notify(NotifIds.SIMPLE_ID, builder.build())
    }
}

@SuppressLint("MissingPermission")
fun interactiveNotification(context: Context) {
    if (!canPostNotifications(context)) return
    ensureNotificationChannel(context)

    // Action OUI (Broadcast)
    val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = NotifIds.ACTION_YES
        putExtra("notif_id", NotifIds.INTERACTIVE_ID)
    }
    val yesPending = PendingIntent.getBroadcast(
        context, 1, yesIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Action NON (Broadcast)
    val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = NotifIds.ACTION_NO
        putExtra("notif_id", NotifIds.INTERACTIVE_ID)
    }
    val noPending = PendingIntent.getBroadcast(
        context, 2, noIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Tap sur la notif → ouvre l'app
    val contentIntent = Intent(context, MainActivity::class.java)
    val contentPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(contentIntent)
        getPendingIntent(3, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }!!

    val builder = NotificationCompat.Builder(context, NotifIds.CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_email)
        .setContentTitle("Notification interactive")
        .setContentText("Choisis une action : OUI ou NON.")
        .setPriority(NotificationCompat.PRIORITY_HIGH) // HIGH pour heads-up
        .setDefaults(NotificationCompat.DEFAULT_ALL) // Son + vibration
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)
        .addAction(0, "OUI", yesPending)
        .addAction(0, "NON", noPending)

    with(NotificationManagerCompat.from(context)) {
        notify(NotifIds.INTERACTIVE_ID, builder.build())
    }
}