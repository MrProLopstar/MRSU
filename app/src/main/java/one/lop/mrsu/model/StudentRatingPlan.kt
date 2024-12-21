package one.lop.mrsu.model

import com.google.gson.annotations.SerializedName

data class StudentRatingPlan(
    @SerializedName("MarkZeroSession")
    val markZeroSession: Mark?,

    @SerializedName("Sections")
    val sections: List<Section>
)
