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
        TokenManager.init(this) // Инициализация TokenManager с application context
    }
}
