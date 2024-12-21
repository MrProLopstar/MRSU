package one.lop.mrsu.model

import com.google.gson.annotations.SerializedName

data class Section(
    @SerializedName("ControlDots")
    val controlDots: List<ControlDot>,

    @SerializedName("SectionType")
    val sectionType: Int,

    @SerializedName("Id")
    val id: Int,

    @SerializedName("Order")
    val order: Int,

    @SerializedName("Title")
    val title: String,

    @SerializedName("Description")
    val description: String?,

    @SerializedName("CreatorId")
    val creatorId: String,

    @SerializedName("CreateDate")
    val createDate: String
)
