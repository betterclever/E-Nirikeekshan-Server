package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.currentTimeStamp
import `in`.indianrail.ncr.enireekshan.dao.InspectionAssignees
import `in`.indianrail.ncr.enireekshan.dao.Inspections
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.model.InspectionCreateModel
import `in`.indianrail.ncr.enireekshan.model.STATUS_UNSEEN
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class InspectionController {

    fun getInspectionAssignedTo(userID: Long) = transaction {
        (Inspections innerJoin InspectionAssignees)
                .select { InspectionAssignees.userID eq userID }
                .groupBy { InspectionAssignees.userID }
                .map {
                    print(it)
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
        inspectionModel.assigneesRoles.forEach {
            Users.slice(Users.id).select {
                (Users.location eq it.location) and
                        (Users.designation eq it.designation) and
                        (Users.department eq it.department)
            }.forEach { row ->
                InspectionAssignees.insert {
                    it[inspectionID] = newInspectionID
                    it[userID] = row[Users.id]
                }
            }
        }
    }
}
