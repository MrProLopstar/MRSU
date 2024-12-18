package one.lop.mrsu.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import one.lop.mrsu.MyApplication
import one.lop.mrsu.util.TokenManager

class AuthInterceptor(instance: MyApplication) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = TokenManager.getAccessToken()
        Log.d("AuthInterceptor", "Access token loaded: $accessToken")

        val requestBuilder = originalRequest.newBuilder()
        if (!accessToken.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        var response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            synchronized(this) {
                val refreshToken = TokenManager.getRefreshToken()
                Log.d("AuthInterceptor", "Loaded refresh token: $refreshToken")
                if (!refreshToken.isNullOrEmpty()) {
                    val newAccessToken = refreshTokenWithApi(refreshToken)
                    if (!newAccessToken.isNullOrEmpty()) {
                        TokenManager.saveTokens(newAccessToken, refreshToken)
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newAccessToken")
                            .build()

                        response.close()
                        response = chain.proceed(newRequest)
                    } else {
                        Log.e("AuthInterceptor", "Failed to refresh token.")
                        TokenManager.clearTokens()
                    }
                } else {
                    Log.e("AuthInterceptor", "Refresh token is null or empty.")
                }
            }
        }

        return response
    }

    private fun refreshTokenWithApi(refreshToken: String): String? {
        return try {
            val refreshResponse = runBlocking {
                RetrofitClient.authInstance.refreshToken(
                    grantType = "refresh_token",
                    clientId = "8",
                    clientSecret = "qweasd",
                    refreshToken = refreshToken
                )
            }
            if (refreshResponse.isSuccessful) {
                refreshResponse.body()?.access_token
            } else {
                Log.e("AuthInterceptor", "Refresh token failed: ${refreshResponse.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error refreshing token: ${e.message}")
            null
        }
    }
}
