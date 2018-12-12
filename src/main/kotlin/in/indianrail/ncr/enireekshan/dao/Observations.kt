package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.ObservationModel
import `in`.indianrail.ncr.enireekshan.model.MediaItemsModel
import `in`.indianrail.ncr.enireekshan.model.ObservationAssigneesModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

object Observations : IntIdTable() {
    val title = text("title")
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
    var urgent by Observations.urgent
    var reportID by Observations.reportID
    var timestamp by Observations.timestamp
    val mediaItems by MediaItem referrersOn MediaItems.observationId
//    val assignedToUser by UserEntity via ObservationAssignees
    var seenByPCSO by Observations.seenByPCSO
    var seenBySrDSO by Observations.seenBySrDSO

    fun getObservationModel(): ObservationModel {
        val observationAssigneesModelMap = ObservationAssignees.select{
            ObservationAssignees.observationID eq observationID.value
        }.map{
            it.getObservationAssigneeModel()
        }
        return ObservationModel(
                assignedToUsers = observationAssigneesModelMap, //List of User Phone number i.e. List<ObservationAssigneeModel>
                id = observationID.value,
                reportID = reportID.value,
                timestamp = timestamp,
                title = title,
                urgent = urgent,
                seenByPCSO = seenByPCSO,
                seenBySrDSO = seenBySrDSO,
                mediaItems = mediaItems.map{ MediaItemsModel(it.filePath) },
                submittedBy = Report[reportID.value].submittedBy.getUserModel()
        )
    }
    private fun ResultRow.getObservationAssigneeModel() = ObservationAssigneesModel(
            status = this[ObservationAssignees.status],
            assignedUser = this[ObservationAssignees.userID].value
    )
}