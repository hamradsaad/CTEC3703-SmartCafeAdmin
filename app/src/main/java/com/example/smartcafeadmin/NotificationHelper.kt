package com.example.smartcafeadmin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

// Shows order notifications (CAFE-09)
object NotificationHelper {

    private const val CHANNEL_ID = "order_updates"

    // Android 8+ needs a notification channel
    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Order updates", NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "New orders and order status changes"
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    // Android 13+ needs the user's permission to show notifications
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    fun show(context: Context, id: Int, title: String, text: String) {
        if (!hasPermission(context)) return   // refused: the app still shows a message on screen
        createChannel(context)

        // Tapping the notification opens the Orders screen
        val intent = Intent(context, OrdersActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pending = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // Permission was removed in Settings: skip quietly
        }
    }
}