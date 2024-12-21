package one.lop.mrsu.network

import AuthService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import one.lop.mrsu.MyApplication
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val AUTH_BASE_URL = "https://p.mrsu.ru/"
    private const val API_BASE_URL = "https://papi.mrsu.ru/"

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(MyApplication.instance))
            .build()
    }

    val authInstance: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }

    val apiInstance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
