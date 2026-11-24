package com.fifa.geminiusage

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import org.json.JSONTokener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var webContainer: FrameLayout
    private lateinit var mainScrollView: ScrollView
    private lateinit var contentContainer: LinearLayout
    private lateinit var glassCard: LinearLayout
    private lateinit var tvAppTitle: TextView
    private lateinit var tvQuotaResult: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvFiveHrMain: TextView
    private lateinit var tvFiveHrSub: TextView
    private lateinit var pbAppFiveHr: ProgressBar
    private lateinit var tvWeeklyMain: TextView
    private lateinit var tvWeeklySub: TextView
    private lateinit var pbAppWeekly: ProgressBar

    private var touchStartX = 0f
    private var touchStartY = 0f
    private var lastTouchY = 0f
    private var isDraggingScroll = false

    private fun isThaiLocale(): Boolean {
        val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.takeIf { !it.isEmpty }?.get(0) ?: Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }
        return currentLocale.language.equals("th", ignoreCase = true)
    }

    private fun getTargetUrl(): String {
        val langParam = if (isThaiLocale()) "th" else "en"
        return "https://gemini.google.com/usage?hl=$langParam"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        applySystemBarAppearance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        webContainer = findViewById(R.id.webContainer)
        mainScrollView = findViewById(R.id.mainScrollView)
        contentContainer = findViewById(R.id.contentContainer)
        glassCard = findViewById(R.id.glassCard)
        tvAppTitle = findViewById(R.id.tvAppTitle)
        tvQuotaResult = findViewById(R.id.tvQuotaResult)
        tvStatus = findViewById(R.id.tvStatus)
        tvFiveHrMain = findViewById(R.id.tvFiveHrMain)
        tvFiveHrSub = findViewById(R.id.tvFiveHrSub)
        pbAppFiveHr = findViewById(R.id.pbAppFiveHr)
        tvWeeklyMain = findViewById(R.id.tvWeeklyMain)
        tvWeeklySub = findViewById(R.id.tvWeeklySub)
        pbAppWeekly = findViewById(R.id.pbAppWeekly)

        val btnRefresh: TextView = findViewById(R.id.btnRefresh)
        val btnToggleWeb: TextView = findViewById(R.id.btnToggleWeb)
        val btnCloseWeb: TextView = findViewById(R.id.btnCloseWeb)

        // ปรับระยะขอบอัตโนมัติตามหน้าจอ (จอหน้า vs จอหลัง)
        applyDisplayAdaptiveLayout()

        // ไล่สีข้อความ Gemini Usage จาก Indigo ไป Sky Blue (ปรับสีอัตโนมัติตาม Light/Dark Mode)
        applyTitleGradient()

        val prefs = getSharedPreferences("gemini_usage_prefs", MODE_PRIVATE)
        val savedText = prefs.getString("last_quota", null)
        if (savedText != null) {
            renderQuotaUI(savedText)
        }

        UsageAlarmReceiver.scheduleNextSync(this)

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString = userAgentString.replace("; wv", "")
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val currentUrl = url ?: return

                if (currentUrl.contains("accounts.google.com") || currentUrl.contains("/sorry/")) {
                    webContainer.visibility = View.VISIBLE
                    tvStatus.text = getString(R.string.status_login_required)
                    return
                }

                if (currentUrl.contains("gemini.google.com/usage")) {
                    webContainer.visibility = View.GONE
                    tvStatus.text = getString(R.string.status_reading_page)
                    Handler(Looper.getMainLooper()).postDelayed({
                        scrapeUsageData()
                    }, 2500)
                }
            }
        }

        btnRefresh.setOnClickListener {
            tvStatus.text = getString(R.string.status_updating)
            webView.loadUrl(getTargetUrl())
        }

        btnToggleWeb.setOnClickListener {
            webContainer.visibility = View.VISIBLE
            webView.loadUrl(getTargetUrl())
        }

        btnCloseWeb.setOnClickListener {
            webContainer.visibility = View.GONE
            scrapeUsageData()
        }

        webView.loadUrl(getTargetUrl())
    }

    override fun onResume() {
        super.onResume()
        applySystemBarAppearance()
        applyDisplayAdaptiveLayout()
        applyTitleGradient()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySystemBarAppearance()
        applyDisplayAdaptiveLayout()
        applyTitleGradient()
    }

    // ปรับสีไอคอน Status Bar และ Navigation Bar ให้ตัดกับพื้นหลังทั้งโหมดสว่างและโหมดมืด
    private fun applySystemBarAppearance() {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
            )
        } else {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkMode
            isAppearanceLightNavigationBars = !isDarkMode
        }
    }

    private fun applyTitleGradient() {
        tvAppTitle.post {
            val width = tvAppTitle.paint.measureText(tvAppTitle.text.toString())
            val startColor = ContextCompat.getColor(this, R.color.title_grad_start)
            val endColor = ContextCompat.getColor(this, R.color.title_grad_end)
            val gradient = LinearGradient(
                0f, 0f, width, tvAppTitle.textSize,
                intArrayOf(startColor, endColor),
                null,
                Shader.TileMode.CLAMP
            )
            tvAppTitle.paint.shader = gradient
            tvAppTitle.invalidate()
        }
    }

    // ตรวจจับจอหลัก (displayId == 0) vs จอหลัง (displayId != 0 หรือขนาดจอเล็ก)
    private fun applyDisplayAdaptiveLayout() {
        val currentDisplayId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.displayId ?: Display.DEFAULT_DISPLAY
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.displayId
        }

        val screenHeightPx = resources.displayMetrics.heightPixels
        val isRearScreen = currentDisplayId != Display.DEFAULT_DISPLAY || screenHeightPx < 1400

        if (isRearScreen) {
            // จอหลัง: ดันหัวข้อลงมา 88dp เพื่อหลบแนวบังขอบบนและกล้องหลัง
            contentContainer.setPadding(
                dpToPx(14),
                dpToPx(88),
                dpToPx(14),
                dpToPx(24)
            )
            glassCard.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        } else {
            // จอหลัก: ใช้ระยะขอบบนปกติ 44dp
            contentContainer.setPadding(
                dpToPx(20),
                dpToPx(44),
                dpToPx(20),
                dpToPx(32)
            )
            glassCard.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20))
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (webContainer.visibility == View.VISIBLE) {
            return super.dispatchTouchEvent(ev)
        }

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = ev.x
                touchStartY = ev.y
                lastTouchY = ev.y
                isDraggingScroll = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = abs(ev.x - touchStartX)
                val dyTotal = abs(ev.y - touchStartY)
                val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

                if (!isDraggingScroll && dyTotal > touchSlop && dyTotal >= dx) {
                    isDraggingScroll = true
                    val cancelEvent = MotionEvent.obtain(ev).apply {
                        action = MotionEvent.ACTION_CANCEL
                    }
                    super.dispatchTouchEvent(cancelEvent)
                    cancelEvent.recycle()
                }

                if (isDraggingScroll) {
                    val deltaY = ev.y - lastTouchY
                    mainScrollView.scrollBy(0, (-deltaY * 1.35f).toInt())
                    lastTouchY = ev.y
                    return true
                }
                lastTouchY = ev.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDraggingScroll) {
                    isDraggingScroll = false
                    return true
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun renderQuotaUI(cleanText: String) {
        tvQuotaResult.text = cleanText

        val pctMatches = Regex("""(\d+)\s*%""").findAll(cleanText).mapNotNull {
            it.groupValues[1].toIntOrNull()
        }.toList()

        val resetLines = cleanText.lines()
            .map { it.trim() }
            .filter { it.contains("รีเซ็ต") || it.contains("Reset", ignoreCase = true) }

        val fiveHrPct = pctMatches.getOrNull(0) ?: 0
        val weeklyPct = pctMatches.getOrNull(1) ?: 0

        tvFiveHrMain.text = if (pctMatches.isNotEmpty()) "$fiveHrPct%" else "--%"
        tvFiveHrSub.text = resetLines.getOrNull(0) ?: getString(R.string.status_waiting_reset)
        pbAppFiveHr.progress = fiveHrPct.coerceIn(0, 100)

        tvWeeklyMain.text = if (pctMatches.size >= 2) "$weeklyPct%" else "--%"
        tvWeeklySub.text = resetLines.getOrNull(1) ?: getString(R.string.status_waiting_reset)
        pbAppWeekly.progress = weeklyPct.coerceIn(0, 100)
    }

    private fun scrapeUsageData() {
        val jsCode = """
            (function() {
                const text = document.body.innerText || "";
                const lines = text.split('\n').map(l => l.trim()).filter(l => l.length > 0);
                const matched = lines.filter(l => {
                    const lower = l.toLowerCase();
                    return l.includes('%') ||
                        l.includes('รีเซ็ต') || lower.includes('reset') ||
                        l.includes('ชั่วโมง') || lower.includes('hour') ||
                        l.includes('สัปดาห์') || lower.includes('week');
                });
                return matched.length > 0 ? matched.join('\n') : text.substring(0, 400);
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode) { rawResult ->
            val cleanText = (JSONTokener(rawResult).nextValue() as? String)?.trim() ?: ""

            if (
                cleanText.contains("ไม่ใช่โรบอต") ||
                cleanText.contains("not a robot", ignoreCase = true) ||
                cleanText.contains("ผิดปกติ") ||
                cleanText.contains("unusual traffic", ignoreCase = true)
            ) {
                webContainer.visibility = View.VISIBLE
                tvStatus.text = getString(R.string.status_captcha_required)
                return@evaluateJavascript
            }

            if (Regex("""\d+\s*%""").containsMatchIn(cleanText)) {
                val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                renderQuotaUI(cleanText)
                tvStatus.text = getString(R.string.status_last_updated, timeNow)

                getSharedPreferences("gemini_usage_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("last_quota", cleanText)
                    .putString("last_time", timeNow)
                    .apply()

                GeminiUsageWidget.updateAllWidgets(this)
            } else {
                tvStatus.text = getString(R.string.status_fetch_failed)
            }
        }
    }
}