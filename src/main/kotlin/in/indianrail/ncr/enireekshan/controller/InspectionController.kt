package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.NotificationUtils
import `in`.indianrail.ncr.enireekshan.currentTimeStamp
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InspectionController {

    val notificationUtils = NotificationUtils()

    fun getInspectionAssignedTo(userID: Long, page: Int = 1): List<InspectionModel> = transaction {
        (Inspections innerJoin Reports)
                .select { InspectionAssignees.userID eq userID }
                .limit(10, (page - 1) * 10)
                .map {
                    it.prepareInspectionModel()
                }
    }

    //fun getInspectionAssignedTo(userID: Long, page: Int = 1) = emptyList<InspectionModel>()

    fun getInspectionsSubmittedBy(userID: Long, page: Int = 1): List<InspectionModel> = transaction {
        val q = (Inspections innerJoin Reports).select { Reports.submittedBy eq userID }
                .limit(10, (page - 1) * 10)
        val sql = q.prepareSQL(this)
        q.map {
            it.prepareInspectionModel()
        }
    }

    fun addMessage(messageModel: MessageCreateModel, userID: Long) = transaction {
        val newMessgaeID = Messages.insertAndGetId {
            it[message] = messageModel.message
            it[inspection] = EntityID(messageModel.inspectionID, Inspections)
            it[sender] = EntityID(userID, Users)
            it[timestamp] = messageModel.timestamp
        }
        val sentByUser =  Users.select{Users.id eq userID}.map{
            it.prepareUserModel()
        }
        val assignedUserTokenList =  (Inspections innerJoin Users).select{(Inspections.id eq messageModel.inspectionID)}.map{
            it[Users.fcmToken]
        }
        notificationUtils.sendNotification(messageModel.message, mapOf(
                "sentBy" to sentByUser[0].name,
                "sentByDepartment" to sentByUser[0].department,
                "sentByLocation" to sentByUser[0].location
        ), assignedUserTokenList)
        newMessgaeID.value
    }

    fun getMessages(inspectionID: Int): List<MessageModel> = transaction {
        Messages.select { Messages.inspection eq inspectionID }
                .map { it.prepareMessageModel() }
    }

    fun addInspection(inspectionModel: InspectionCreateModel) = transaction {
        val newInspectionID = Inspections.insertAndGetId {
            it[title] = inspectionModel.title
            it[status] = STATUS_UNSEEN
            it[urgent] = inspectionModel.urgent
            it[reportID] = EntityID(inspectionModel.reportID, Reports)
            it[timestamp] = inspectionModel.timestamp
            it[seenByPCSO] = false
            it[seenBySrDSO] = false
            it[assignedToUser] = EntityID(inspectionModel.assignedToUser, Users)
        }
//        val recepients = mutableListOf<String>()
//        inspectionModel.assigneeRoles.forEach {
//            val res = Users.slice(Users.id, Users.fcmToken).select {
//                (Users.location eq it.location) and
//                        (Users.designation eq it.designation) and
//                        (Users.department eq it.department)
//            }
//            res.forEach { row ->
//                row[Users.fcmToken]?.let { recepients.add(it) }
//                InspectionAssignees.insert {
//                    it[inspectionID] = newInspectionID
//                    it[userID] = row[Users.id]
//                }
//            }
//        }
        newInspectionID.value
    }

    fun getInspectionByID(id: Int) = transaction {
        val results = (Inspections innerJoin Users).select { Inspections.id eq id }
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
            val assignedUserTokenList =  (Inspections innerJoin Users).select{(Inspections.id eq id)}.map{
                it[Users.fcmToken]
            }
            val assignedInspection =  (Inspections).select{(Inspections.id eq id)}.map{
                it[Inspections.title]
            }

            notificationUtils.sendNotification("Status Update: " + assignedInspection[0], mapOf(
            ), assignedUserTokenList)
        }
    }

    fun getInspectionsWithTitleLike(searchQuery: String): List<InspectionModel> = transaction {
        Inspections.select { Inspections.title like searchQuery }
                .limit(15)
                .map {
                    it.prepareInspectionModel()
                }
    }

    private fun ResultRow.prepareMessageModel() = MessageModel(
            message = this[Messages.message],
            sender = UserEntity[this[Messages.sender]].getUserModel(),
            inspectionID = this[Messages.inspection].value,
            timestamp = this[Messages.timestamp]
    )

}

fun ResultRow.prepareInspectionModel() = InspectionModel(
        id = this[Inspections.id].value,
        title = this[Inspections.title],
        urgent = this[Inspections.urgent],
        status = this[Inspections.status],
        reportID = this[Reports.id].value,
        timestamp = this[Inspections.timestamp],
        //assignees = Inspection[this[Inspections.id]].assignees.map(UserEntity::getUserModel),
        submittedBy = UserEntity[this[Reports.submittedBy]].getUserModel(),
        mediaItems = MediaItems.select { MediaItems.inspectionId eq this@prepareInspectionModel[Inspections.id].value }.map {
            MediaItemsModel(it[MediaItems.filePath])
        },
        seenBySrDSO = this[Inspections.seenBySrDSO],
        seenByPCSO = this[Inspections.seenByPCSO],
        assignedToUser = this[Inspections.assignedToUser].value
)

fun ResultRow.prepareUserModel() = UserModel(
        phone = this[Users.id].value,
        name = this[Users.name],
        designation = this[Users.designation],
        department = this[Users.department],
        location = this[Users.location],
        assignable = this[Users.assignable],
        fcmtoken = this[Users.fcmToken]
)
