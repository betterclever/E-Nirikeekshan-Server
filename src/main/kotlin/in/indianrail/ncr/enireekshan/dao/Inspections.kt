package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.InspectionModel
import `in`.indianrail.ncr.enireekshan.model.MediaItemsModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Inspections : IntIdTable() {
    val title = text("title")
    val status = text("status")
    val timestamp = long("timestamp")
    val urgent = bool("urgent")
    val reportID = reference("reportID", Reports)
    val seenByPCSO = bool("seenByPCSO")
    val seenBySrDSO = bool("seenBySrDSO")
}

class Inspection(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Inspection>(Inspections)

    var title by Inspections.title
    var inspectionID by Inspections.id
    var status by Inspections.status
    var urgent by Inspections.urgent
    var reportID by Inspections.reportID
    var timestamp by Inspections.timestamp
    val mediaItems by MediaItem referrersOn MediaItems.inspectionId
    val assignees by UserEntity via InspectionAssignees
    var seenByPCSO by Inspections.seenByPCSO
    var seenBySrDSO by Inspections.seenBySrDSO

    fun getInspectionModel() = InspectionModel(
            assignees = assignees.map { it.getUserModel() },
            id = inspectionID.value,
            reportID = reportID.value,
            status = status,
            timestamp = timestamp,
            title = title,
            urgent = urgent,
            seenByPCSO = seenByPCSO,
            seenBySrDSO = seenBySrDSO,
            mediaItems = mediaItems.map{ MediaItemsModel(it.filePath) },
            submittedBy = Report[reportID.value].submittedBy.getUserModel()
    )
}


