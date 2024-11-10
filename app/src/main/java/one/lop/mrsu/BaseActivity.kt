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
        loadLanguage()
        loadAppTheme()
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
    }

    private fun cycleAppTheme(themeItem: MenuItem) {
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                setAppTheme("dark")
                themeItem.title = getString(R.string.nav_theme_dark)
                themeItem.setIcon(R.drawable.baseline_brightness_3_24)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                setAppTheme("system")
                themeItem.title = getString(R.string.nav_theme_system)
                themeItem.setIcon(R.drawable.baseline_brightness_medium_24)
            }
            else -> {
                setAppTheme("light")
                themeItem.title = getString(R.string.nav_theme_light)
                themeItem.setIcon(R.drawable.baseline_brightness_high_24)
            }
        }
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
        val theme = getSharedPreferences("app_settings", MODE_PRIVATE)
            .getString("theme", "system")
        setAppTheme(theme ?: "system")
    }

    private fun toggleLanguage() {
        val newLanguage = if (Locale.getDefault().language == "ru") "en" else "ru"
        setLocale(newLanguage)
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
    }

    private fun loadLanguage() {
        val languageCode = getSharedPreferences("app_settings", MODE_PRIVATE)
            .getString("language", Locale.getDefault().language)
        setLocale(languageCode ?: Locale.getDefault().language)
    }

    private fun logout() {
        // Выполните логику выхода, если необходимо, и переходите на экран авторизации
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
