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
    val submittedBy = reference("submittedBy", Users)
    val reportID = text("reportID")
}

class Inspection(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Inspection>(Inspections) {
        fun fromInspectionModel(inspectionModel: InspectionModel): Inspection = new {
            inspectionModel.let {
                title = it.title
                status = it.status
                reportID = "ABC"
                timestamp = it.timestamp
               // submittedBy = it.submittedBy.
            }
        }
    }

    var title by Inspections.title
    var inspectionID by Inspections.id
    var status by Inspections.status
    var reportID by Inspections.reportID
    var timestamp by Inspections.timestamp
    val assignees by UserEntity via InspectionAssignees
    var submittedBy by UserEntity referencedOn Inspections.submittedBy

    fun getInspectionModel() = InspectionModel(
            title = title,
            status = status,
            assignees = assignees.map { it.getUserModel() },
            timestamp = timestamp,
            submittedBy = submittedBy.getUserModel(),
            reportID = reportID
    )
}


