package one.lop.mrsu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.lop.mrsu.model.User
import one.lop.mrsu.network.RetrofitClient

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAccessToken()
    }

    private fun checkAccessToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiInstance.ping()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("MainActivity", "Access token is valid. Loading profile.")
                        loadUserProfile()
                    } else {
                        Log.d("MainActivity", "Access token is invalid. Redirecting to login.")
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error checking access token: ${e.message}")
                withContext(Dispatchers.Main) {
                    navigateToLogin()
                }
            }
        }
    }

    private fun loadUserProfile() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiInstance.getUserInfo()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        saveUserInfoToLocal(response.body()!!)
                    } else {
                        Log.e("MainActivity", "Failed to load user profile. Redirecting to login.")
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error loading user profile: ${e.message}")
                withContext(Dispatchers.Main) {
                    navigateToLogin()
                }
            }
        }
    }

    private fun saveUserInfoToLocal(user: User) {
        // Сохранение данных пользователя локально
        Log.d("MainActivity", "User data saved: ${user.FIO}")
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
