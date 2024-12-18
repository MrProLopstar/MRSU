package one.lop.mrsu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.Locale
import android.util.Log

open class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navView: NavigationView
    protected lateinit var bottomNavigationView: BottomNavigationView
    protected lateinit var securePrefs: SharedPreferences
    private var toastAlreadyShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Вызов super первым
        setContentView(R.layout.activity_base) // Установка содержимого

        initPreferences() // Инициализация securePrefs
        applyAppTheme() // Применение темы после инициализации securePrefs
        initializeUI() // Инициализация UI компонентов

        // Устанавливаем начальный фрагмент, если это не восстановление состояния
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
        }
    }

    private fun initializeUI() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
        setupBottomNavigation()

        // Обработчики для бокового меню
        findViewById<LinearLayout>(R.id.btn_language).setOnClickListener {
            toggleLanguage()
        }

        findViewById<LinearLayout>(R.id.btn_theme).setOnClickListener {
            cycleAppTheme()
        }

        findViewById<LinearLayout>(R.id.btn_logout).setOnClickListener {
            logout()
        }

        updateThemeMenuUI()
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment(), "Home")
                    true
                }
                R.id.nav_schedule -> {
                    replaceFragment(ScheduleFragment(), "Schedule")
                    true
                }
                R.id.nav_settings -> {
                    showToast("Настройки пока не реализованы")
                    true
                }
                else -> false
            }
        }

        // Сброс фрагмента при повторном выборе вкладки
        bottomNavigationView.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val fragment = supportFragmentManager.findFragmentByTag("Home")
                    if (fragment is HomeFragment) {
                        fragment.scrollToTop()
                    }
                }
                R.id.nav_schedule -> {
                    val fragment = supportFragmentManager.findFragmentByTag("Schedule")
                    if (fragment is ScheduleFragment) {
                        //fragment.scrollToTop()
                    }
                }
                // Добавьте обработку других пунктов по необходимости
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                replaceFragment(HomeFragment(), "Home")
            }
            R.id.nav_schedule -> {
                replaceFragment(ScheduleFragment(), "Schedule")
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        val currentFragment = getCurrentFragment()
        if (currentFragment != null && currentFragment::class.java == fragment::class.java) {
            // Фрагмент уже отображается, ничего не делаем
            return
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment, tag)
            .commit()
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.content_frame)
    }

    private fun applyAppTheme() {
        val theme = getAppTheme()
        Log.d("BaseActivity", "Applying theme: $theme")
        setAppTheme(theme)
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
    }

    private fun setAppTheme(theme: String) {
        val nightMode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        securePrefs.edit()
            .putString("theme", theme)
            .apply()
    }

    private fun getAppTheme(): String {
        val theme = securePrefs.getString("theme", "system") ?: "system"
        Log.d("BaseActivity", "Retrieved theme: $theme")
        return theme
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

        val context = createConfigurationContext(config)
        resources.updateConfiguration(context.resources.configuration, context.resources.displayMetrics)

        securePrefs.edit()
            .putString("language", languageCode)
            .apply()
    }

    private fun getSavedLanguage(): String {
        return securePrefs.getString("language", Locale.getDefault().language) ?: Locale.getDefault().language
    }

    private fun logout() {
        showToast(getString(R.string.logout_message))
        navigateToLogin()
    }

    open fun navigateToLogin() {
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

    private fun initPreferences() {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        securePrefs = EncryptedSharedPreferences.create(
            this,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
