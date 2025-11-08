package com.example.kotlinnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifId = intent.getIntExtra("notif_id", -1)
        when (intent.action) {
            NotifIds.ACTION_YES -> {
                Toast.makeText(context, "Action: OUI", Toast.LENGTH_SHORT).show()
                if (notifId != -1) NotificationManagerCompat.from(context).cancel(notifId)
            }
            NotifIds.ACTION_NO -> {
                Toast.makeText(context, "Action: NON", Toast.LENGTH_SHORT).show()
                if (notifId != -1) NotificationManagerCompat.from(context).cancel(notifId)
            }
        }
    }
}

