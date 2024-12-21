package one.lop.mrsu.model

import com.google.gson.annotations.SerializedName

data class Report(
    @SerializedName("Id")
    val id: Int,

    @SerializedName("CreateDate")
    val createDate: String,

    @SerializedName("DocFile")
    val docFile: DocFile
)
