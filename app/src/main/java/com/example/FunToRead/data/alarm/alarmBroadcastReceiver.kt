// data/alarm/alarmBroadcastReceiver.kt
package com.example.FunToRead.data.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.FunToRead.R
import com.example.FunToRead.ui.MainActivity
import com.example.FunToRead.ui.ItemsViewModel

  //BroadcastReceiver for handling alarms and showing notifications when an alarm is triggered.

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val readingTime = intent.getIntExtra("reading_time", 0)
        Log.d("AlarmBroadcastReceiver", "onReceive called with readingTime: $readingTime")

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("AlarmBroadcastReceiver", "Notification permission not granted")
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "default")
            .setContentTitle(context.getString(R.string.Timer_Expired))
            .setContentText(context.getString(R.string.Your_reading_time_has_ended))
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        Log.d("AlarmBroadcastReceiver", "Showing notification")

        updateTotalReadingTime(context, readingTime)

        try {
            NotificationManagerCompat.from(context).notify(1, notification)
        } catch (e: SecurityException) {
            Log.e("AlarmBroadcastReceiver", "SecurityException when showing notification: ${e.message}", e)
        }
    }

    private fun updateTotalReadingTime(context: Context, readingTime: Int) {
        val sharedPreferences = context.getSharedPreferences("reading_prefs", Context.MODE_PRIVATE)
        val totalReadingTime = sharedPreferences.getInt("total_reading_time", 0)
        with(sharedPreferences.edit()) {
            putInt("total_reading_time", totalReadingTime + readingTime)
            apply()
        }
    }
}