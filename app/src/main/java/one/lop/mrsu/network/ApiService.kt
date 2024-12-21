package one.lop.mrsu.network

import one.lop.mrsu.model.AcceptedAttendance
import one.lop.mrsu.model.DisciplineInfo
import one.lop.mrsu.model.StudentRatingPlan
import one.lop.mrsu.model.Group
import one.lop.mrsu.model.StudentSemester
import one.lop.mrsu.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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

    @GET("v1/Discipline/{id}")
    suspend fun getDiscipline(
        @Path("id") id: Int
    ): Response<DisciplineInfo>

    @GET("v2/StudentRatingPlan/{id}")
    suspend fun getStudentRatingPlan(
        @Path("id") id: Int
    ): Response<StudentRatingPlan>

    @GET("v1/StudentSemester")
    suspend fun getStudentSemester(
        @Query("selector") selector: String = "current"
    ): Response<StudentSemester>

    @POST("v1/StudentAttendanceCode")
    suspend fun postStudentAttendanceCode(
        @Query("code") code: String
    ): Response<AcceptedAttendance>
}
