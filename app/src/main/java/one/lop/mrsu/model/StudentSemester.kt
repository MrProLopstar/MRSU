package one.lop.mrsu.model

import com.google.gson.annotations.SerializedName

data class StudentSemester(
    @SerializedName("RecordBooks")
    val recordBooks: List<RecordBook>,

    @SerializedName("UnreadedDisCount")
    val unreadedDisCount: Int,

    @SerializedName("UnreadedDisMesCount")
    val unreadedDisMesCount: Int,

    @SerializedName("Year")
    val year: String,

    @SerializedName("Period")
    val period: Int
)

data class RecordBook(
    @SerializedName("Cod")
    val cod: String,

    @SerializedName("Number")
    val number: String,

    @SerializedName("Faculty")
    val faculty: String,

    @SerializedName("Disciplines")
    val disciplines: List<Discipline>
)
