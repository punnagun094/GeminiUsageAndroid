package com.fifa.geminiusage

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONTokener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageSyncService : Service() {

    private var headlessWebView: WebView? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable { cleanupAndStop() }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ตั้งเวลาตัดการทำงานอัตโนมัติใน 25 วินาที หากไม่มีเน็ต
        handler.postDelayed(timeoutRunnable, 25_000L)

        try {
            val wv = WebView(applicationContext)
            headlessWebView = wv

            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true)

            wv.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                userAgentString = userAgentString.replace("; wv", "")
            }

            wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val currentUrl = url ?: return

                    // ถ้า Session หมดอายุ ให้หยุดรอผู้ใช้เปิดแอปมาล็อกอินใหม่
                    if (currentUrl.contains("accounts.google.com")) {
                        cleanupAndStop()
                        return
                    }

                    if (currentUrl.contains("gemini.google.com/usage")) {
                        handler.postDelayed({
                            extractAndSaveQuota(wv)
                        }, 2500L)
                    }
                }
            }

            wv.loadUrl("https://gemini.google.com/usage?hl=th")
        } catch (_: Exception) {
            cleanupAndStop()
        }

        return START_NOT_STICKY
    }

    private fun extractAndSaveQuota(wv: WebView) {
        val jsCode = """
            (function() {
                const text = document.body.innerText || "";
                const lines = text.split('\n').map(l => l.trim()).filter(l => l.length > 0);
                const matched = lines.filter(l => 
                    l.includes('%') || l.includes('รีเซ็ต') || l.includes('Reset') || l.includes('ชั่วโมง') || l.includes('สัปดาห์')
                );
                return matched.length > 0 ? matched.join('\n') : "";
            })();
        """.trimIndent()

        wv.evaluateJavascript(jsCode) { rawResult ->
            val cleanText = (JSONTokener(rawResult).nextValue() as? String)?.trim() ?: ""
            if (cleanText.contains("%")) {
                val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                getSharedPreferences("gemini_usage_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("last_quota", cleanText)
                    .putString("last_time", timeNow)
                    .apply()

                GeminiUsageWidget.updateAllWidgets(applicationContext)
            }
            cleanupAndStop()
        }
    }

    private fun cleanupAndStop() {
        handler.removeCallbacks(timeoutRunnable)
        headlessWebView?.apply {
            stopLoading()
            destroy()
        }
        headlessWebView = null
        stopSelf()
    }
}