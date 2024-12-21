package one.lop.mrsu.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("Id")
    val id: String? = null,

    @SerializedName("Email")
    val email: String? = null,

    @SerializedName("FIO")
    val fio: String? = null,

    @SerializedName("EnglishFIO")
    val englishFio: String? = null,

    @SerializedName("StudentCode")
    val studentCode: String? = null,

    @SerializedName("BirthDate")
    val birthDate: String? = null,

    @SerializedName("UserName")
    val userName: String? = null,

    @SerializedName("Photo")
    val photo: Photo? = null
)
