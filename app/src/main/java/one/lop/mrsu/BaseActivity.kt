package one.lop.mrsu

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.lop.mrsu.model.User
import one.lop.mrsu.network.RetrofitClient
import one.lop.mrsu.util.TokenManager
import java.util.Locale

open class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navView: NavigationView
    protected lateinit var bottomNavigationView: BottomNavigationView
    protected lateinit var securePrefs: SharedPreferences
    private var toastAlreadyShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val accessToken = TokenManager.getAccessToken()
        Log.d("BaseActivity", "Access token on app start: $accessToken")

        if (accessToken.isNullOrEmpty()) {
            navigateToLogin()
        } else {
            Log.d("BaseActivity", "User is already logged in")
        }

        initPreferences()
        applyAppTheme()
        initializeUI()

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_schedule
        }

        fetchAndDisplayUserInfo()
    }

    private fun initializeUI() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setContentInsetsAbsolute(0, 0)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        navView.setNavigationItemSelectedListener(this)
        setupBottomNavigation()

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

    private fun updateThemeMenuUI() {
        val themeText = findViewById<TextView>(R.id.btn_theme_text)
        val themeIcon = findViewById<ImageView>(R.id.btn_theme_icon)

        val currentTheme = securePrefs.getString("theme", "light") ?: "light"
        themeText.text = when (currentTheme) {
            "light" -> getString(R.string.nav_theme_light)
            "dark" -> getString(R.string.nav_theme_dark)
            else -> getString(R.string.nav_theme_system)
        }

        themeIcon.setImageResource(
            when (currentTheme) {
                "light" -> R.drawable.baseline_brightness_high_24
                "dark" -> R.drawable.baseline_brightness_3_24
                else -> R.drawable.baseline_brightness_medium_24
            }
        )
    }


    private fun fetchAndDisplayUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiInstance.getUserInfo()
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    withContext(Dispatchers.Main) {
                        saveUserInfoToLocal(user)
                        updateUserHeader(user)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                Log.e("BaseActivity", "Error fetching user info: ${e.message}")
                withContext(Dispatchers.Main) {
                    navigateToLogin()
                }
            }
        }
    }

    private fun saveUserInfoToLocal(user: User) {
        securePrefs.edit().apply {
            putString("user_name", user.fio)
            putString("user_email", user.email)
            putString("user_photo_url", user.photo?.urlMedium)
            apply()
        }
    }

    private fun updateUserHeader(user: User) {
        val headerView = navView.getHeaderView(0)
        val userNameView = headerView.findViewById<TextView>(R.id.userName)
        val userEmailView = headerView.findViewById<TextView>(R.id.userEmail)
        val userPhotoView = headerView.findViewById<ImageView>(R.id.userPhoto)

        userNameView.text = user.fio
        userEmailView.text = user.email

        Glide.with(this)
            .load(user.photo?.urlMedium)
            .circleCrop()
            .placeholder(R.drawable.ic_user_placeholder)
            .error(R.drawable.ic_user_placeholder)
            .into(userPhotoView)
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_disciplines -> {
                    replaceFragment(DisciplineListFragment(), "Disciplines")
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

        bottomNavigationView.selectedItemId = R.id.nav_schedule
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_disciplines -> {
                replaceFragment(DisciplineListFragment(), "Disciplines")
            }
            R.id.nav_schedule -> {
                replaceFragment(ScheduleFragment(), "Schedule")
            }
            R.id.nav_attendance -> {
                openAttendanceModal()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun openAttendanceModal() {
        val attendanceDialog = AttendanceCodeDialog()
        attendanceDialog.show(supportFragmentManager, "AttendanceDialog")
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.content_frame)
        if (currentFragment != null && currentFragment::class.java == fragment::class.java) {
            return
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment, tag)
            .commit()
    }

    private fun applyAppTheme() {
        val theme = securePrefs.getString("theme", "light") ?: "light"
        val nightMode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun cycleAppTheme() {
        val newTheme = when (securePrefs.getString("theme", "light")) {
            "light" -> "dark"
            "dark" -> "system"
            else -> "light"
        }
        securePrefs.edit().putString("theme", newTheme).apply()
        recreate()
    }

    private fun toggleLanguage() {
        val newLanguage = if (securePrefs.getString("language", "ru") == "ru") "en" else "ru"
        setLocale(newLanguage)
        recreate()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)

        securePrefs.edit()
            .putString("language", languageCode)
            .apply()
    }

    private fun logout() {
        TokenManager.clearTokens()
        Log.d("BaseActivity", "Tokens cleared, navigating to login")

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        if (!toastAlreadyShown) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            toastAlreadyShown = true
            Handler(Looper.getMainLooper()).postDelayed({
                toastAlreadyShown = false
            }, 2000)
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
