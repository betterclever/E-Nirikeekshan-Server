package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.ReportModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Reports : IntIdTable() {
    val submittedBy = reference("submittedBy", Users)
    val timestamp = long("timestamp")
    val title = text("title")
}

class Report(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Report>(Reports)

    var reportID by Reports.id
    var submittedBy by UserEntity referencedOn Reports.submittedBy
    val observations by Observation referrersOn Observations.reportID
    val timestamp by Reports.timestamp
    val title by Reports.title

    fun getReportModel() = ReportModel(
            id = reportID.value,
            submittedBy = submittedBy.getUserModel(),
            observations = observations.map{ it.getObservationModel() },
            timestamp =  timestamp,
            title = title
    )
}


