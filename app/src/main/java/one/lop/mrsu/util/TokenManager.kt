package one.lop.mrsu.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object TokenManager {
    private const val PREFS_NAME = "token_prefs"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        Log.d("TokenManager", "Saving tokens: access=$accessToken, refresh=$refreshToken")
        prefs.edit().apply {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            apply() // Asynchronous commit
        }
        Log.d("TokenManager", "Tokens saved successfully")
    }

    fun getAccessToken(): String? {
        val token = prefs.getString(ACCESS_TOKEN_KEY, null)
        Log.d("TokenManager", "Retrieved access token: $token")
        return token
    }

    fun getRefreshToken(): String? = prefs.getString(REFRESH_TOKEN_KEY, null)

    fun clearTokens() {
        prefs.edit().apply {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            apply()
        }
    }
}
