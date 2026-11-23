package com.fifa.geminiusage

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class UsageAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        // ตั้งรอบถัดไปในอีก 2 ชั่วโมงเสมอ
        scheduleNextSync(context)

        try {
            val serviceIntent = Intent(context, UsageSyncService::class.java)
            context.startService(serviceIntent)
        } catch (_: Exception) {
            // ข้ามหากระบบจำกัดการทำงานชั่วคราว
        }
    }

    companion object {
        private const val ACTION_SYNC = "com.fifa.geminiusage.ACTION_AUTO_SYNC"
        private const val INTERVAL_MS = 2 * 60 * 60 * 1000L // ทุก 2 ชั่วโมง

        fun scheduleNextSync(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, UsageAlarmReceiver::class.java).apply {
                action = ACTION_SYNC
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = System.currentTimeMillis() + INTERVAL_MS
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } catch (_: Exception) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
    }
}