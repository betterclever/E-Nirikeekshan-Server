package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.ObservationModel
import `in`.indianrail.ncr.enireekshan.model.MediaItemsModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Observations : IntIdTable() {
    val title = text("title")
    val status = text("status")
    val timestamp = long("timestamp")
    val urgent = bool("urgent")
    val reportID = reference("reportID", Reports)
    val seenByPCSO = bool("seenByPCSO")
    val seenBySrDSO = bool("seenBySrDSO")
}

class Observation(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Observation>(Observations)
    var title by Observations.title
    var observationID by Observations.id
    var status by Observations.status
    var urgent by Observations.urgent
    var reportID by Observations.reportID
    var timestamp by Observations.timestamp
    val mediaItems by MediaItem referrersOn MediaItems.observationId
    val assignedToUser by UserEntity via ObservationAssignees
    var seenByPCSO by Observations.seenByPCSO
    var seenBySrDSO by Observations.seenBySrDSO

    fun getObservationModel() = ObservationModel(
            assignedToUser = assignedToUser.map { it.phone.value }, //List of User Phone number i.e. List<Long>
            id = observationID.value,
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