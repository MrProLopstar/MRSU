package one.lop.mrsu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private var toastAlreadyShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        findViewById<LinearLayout>(R.id.btn_language).setOnClickListener {
            toggleLanguage()
        }

        findViewById<LinearLayout>(R.id.btn_theme).setOnClickListener {
            cycleAppTheme()
        }

        findViewById<LinearLayout>(R.id.btn_logout).setOnClickListener {
            logout()
        }

        updateBottomMenu()
        updateThemeMenuUI()
    }

    private fun applyAppTheme() {
        val theme = getAppTheme()
        setAppTheme(theme)
    }

    protected fun setContentLayout(layoutResID: Int) {
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)
        LayoutInflater.from(this).inflate(layoutResID, contentFrame, true)
    }

    fun updateUserHeader(sharedPrefs: SharedPreferences) {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val userNameView = headerView.findViewById<TextView>(R.id.userName)
        val userEmailView = headerView.findViewById<TextView>(R.id.userEmail)
        val userImageView = headerView.findViewById<ImageView>(R.id.userPhoto)

        val userName = sharedPrefs.getString("user_name", null)
        val userEmail = sharedPrefs.getString("user_email", null)
        val userPhotoUrl = sharedPrefs.getString("user_photo_url", null)

        userName?.let { userNameView.text = it }
        userEmail?.let { userEmailView.text = it }

        userPhotoUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .circleCrop()
                .placeholder(R.drawable.ic_user_placeholder) // Иконка-заглушка
                .error(R.drawable.ic_user_placeholder) // Иконка на случай ошибки
                .into(userImageView)
        }
    }

    private fun updateBottomMenu() {
        findViewById<TextView>(R.id.btn_language_text).text = getString(R.string.nav_language)
        val themeText = findViewById<TextView>(R.id.btn_theme_text)
        themeText.text = when (getAppTheme()) {
            "light" -> getString(R.string.nav_theme_light)
            "dark" -> getString(R.string.nav_theme_dark)
            else -> getString(R.string.nav_theme_system)
        }
    }

    private fun cycleAppTheme() {
        val newTheme = when (getAppTheme()) {
            "light" -> "dark"
            "dark" -> "system"
            else -> "light"
        }
        setAppTheme(newTheme)
        updateThemeMenuUI()
        recreate()
    }

    private fun updateThemeMenuUI() {
        val themeText = findViewById<TextView>(R.id.btn_theme_text)
        val themeIcon = findViewById<ImageView>(R.id.btn_theme_icon)
        val bottomSection = findViewById<LinearLayout>(R.id.nav_bottom_section)

        themeText.text = when (getAppTheme()) {
            "light" -> getString(R.string.nav_theme_light)
            "dark" -> getString(R.string.nav_theme_dark)
            else -> getString(R.string.nav_theme_system)
        }

        themeIcon.setImageResource(
            when (getAppTheme()) {
                "light" -> R.drawable.baseline_brightness_high_24
                "dark" -> R.drawable.baseline_brightness_3_24
                else -> R.drawable.baseline_brightness_medium_24
            }
        )

        // Обновление цвета нижней секции
        val backgroundColor = when (getAppTheme()) {
            "light" -> R.color.white
            "dark" -> R.color.black
            else -> R.color.white
        }
        bottomSection.setBackgroundColor(resources.getColor(backgroundColor, theme))
    }

    private fun setAppTheme(theme: String) {
        val nightMode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("theme", theme)
            .apply()
    }

    private fun getAppTheme(): String {
        return getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .getString("theme", "system") ?: "system"
    }

    private fun toggleLanguage() {
        val newLanguage = if (getSavedLanguage() == "ru") "en" else "ru"
        setLocale(newLanguage)
        recreate()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        // Создаем новый контекст с обновленной конфигурацией
        val context = createConfigurationContext(config)

        // Обновляем ресурсы приложения
        resources.updateConfiguration(context.resources.configuration, context.resources.displayMetrics)

        // Сохраняем выбранный язык в SharedPreferences
        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putString("language", languageCode)
            .apply()
    }

    private fun getSavedLanguage(): String {
        return getSharedPreferences("app_settings", MODE_PRIVATE)
            .getString("language", Locale.getDefault().language) ?: Locale.getDefault().language
    }

    private fun logout() {
        showToast(getString(R.string.logout_message))
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    fun showToast(message: String) {
        if (!toastAlreadyShown) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            toastAlreadyShown = true
            Handler(Looper.getMainLooper()).postDelayed({
                toastAlreadyShown = false
            }, 2000)
        }
    }
}
