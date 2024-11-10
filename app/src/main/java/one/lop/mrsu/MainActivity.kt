package one.lop.mrsu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.lop.mrsu.model.User
import one.lop.mrsu.network.RetrofitClient

class MainActivity : BaseActivity() {
    private var toastAlreadyShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_main)

        // Настройка зашифрованного хранилища для токенов
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val securePrefs = EncryptedSharedPreferences.create(
            this,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Обычное SharedPreferences для сохранения данных пользователя
        val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val accessToken = securePrefs.getString("access_token", null)

        // Проверка токена доступа
        if (accessToken == null) {
            navigateToLogin()
        } else {
            checkAccessToken(accessToken, securePrefs, sharedPrefs)
        }
    }

    private fun checkAccessToken(accessToken: String, securePrefs: SharedPreferences, sharedPrefs: SharedPreferences) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiInstance.ping("Bearer $accessToken")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        loadUserProfile("Bearer $accessToken", sharedPrefs)
                    } else {
                        showToast(getString(R.string.error_token_expired))
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.error_network) + ": ${e.message}")
                    navigateToLogin()
                }
            }
        }
    }

    private fun loadUserProfile(accessToken: String, sharedPrefs: SharedPreferences) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiInstance.getUserInfo(accessToken)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        saveUserInfoToLocal(response.body()!!, sharedPrefs)
                    } else {
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.error_network))
                    navigateToLogin()
                }
            }
        }
    }

    private fun saveUserInfoToLocal(user: User, sharedPrefs: SharedPreferences) {
        with(sharedPrefs.edit()) {
            putString("user_name", user.FIO)
            putString("user_email", user.Email)
            putString("user_photo_url", user.Photo.UrlSmall)
            apply()
        }
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
}
