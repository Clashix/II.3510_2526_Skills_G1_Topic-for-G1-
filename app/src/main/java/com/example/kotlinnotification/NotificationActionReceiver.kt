package com.example.kotlinnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

/**
 * Handles button clicks from interactive notifications
 * When user taps "YES" or "NO" in a notification, Android sends a broadcast here
 */
class NotificationActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        // Get notification ID to cancel it after action
        val notifId = intent.getIntExtra("notif_id", -1)
        
        // Check which button was clicked
        when (intent.action) {
            NotifIds.ACTION_YES -> {
                Toast.makeText(context, "Action: OUI", Toast.LENGTH_SHORT).show()
                // Cancel notification after action
                if (notifId != -1) NotificationManagerCompat.from(context).cancel(notifId)
            }
            NotifIds.ACTION_NO -> {
                Toast.makeText(context, "Action: NON", Toast.LENGTH_SHORT).show()
                if (notifId != -1) NotificationManagerCompat.from(context).cancel(notifId)
            }
        }
    }
}

