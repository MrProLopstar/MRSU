package one.lop.mrsu.model

import java.util.*

data class TimeTable(
    val group: String,
    val planNumber: String,
    val facultyName: String,
    val timeTableBlockId: Int,
    val timeTable: DailyTimeTable
)

data class DailyTimeTable(
    val date: Date,
    val lessons: List<Lesson>
)

data class Lesson(
    val number: Int,
    val subgroupCount: Int,
    val disciplines: List<Discipline>
)

data class Discipline(
    val id: Int,
    val title: String,
    val language: String,
    val lessonType: Int,
    val remote: Boolean,
    val group: String,
    val subgroupNumber: Int,
    val teacher: Teacher,
    val auditorium: Auditorium
)

data class Teacher(
    val id: String,
    val userName: String,
    val fio: String,
    val photo: Photo
)

data class Auditorium(
    val id: Int,
    val number: String,
    val title: String,
    val campusId: Int,
    val campusTitle: String
)
