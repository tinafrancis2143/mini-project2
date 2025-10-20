package com.example.miniproject2.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.miniproject2.R

class NotificationReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManagerCompat.from(context)

        // Get the details for the notification from the Intent
        val notificationId = intent.getIntExtra("notificationId", 0)
        val message = intent.getStringExtra("message") ?: "Your product is expiring soon!"

        // --- FIX #1: Create a Notification Channel only on Android 8.0 (Oreo) and higher ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "expiry_notifications"
            val channelName = "Product Expiry Alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for expiring products"
            }
            // Register the channel with the system
//            notificationManager.createNotificationChannel(channel)
            val systemNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotificationManager.createNotificationChannel(channel)

        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, "expiry_notifications")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("Glowly Expiry Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // --- FIX #2: Check for the POST_NOTIFICATIONS permission before showing the notification ---
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, notification)
        } else {
            // If permission is not granted, we cannot show the notification.
            // In a real app, you might log this or handle it differently.
        }
    }
}