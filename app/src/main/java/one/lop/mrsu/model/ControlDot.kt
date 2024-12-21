package one.lop.mrsu.model

import com.google.gson.annotations.SerializedName

data class ControlDot(
    @SerializedName("Mark")
    val mark: Mark?,

    @SerializedName("Report")
    val report: Report?,

    @SerializedName("Id")
    val id: Int,

    @SerializedName("Order")
    val order: Int,

    @SerializedName("Title")
    val title: String,

    @SerializedName("Date")
    val date: String,

    @SerializedName("MaxBall")
    val maxBall: Double,

    @SerializedName("IsReport")
    val isReport: Boolean,

    @SerializedName("IsCredit")
    val isCredit: Boolean,

    @SerializedName("CreatorId")
    val creatorId: String,

    @SerializedName("CreateDate")
    val createDate: String
)
