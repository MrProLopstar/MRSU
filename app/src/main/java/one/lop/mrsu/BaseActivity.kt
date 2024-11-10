package one.lop.mrsu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private var toastAlreadyShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
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

        setupNavigationView()
        loadAppTheme()
        loadLanguage()
    }

    fun updateUserHeader(sharedPrefs: SharedPreferences) {
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
                .apply(RequestOptions.circleCropTransform())
                .into(userImageView)
        }
    }

    protected fun setContentLayout(layoutResID: Int) {
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)
        LayoutInflater.from(this).inflate(layoutResID, contentFrame, true)
    }

    private fun setupNavigationView() {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    true
                }
                R.id.nav_language -> {
                    toggleLanguage()
                    true
                }
                R.id.nav_theme -> {
                    cycleAppTheme(menuItem)
                    true
                }
                else -> false
            }
        }
        updateThemeMenuItem(navView.menu.findItem(R.id.nav_theme))
    }

    private fun cycleAppTheme(themeItem: MenuItem) {
        val newTheme = when (getAppTheme()) {
            "light" -> "dark"
            "dark" -> "system"
            else -> "light"
        }
        setAppTheme(newTheme)
        updateThemeMenuItem(themeItem) // Обновляем отображение темы в меню сразу
    }

    private fun setAppTheme(theme: String) {
        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)

        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putString("theme", theme)
            .apply()
    }

    private fun loadAppTheme() {
        val theme = getAppTheme()
        setAppTheme(theme)
    }

    private fun getAppTheme(): String {
        return getSharedPreferences("app_settings", MODE_PRIVATE)
            .getString("theme", "system") ?: "system"
    }

    private fun updateThemeMenuItem(themeItem: MenuItem) {
        val theme = getAppTheme()
        when (theme) {
            "light" -> {
                themeItem.title = getString(R.string.nav_theme_light)
                themeItem.setIcon(R.drawable.baseline_brightness_high_24)
            }
            "dark" -> {
                themeItem.title = getString(R.string.nav_theme_dark)
                themeItem.setIcon(R.drawable.baseline_brightness_3_24)
            }
            else -> {
                themeItem.title = getString(R.string.nav_theme_system)
                themeItem.setIcon(R.drawable.baseline_brightness_medium_24)
            }
        }
    }

    private fun toggleLanguage() {
        val newLanguage = if (Locale.getDefault().language == "ru") "en" else "ru"
        setLocale(newLanguage)
        updateNavigationViewText()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putString("language", languageCode)
            .apply()
        updateNavigationViewText() // Обновляем отображение текста
    }

    private fun updateNavigationViewText() {
        navView.menu.findItem(R.id.nav_logout).title = getString(R.string.nav_logout)
        navView.menu.findItem(R.id.nav_language).title = getString(R.string.nav_language)
        navView.menu.findItem(R.id.nav_theme).title = getString(R.string.nav_theme)
    }

    private fun loadLanguage() {
        val languageCode = getSharedPreferences("app_settings", MODE_PRIVATE)
            .getString("language", Locale.getDefault().language)
        setLocale(languageCode ?: Locale.getDefault().language)
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
