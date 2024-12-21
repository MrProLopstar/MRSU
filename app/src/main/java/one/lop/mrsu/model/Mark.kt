package one.lop.mrsu.model

import com.google.gson.annotations.SerializedName

data class Mark(
    @SerializedName("Id")
    val id: Int,

    @SerializedName("Ball")
    val ball: Double?,

    @SerializedName("CreatorId")
    val creatorId: String,

    @SerializedName("CreateDate")
    val createDate: String
)
