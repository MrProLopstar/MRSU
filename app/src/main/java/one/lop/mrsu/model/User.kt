package one.lop.mrsu.model

data class User(
    val Id: String,
    val Email: String,
    val FIO: String,
    val EnglishFIO: String,
    val StudentCode: String,
    val BirthDate: String,
    val UserName: String,
    val Photo: Photo
)

data class Photo(
    val UrlSmall: String,
    val UrlMedium: String,
    val UrlSource: String
)