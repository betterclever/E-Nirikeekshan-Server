package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.currentTimeStamp
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.InspectionCreateModel
import `in`.indianrail.ncr.enireekshan.model.InspectionModel
import `in`.indianrail.ncr.enireekshan.model.STATUS_UNSEEN
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class InspectionController {

    fun getInspectionAssignedTo(userID: Long): List<InspectionModel> = transaction {
        (Inspections innerJoin InspectionAssignees)
                .select { InspectionAssignees.userID eq userID }
                .groupBy { InspectionAssignees.inspectionID }
                .map {
                    it.value.map {
                        InspectionModel(
                                title = it[Inspections.title],
                                status = it[Inspections.status],
                                reportID = it[Inspections.reportID],
                                timestamp = it[Inspections.timestamp],
                                assignees = Inspection[it[Inspections.id]].assignees.map(UserEntity::getUserModel),
                                submittedBy = UserEntity[it[Inspections.submittedBy]].getUserModel())
                    }
                }.flatten()
    }

    fun getInspectionsSubmittedBy(userID: Long): List<InspectionModel> = transaction {
        Inspections.select { Inspections.submittedBy eq userID }
                .map {
                    InspectionModel(
                            title = it[Inspections.title],
                            status = it[Inspections.status],
                            reportID = it[Inspections.reportID],
                            timestamp = it[Inspections.timestamp],
                            assignees = Inspection[it[Inspections.id]].assignees.map(UserEntity::getUserModel),
                            submittedBy = UserEntity[it[Inspections.submittedBy]].getUserModel()
                    )
                }
    }

    fun addInspection(inspectionModel: InspectionCreateModel) = transaction {
        val newInspectionID = Inspections.insertAndGetId {
            it[title] = inspectionModel.title
            it[status] = STATUS_UNSEEN
            it[reportID] = "abc"
            it[timestamp] = currentTimeStamp
            it[submittedBy] = EntityID(inspectionModel.submitterID, Users)
        }
        inspectionModel.assigneeRoles.forEach {
            val res = Users.slice(Users.id).select {
                (Users.location eq it.location) and
                        (Users.designation eq it.designation) and
                        (Users.department eq it.department)
            }
            res.forEach { row ->
                InspectionAssignees.insert {
                    it[inspectionID] = newInspectionID
                    it[userID] = row[Users.id]
                }
            }
        }
    }
}
