package one.lop.mrsu.model

import com.google.gson.annotations.SerializedName

data class DocFile(
    @SerializedName("Id")
    val id: String,

    @SerializedName("CreatorId")
    val creatorId: String,

    @SerializedName("Title")
    val title: String,

    @SerializedName("FileName")
    val fileName: String,

    @SerializedName("MIMEtype")
    val mimeType: String,

    @SerializedName("Size")
    val size: Int,

    @SerializedName("Date")
    val date: String,

    @SerializedName("URL")
    val url: String
)
