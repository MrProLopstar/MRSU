package one.lop.mrsu.network

import one.lop.mrsu.model.Group
import one.lop.mrsu.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("v1/Ping")
    suspend fun ping(): Response<Void>

    @GET("v1/User")
    suspend fun getUserInfo(): Response<User>

    @GET("v1/StudentTimeTable")
    suspend fun getStudentTimeTable(
        @Query("date") date: String
    ): Response<List<Group>>

}
