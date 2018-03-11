package `in`.indianrail.ncr.enireekshan.model

val STATUS_UNSEEN = "unseen"
val STATUS_SEEN = "unseen"
val STATUS_COMPLIED = "complied"

data class InspectionModel(
        val title : String,
        val status : String,
        val reportID : String,
        val timestamp: Long,
        val submittedBy: UserModel,
        val assignees : List<UserModel>
)

data class AssigneeRole(
        val location: String,
        val designation: String,
        val department: String
)

data class InspectionCreateModel(
        val title : String,
        val submitterID: Long,
        val assigneesRoles : List<AssigneeRole>
)