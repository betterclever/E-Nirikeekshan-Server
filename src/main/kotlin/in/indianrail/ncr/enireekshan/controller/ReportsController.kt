package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.dao.Report
import `in`.indianrail.ncr.enireekshan.dao.Reports
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.model.ReportModel
import `in`.indianrail.ncr.enireekshan.model.ReportCreateModel
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
        newReportID.value
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