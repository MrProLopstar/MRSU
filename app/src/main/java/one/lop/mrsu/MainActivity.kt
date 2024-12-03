package one.lop.mrsu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.lop.mrsu.model.User
import one.lop.mrsu.network.RetrofitClient

class MainActivity : BaseActivity() {

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // BaseActivity уже устанавливает содержимое и управляет фрагментами

        initPreferences()

        val accessToken = securePrefs.getString("access_token", null)
        Log.d("MainActivity", "Access token: $accessToken")

        if (accessToken.isNullOrEmpty()) {
            Log.d("MainActivity", "Токен отсутствует, переход на экран логина.")
            navigateToLogin()
        } else {
            checkAccessToken(accessToken)
        }
    }

    private fun initPreferences() {
        sharedPrefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    }

    private fun checkAccessToken(accessToken: String) {
        if (isFinishing || isDestroyed) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiInstance.ping("Bearer $accessToken")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("MainActivity", "Токен действителен, загружаем профиль.")
                        loadUserProfile("Bearer $accessToken")
                    } else {
                        Log.d("MainActivity", "Токен истек, переход на экран логина.")
                        showToast(getString(R.string.error_token_expired))
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка при проверке токена: ${e.message}")
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.error_network) + ": ${e.message}")
                    navigateToLogin()
                }
            }
        }
    }

    private fun loadUserProfile(accessToken: String) {
        if (isFinishing || isDestroyed) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiInstance.getUserInfo(accessToken)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        saveUserInfoToLocal(response.body()!!)
                        if (!isFinishing && !isDestroyed) updateUserHeader(sharedPrefs)
                    } else {
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка при загрузке профиля: ${e.message}")
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.error_network))
                    navigateToLogin()
                }
            }
        }
    }

    private fun saveUserInfoToLocal(user: User) {
        with(sharedPrefs.edit()) {
            putString("user_name", user.FIO)
            putString("user_email", user.Email)
            putString("user_photo_url", user.Photo.UrlSmall)
            apply()
        }
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
