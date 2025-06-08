package com.ccover.a3dprintercontrol.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ccover.a3dprintercontrol.R

object NotificationHelper {
    private const val CHANNEL_ID = "printer_alerts"
    private const val CHANNEL_NAME = "Оповещения о принтерах"
    private const val CHANNEL_DESC = "Критические события и уведомления о состоянии 3D-принтеров"

    /**
     * Создаёт канал уведомлений
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Показывает уведомление с заданным заголовком и текстом
     */
    fun showNotification(context: Context, title: String, message: String) {
        val notificationId = System.currentTimeMillis().toInt()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}
