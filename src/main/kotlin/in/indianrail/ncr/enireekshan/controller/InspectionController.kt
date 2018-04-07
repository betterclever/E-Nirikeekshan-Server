package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.currentTimeStamp
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.InspectionCreateModel
import `in`.indianrail.ncr.enireekshan.model.InspectionModel
import `in`.indianrail.ncr.enireekshan.model.STATUS_UNSEEN
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InspectionController {

    fun getInspectionAssignedTo(userID: Long, page: Int = 1): List<InspectionModel> = transaction {
        (Inspections innerJoin InspectionAssignees)
                .select { InspectionAssignees.userID eq userID }
                .limit(10, (page - 1) * 10)
                .map {
                    it.prepareInspectionModel()
                }
    }

    //fun getInspectionAssignedTo(userID: Long, page: Int = 1) = emptyList<InspectionModel>()

    fun getInspectionsSubmittedBy(userID: Long, page: Int = 1): List<InspectionModel> = transaction {
        val q = Inspections.select { Inspections.submittedBy eq userID }
                .limit(10, (page - 1) * 10)

        val sql = q.prepareSQL(this)
        q.map {
            it.prepareInspectionModel()
        }
    }

    fun addInspection(inspectionModel: InspectionCreateModel) = transaction {
        val newInspectionID = Inspections.insertAndGetId {
            it[title] = inspectionModel.title
            it[status] = STATUS_UNSEEN
            it[urgent] = inspectionModel.urgent
            it[reportID] = "abc"
            it[timestamp] = currentTimeStamp
            it[submittedBy] = EntityID(inspectionModel.submitterID, Users)
        }
        inspectionModel.assigneeRoles.forEach {
            /*val messaging = FirebaseMessaging.getInstance()
            val message = Message.builder()
                    .putData("sender", )
            */
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

    fun getInspectionByID(id: Int) = transaction {
        val results = Inspections.select { Inspections.id eq id }
                .map { it.prepareInspectionModel() }

        if (results.isNotEmpty()) {
            results[0]
        } else null
    }

    fun updateInspectionStatus(id: Int, status: String) {
        // validate status
        return transaction {
            Inspections.update({
                Inspections.id eq id
            }) {
                it[Inspections.status] = status
            }
        }
    }

    fun getInspectionsWithTitleLike(searchQuery: String): List<InspectionModel> = transaction {
        Inspections.select { Inspections.title like searchQuery }
                .limit(15)
                .map {
                    it.prepareInspectionModel()
                }
    }

    private fun ResultRow.prepareInspectionModel() = InspectionModel(
            id = 0,
            mediaRef = this[Inspections.mediaRef],
            title = this[Inspections.title],
            urgent = this[Inspections.urgent],
            status = this[Inspections.status],
            reportID = this[Inspections.reportID],
            timestamp = this[Inspections.timestamp],
            assignees = Inspection[this[Inspections.id]].assignees.map(UserEntity::getUserModel),
            submittedBy = UserEntity[this[Inspections.submittedBy]].getUserModel()
    )
}
