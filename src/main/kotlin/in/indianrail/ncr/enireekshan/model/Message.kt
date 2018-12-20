package `in`.indianrail.ncr.enireekshan.model

import `in`.indianrail.ncr.enireekshan.dao.UserEntity

data class MessageModel(
        val message: String,
        val sender: UserModel,
        val observationID: Int,
        val timestamp: Long
)

data class MessageCreateModel(
        val message: String,
        val timestamp: Long
)