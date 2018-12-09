package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.NotificationUtils
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.*
import com.google.cloud.storage.Acl
import com.typesafe.config.ConfigException
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

val notificationUtils = NotificationUtils()

class ReportsController{
    fun getAllReports(): List<ReportModel> = transaction {
        Report.all().map { it.getReportModel() }
    }

    fun addReport(report: ReportCreateModel) = transaction {
        val newReportID = Reports.insertAndGetId {
            it[submittedBy] = EntityID(report.submittedBy, Users)
        }
        val inspectionIDList = ArrayList<Int>()
        val sentByUser =  Users.select{Users.id eq report.submittedBy}.map{
            it.prepareUserModel()
        }
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
            val assignedUserTokenList =  Users.select{Users.id eq inspection.assignedToUser}.map{
                it[Users.fcmToken]
            }
            notificationUtils.sendNotification(inspection.title, mapOf(
                    "type" to "New Assignment",
                    "sentBy" to sentByUser[0].name,
                    "sentByDepartment" to sentByUser[0].department,
                    "sentByLocation" to sentByUser[0].location
            ), assignedUserTokenList)

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
        val report = Report[id]
        report.getReportModel()
    }

    fun getReportsByUser(id: Long) = transaction {
        Reports.select{Reports.submittedBy eq id}.map {
            it[Reports.id].value
        }.map { Report[it].getReportModel() }
    }
}