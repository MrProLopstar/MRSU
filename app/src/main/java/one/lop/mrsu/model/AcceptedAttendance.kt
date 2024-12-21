package one.lop.mrsu.model

data class AcceptedAttendance(
    val disciplineId: Int,
    val disciplineTitle: String,
    val date: String,
    val teacher: UserCrop
)

data class UserCrop(
    val id: String,
    val userName: String,
    val fio: String,
    val photo: Photo
)
