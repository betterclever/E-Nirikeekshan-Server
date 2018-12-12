package `in`.indianrail.ncr.enireekshan.model

val STATUS_UNSEEN = "unseen"
val STATUS_SEEN = "seen"
val STATUS_COMPLIED = "complied"

data class ObservationStatusUpdateModel(
        val status: String,
        val notificationID: Int,
        val senderID: Long
)
data class ObservationAssigneesModel(
        val status: String,
        val assignedUser: Long
)