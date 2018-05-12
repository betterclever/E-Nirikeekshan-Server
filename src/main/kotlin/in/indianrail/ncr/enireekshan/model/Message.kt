package `in`.indianrail.ncr.enireekshan.model

data class MessageModel(
    val message: String,
    val sender: UserModel,
    val inspectionID: Int
)