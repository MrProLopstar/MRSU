package one.lop.mrsu.network

import one.lop.mrsu.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("v1/Ping")
    suspend fun ping(@Header("Authorization") token: String): Response<Void>

    @GET("v1/User")
    suspend fun getUserInfo(@Header("Authorization") token: String): Response<User>
}
