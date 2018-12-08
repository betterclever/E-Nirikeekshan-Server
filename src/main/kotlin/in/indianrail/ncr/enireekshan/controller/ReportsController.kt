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
}