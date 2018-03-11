package `in`.indianrail.ncr.enireekshan.model

data class InspectionModel(
        val title : String,
        val status : String,
        val reportID : String,
        val timestamp: Long,
        val submittedBy: UserModel,
        val assignees : List<UserModel>
)