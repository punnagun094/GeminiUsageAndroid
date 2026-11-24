package com.fifa.geminiusage

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class GeminiUsageWidget : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        UsageAlarmReceiver.scheduleNextSync(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, GeminiUsageWidget::class.java)
            val ids = manager.getAppWidgetIds(component)
            for (id in ids) {
                updateSingleWidget(context, manager, id)
            }
        }

        private fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("gemini_usage_prefs", Context.MODE_PRIVATE)
            val rawQuota = prefs.getString("last_quota", "") ?: ""
            val lastTime = prefs.getString("last_time", "") ?: ""

            // ดึงตัวเลข % ทั้ง 2 ค่า (ตัวแรก = รอบ 5 ชม., ตัวที่สอง = รายสัปดาห์)
            val pctMatches = Regex("""(\d+)\s*%""").findAll(rawQuota).mapNotNull {
                it.groupValues[1].toIntOrNull()
            }.toList()

            val fiveHrPct = pctMatches.getOrNull(0) ?: 0
            val weeklyPct = pctMatches.getOrNull(1) ?: 0

            // ดึงบรรทัดเวลารีเซ็ตทั้ง 2 บรรทัด
            val resetLines = rawQuota.lines()
                .map { it.trim() }
                .filter { it.contains("รีเซ็ต") || it.contains("Reset", ignoreCase = true) }

            val fiveHrReset = resetLines.getOrNull(0) ?: context.getString(R.string.five_hr_cycle_short)
            val weeklyReset = resetLines.getOrNull(1) ?: context.getString(R.string.weekly_limit)

            val updatedLabel = if (lastTime.isNotEmpty()) {
                context.getString(R.string.widget_updated_at, lastTime)
            } else {
                context.getString(R.string.widget_tap_to_refresh)
            }

            val views = RemoteViews(context.packageName, R.layout.widget_gemini_usage).apply {
                setTextViewText(R.id.tvWidgetUpdated, updatedLabel)
                setTextViewText(R.id.tvFiveHrReset, fiveHrReset)
                setTextViewText(R.id.tvFiveHrPct, if (pctMatches.isNotEmpty()) "$fiveHrPct%" else "--%")
                setProgressBar(R.id.pbFiveHr, 100, fiveHrPct.coerceIn(0, 100), false)

                setTextViewText(R.id.tvWeeklyReset, weeklyReset)
                setTextViewText(R.id.tvWeeklyPct, if (pctMatches.size >= 2) "$weeklyPct%" else "--%")
                setProgressBar(R.id.pbWeekly, 100, weeklyPct.coerceIn(0, 100), false)

                // แตะที่ Widget เพื่อเปิดแอปและสั่งรีเฟรชตัวเลขล่าสุดทันที
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}