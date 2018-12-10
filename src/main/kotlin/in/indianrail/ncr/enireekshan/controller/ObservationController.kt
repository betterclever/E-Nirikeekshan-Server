package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.NotificationUtils
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ObservationController {

    val notificationUtils = NotificationUtils()

    fun getObservationAssignedTo(userID: Long, page: Int = 1): List<ObservationModel> = transaction {
        (Observations innerJoin Reports)
                .select { ObservationAssignees.userID eq userID }
                .limit(10, (page - 1) * 10)
                .map {
                    it.prepareObservationModel()
                }
    }

    //fun getObservationAssignedTo(userID: Long, page: Int = 1) = emptyList<ObservationModel>()

    fun getObservationsSubmittedBy(userID: Long, page: Int = 1): List<ObservationModel> = transaction {
        val q = (Observations innerJoin Reports).select { Reports.submittedBy eq userID }
                .limit(10, (page - 1) * 10)
        val sql = q.prepareSQL(this)
        q.map {
            it.prepareObservationModel()
        }
    }

    fun addMessage(messageModel: MessageCreateModel, senderID: Long) = transaction {
        val newMessgaeID = Messages.insertAndGetId {
            it[message] = messageModel.message
            it[observation] = EntityID(messageModel.observationID, Observations)
            it[sender] = EntityID(senderID, Users)
            it[timestamp] = messageModel.timestamp
        }
        val sentByUser =  Users.select{ Users.id eq senderID}.map{
            it.prepareUserModel()
        }
        val messageData = mapOf(
                "sentBy" to sentByUser[0].name,
                "sentByDepartment" to sentByUser[0].department,
                "sentByLocation" to sentByUser[0].location
        )
        notificationUtils.sendNotificationForEvent(EntityID(messageModel.observationID, Observations),
                senderID,
                messageModel.message,
                messageData)
        newMessgaeID.value
    }

    fun getMessages(observationID: Int): List<MessageModel> = transaction {
        Messages.select { Messages.observation eq observationID }
                .map { it.prepareMessageModel() }
    }

    fun getObservationByID(id: Int) = transaction {
        val results = (Observations innerJoin Users).select { Observations.id eq id }
                .map { it.prepareObservationModel() }

        if (results.isNotEmpty()) {
            results[0]
        } else null
    }

    fun updateObservationStatus(id: Int, status: String, senderID: Long) {
        // validate status
        return transaction {
            Observations.update({
                Observations.id eq id
            }) {
                it[Observations.status] = status
            }
            val messageString = "Status changed  to $status by ${UserEntity[senderID].name}"
            notificationUtils.sendNotificationForEvent(EntityID(id, Observations), senderID, messageString)
        }
    }

    fun getObservationsWithTitleLike(searchQuery: String): List<ObservationModel> = transaction {
        Observations.select { Observations.title like searchQuery }
                .limit(15)
                .map {
                    it.prepareObservationModel()
                }
    }

    private fun ResultRow.prepareMessageModel() = MessageModel(
            message = this[Messages.message],
            sender = UserEntity[this[Messages.sender]].getUserModel(),
            observationID = this[Messages.observation].value,
            timestamp = this[Messages.timestamp]
    )

}

fun ResultRow.prepareObservationModel() = ObservationModel(
        id = this[Observations.id].value,
        title = this[Observations.title],
        urgent = this[Observations.urgent],
        status = this[Observations.status],
        reportID = this[Reports.id].value,
        timestamp = this[Observations.timestamp],
        assignedToUsers = Observation[this[Observations.id]].assignedToUser.map { it.phone.value },
        submittedBy = UserEntity[this[Reports.submittedBy]].getUserModel(),
        mediaItems = MediaItems.select { MediaItems.observationId eq this@prepareObservationModel[Observations.id].value }.map {
            MediaItemsModel(it[MediaItems.filePath])
        },
        seenBySrDSO = this[Observations.seenBySrDSO],
        seenByPCSO = this[Observations.seenByPCSO]
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
