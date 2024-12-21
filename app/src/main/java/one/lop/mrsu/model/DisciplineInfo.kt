package one.lop.mrsu.model

import com.google.gson.annotations.SerializedName

data class DisciplineInfo(
    @SerializedName("Relevance")
    val relevance: Boolean,
    @SerializedName("IsTeacher")
    val isTeacher: Boolean,
    @SerializedName("UnreadedCount")
    val unreadedCount: Int,
    @SerializedName("UnreadedMessageCount")
    val unreadedMessageCount: Int,
    @SerializedName("Groups")
    val groups: List<String>?,
    @SerializedName("DocFiles")
    val docFiles: List<DocFile>,
    @SerializedName("WorkingProgramm")
    val workingProgramm: WorkingProgram?,
    @SerializedName("Id")
    val id: Int,
    @SerializedName("PlanNumber")
    val planNumber: String,
    @SerializedName("Year")
    val year: String,
    @SerializedName("Faculty")
    val faculty: String,
    @SerializedName("EducationForm")
    val educationForm: String,
    @SerializedName("EducationLevel")
    val educationLevel: String,
    @SerializedName("Specialty")
    val specialty: String,
    @SerializedName("SpecialtyCod")
    val specialtyCod: String,
    @SerializedName("Profile")
    val profile: String,
    @SerializedName("PeriodString")
    val periodString: String,
    @SerializedName("PeriodInt")
    val periodInt: Int,
    @SerializedName("Title")
    val title: String,
    @SerializedName("Language")
    val language: String?
)

data class WorkingProgram(
    @SerializedName("key1")
    val key1: String?,
    @SerializedName("key2")
    val key2: String?
)
