package `in`.indianrail.ncr.enireekshan.model

import com.sun.org.apache.xpath.internal.operations.Bool

val STATUS_UNSEEN = "unseen"
val STATUS_SEEN = "unseen"
val STATUS_COMPLIED = "complied"

data class InspectionModel(
        val assignees: List<UserModel>,
        val id: Int,
        val mediaRef: String?,
        val reportID: String,
        val status: String,
        val submittedBy: UserModel,
        val timestamp: Long,
        val title: String,
        val urgent: Boolean
)

data class AssigneeRole(
        val location: String,
        val designation: String,
        val department: String
)

data class InspectionCreateModel(
        val title: String,
        val submitterID: Long,
        val mediaRef: String?,
        val urgent: Boolean,
        val assigneeRoles: List<AssigneeRole>
)