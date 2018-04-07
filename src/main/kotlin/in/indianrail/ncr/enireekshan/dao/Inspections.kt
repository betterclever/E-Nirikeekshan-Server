package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.InspectionModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Inspections : IntIdTable() {
    val title = text("title")
    val status = text("status")
    val timestamp = long("timestamp")
    val mediaRef = text("media").nullable()
    val urgent = bool("urgent")
    val submittedBy = reference("submittedBy", Users)
    val reportID = text("reportID")
}

class Inspection(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Inspection>(Inspections)

    var title by Inspections.title
    var inspectionID by Inspections.id
    var status by Inspections.status
    var urgent by Inspections.urgent
    val mediaRef by Inspections.mediaRef
    var reportID by Inspections.reportID
    var timestamp by Inspections.timestamp
    val assignees by UserEntity via InspectionAssignees
    var submittedBy by UserEntity referencedOn Inspections.submittedBy

    fun getInspectionModel() = InspectionModel(
            assignees = assignees.map { it.getUserModel() },
            id = id.value,
            mediaRef = mediaRef,
            reportID = reportID,
            status = status,
            submittedBy = submittedBy.getUserModel(),
            timestamp = timestamp,
            title = title,
            urgent = urgent
    )
}


