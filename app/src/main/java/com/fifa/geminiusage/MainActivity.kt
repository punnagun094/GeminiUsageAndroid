package com.fifa.geminiusage

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private val targetUrl = "https://gemini.google.com/usage?hl=th"

    private var touchStartX = 0f
    private var touchStartY = 0f
    private var lastTouchY = 0f
    private var isDraggingScroll = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
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
                    tvStatus.text = "กรุณายืนยันตัวตน / ล็อกอินในหน้าเว็บ"
                    return
                }

                if (currentUrl.contains("gemini.google.com/usage")) {
                    webContainer.visibility = View.GONE
                    tvStatus.text = "กำลังอ่านค่าจากหน้าเว็บ..."
                    Handler(Looper.getMainLooper()).postDelayed({
                        scrapeUsageData()
                    }, 2500)
                }
            }
        }

        btnRefresh.setOnClickListener {
            tvStatus.text = "กำลังอัปเดตข้อมูลล่าสุด..."
            webView.loadUrl(targetUrl)
        }

        btnToggleWeb.setOnClickListener {
            webContainer.visibility = View.VISIBLE
            webView.loadUrl(targetUrl)
        }

        btnCloseWeb.setOnClickListener {
            webContainer.visibility = View.GONE
            scrapeUsageData()
        }

        webView.loadUrl(targetUrl)
    }

    override fun onResume() {
        super.onResume()
        applyDisplayAdaptiveLayout()
        applyTitleGradient()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyDisplayAdaptiveLayout()
        applyTitleGradient()
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
        tvFiveHrSub.text = resetLines.getOrNull(0) ?: "กำลังรอเวลารีเซ็ต..."
        pbAppFiveHr.progress = fiveHrPct.coerceIn(0, 100)

        tvWeeklyMain.text = if (pctMatches.size >= 2) "$weeklyPct%" else "--%"
        tvWeeklySub.text = resetLines.getOrNull(1) ?: "กำลังรอเวลารีเซ็ต..."
        pbAppWeekly.progress = weeklyPct.coerceIn(0, 100)
    }

    private fun scrapeUsageData() {
        val jsCode = """
            (function() {
                const text = document.body.innerText || "";
                const lines = text.split('\n').map(l => l.trim()).filter(l => l.length > 0);
                const matched = lines.filter(l => 
                    l.includes('%') || l.includes('รีเซ็ต') || l.includes('Reset') || l.includes('ชั่วโมง') || l.includes('สัปดาห์')
                );
                return matched.length > 0 ? matched.join('\n') : text.substring(0, 400);
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode) { rawResult ->
            val cleanText = (JSONTokener(rawResult).nextValue() as? String)?.trim() ?: ""

            if (cleanText.contains("ไม่ใช่โรบอต") || cleanText.contains("ผิดปกติ") || cleanText.contains("unusual traffic", ignoreCase = true)) {
                webContainer.visibility = View.VISIBLE
                tvStatus.text = "กรุณาติ๊กยืนยัน 'ไม่ใช่โรบอต' ในหน้าเว็บ"
                return@evaluateJavascript
            }

            if (Regex("""\d+\s*%""").containsMatchIn(cleanText)) {
                val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                renderQuotaUI(cleanText)
                tvStatus.text = "อัปเดตล่าสุดเมื่อ $timeNow"

                getSharedPreferences("gemini_usage_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("last_quota", cleanText)
                    .putString("last_time", timeNow)
                    .apply()

                GeminiUsageWidget.updateAllWidgets(this)
            } else {
                tvStatus.text = "ยังดึงค่าไม่ได้ (คงค่าเดิมไว้) ลองกด 'หน้าล็อกอิน'"
            }
        }
    }
}