package one.lop.mrsu.network

import one.lop.mrsu.model.User
import one.lop.mrsu.model.TimeTable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("v1/Ping")
    suspend fun ping(@Header("Authorization") token: String): Response<Void>

    @GET("v1/User")
    suspend fun getUserInfo(@Header("Authorization") token: String): Response<User>

    @GET("v2/StudentTimeTable")
    suspend fun getStudentTimeTable(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): Response<List<TimeTable>>
}
