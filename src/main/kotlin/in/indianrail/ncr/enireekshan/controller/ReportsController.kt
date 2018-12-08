package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.ReportModel
import `in`.indianrail.ncr.enireekshan.model.ReportCreateModel
import `in`.indianrail.ncr.enireekshan.model.STATUS_UNSEEN
import com.google.cloud.storage.Acl
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ReportsController{
    fun getAllReports(): List<ReportModel> = transaction {
        Report.all().map { it.getReportModel() }
    }

    fun addReport(report: ReportCreateModel) = transaction {
        val newReportID = Reports.insertAndGetId {
            it[submittedBy] = EntityID(report.submittedBy, Users)
        }
        val inspectionIDList = ArrayList<Int>()
        report.inspections.forEach{ inspection ->
            val newInspectionID = Inspections.insertAndGetId {
                it[title] = inspection.title
                it[status] = STATUS_UNSEEN
                it[urgent] = inspection.urgent
                it[reportID] = newReportID
                it[timestamp] = inspection.timestamp
                it[seenByPCSO] = false
                it[seenBySrDSO] = false
                it[assignedToUser] = EntityID(inspection.assignedToUser, Users)
            }
            inspection.mediaLinks.forEach{mediaLink ->
                val newMediaID = MediaItems.insertAndGetId {
                    it[inspectionId] = EntityID(newInspectionID.value, Inspections)
                    it[filePath] = mediaLink
                }
            }
            inspectionIDList.add(newInspectionID.value)
        }
        inspectionIDList
    }

    fun getReportsByID(id: Int) = transaction {
        val results = Reports.select { Reports.id eq id }
                .map { it.prepareReportModel() }

        if (results.isNotEmpty()) {
            results[0]
        } else null
    }

    fun getReportsByUser(id: Long) = transaction {
        Reports.select{Reports.submittedBy eq id}.map {
            it[Reports.id].value
        }
    }
    private fun ResultRow.prepareReportModel() = ReportModel(
            id = this[Reports.id].value,
            submittedBy = this[Reports.submittedBy].value
    )
}