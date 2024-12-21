package one.lop.mrsu

import android.app.Application
import one.lop.mrsu.util.TokenManager

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        TokenManager.init(this)
        android.util.Log.d("MyApplication", "Application created, TokenManager initialized")
    }

    fun isLoggedIn(): Boolean {
        val accessToken = TokenManager.getAccessToken()
        val refreshToken = TokenManager.getRefreshToken()
        return !accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()
    }

}
