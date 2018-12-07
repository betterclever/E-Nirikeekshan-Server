package `in`.indianrail.ncr.enireekshan.model

import org.jetbrains.exposed.dao.EntityID


val STATUS_UNSEEN = "unseen"
val STATUS_SEEN = "seen"
val STATUS_COMPLIED = "complied"

data class InspectionModel(
        val assignees: List<UserModel>,
        val id: Int,
        val reportID: EntityID<Int>,
        val status: String,
        val timestamp: Long,
        val title: String,
        val urgent: Boolean,
        val seenByPCSO: Boolean,
        val seenBySrDSO: Boolean,
        val mediaItems: List<MediaItemsModel>,
        val submittedBy: UserModel
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
        val assigneeRoles: List<AssigneeRole>,
        val reportID: EntityID<Int>
)