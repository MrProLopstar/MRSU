package one.lop.mrsu.model
import com.google.gson.annotations.SerializedName
import one.lop.mrsu.model.Photo

data class Group(
    @SerializedName("Group")
    val group: String,

    @SerializedName("PlanNumber")
    val planNumber: String,

    @SerializedName("FacultyName")
    val facultyName: String,

    @SerializedName("TimeTableBlockd")
    val timeTableBlockd: Int,

    @SerializedName("TimeTable")
    val timeTable: DailyTimeTable?
)

data class DailyTimeTable(
    @SerializedName("Date")
    val date: String,

    @SerializedName("Lessons")
    val lessons: List<Lesson>
)


data class Lesson(
    @SerializedName("Number")
    val number: Int,

    @SerializedName("SubgroupCount")
    val subgroupCount: Int,

    @SerializedName("Disciplines")
    val disciplines: List<Discipline>
)

data class Discipline(
    @SerializedName("Id")
    val id: Int,

    @SerializedName("Title")
    val title: String,

    @SerializedName("Language")
    val language: String?,

    @SerializedName("LessonType")
    val lessonType: Int,

    @SerializedName("Remote")
    val remote: Boolean,

    @SerializedName("Group")
    val group: String,

    @SerializedName("SubgroupNumber")
    val subgroupNumber: Int,

    @SerializedName("Teacher")
    val teacher: Teacher?,

    @SerializedName("Auditorium")
    val auditorium: Auditorium?
)

data class Teacher(
    @SerializedName("Id")
    val id: String,

    @SerializedName("UserName")
    val userName: String,

    @SerializedName("FIO")
    var fio: String,

    @SerializedName("Photo")
    val photo: Photo?
)

data class Auditorium(
    @SerializedName("Id")
    val id: Int,

    @SerializedName("Number")
    val number: String,

    @SerializedName("Title")
    val title: String,

    @SerializedName("CampusId")
    val campusId: Int,

    @SerializedName("CampusTitle")
    val campusTitle: String
)