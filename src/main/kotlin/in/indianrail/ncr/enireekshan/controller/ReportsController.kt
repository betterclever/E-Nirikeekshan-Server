package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.NotificationUtils
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

val notificationUtils = NotificationUtils()

class ReportsController{
    fun getAllReports(): List<ReportModel> = transaction {
        Reports.selectAll().orderBy(Reports.timestamp, isAsc = false).map {
            it.prepareReportModel()
        }
    }

    fun addReport(report: ReportCreateModel) = transaction {
        println(report)
        val newReportID = Reports.insertAndGetId {
            it[submittedBy] = EntityID(report.submittedBy, Users)
            it[timestamp] = report.timestamp
            it[title] = report.title
        }
        val observationIDList = ArrayList<Int>()
        val sentByUser =  Users.select{Users.id eq report.submittedBy}.map{
            it.prepareUserModel()
        }
        report.observations.forEach{ observation ->
            val newObservationID = Observations.insertAndGetId {
                it[title] = observation.title
                it[status] = STATUS_UNSEEN
                it[urgent] = observation.urgent
                it[reportID] = newReportID
                it[timestamp] = observation.timestamp
                it[seenByPCSO] = false
                it[seenBySrDSO] = false
            }

            observation.assignedToUsers.forEach { phone->
                val entry = ObservationAssignees.insert {
                    it[observationID] = newObservationID
                    it[userID] = EntityID(phone, Users)
                }
            }
            val assignedUserTokenList =  Users.select{Users.id inList observation.assignedToUsers.map{it}}.map{
                it[Users.fcmToken]
            }
            notificationUtils.sendNotification(observation.title, mapOf(
                    "type" to "New Assignment",
                    "sentBy" to sentByUser[0].name,
                    "sentByDepartment" to sentByUser[0].department,
                    "sentByLocation" to sentByUser[0].location
            ), assignedUserTokenList)

            observation.mediaLinks.forEach{mediaLink ->
                val newMediaID = MediaItems.insertAndGetId {
                    it[observationId] = EntityID(newObservationID.value, Observations)
                    it[filePath] = mediaLink
                }
            }
            observationIDList.add(newObservationID.value)
        }
        Report[newReportID].getReportModel()

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
    private fun ResultRow.prepareReportModel() = ReportModel(
            id = this[Reports.id].value,
            submittedBy = this[Reports.submittedBy].value,
            timestamp = this[Reports.timestamp],
            observations = (Observations innerJoin Reports).select { Observations.reportID eq this@prepareReportModel[Reports.id]}.map{
                it.prepareObservationModel()
            },
            title = this[Reports.title]
    )
}