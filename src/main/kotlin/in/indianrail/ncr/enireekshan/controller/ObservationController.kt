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

    fun addMessage(messageModel: MessageCreateModel, senderID: Long, observationID: Int) = transaction {
        val newMessgaeID = Messages.insertAndGetId {
            it[message] = messageModel.message
            it[observation] = EntityID(observationID, Observations)
            it[sender] = EntityID(senderID, Users)
            it[timestamp] = messageModel.timestamp
        }
        val sentByUser =  Users.select{ Users.id eq senderID}.map{
            it.prepareUserModel()
        }
        val notificationMap = mapOf("title" to "Message from ${sentByUser[0].designation} , ${sentByUser[0].location}",
                "body" to messageModel.message)
        val messageData = mapOf(
                "intentId" to observationID.toString(),
                "title" to "Message from ${sentByUser[0].designation} , ${sentByUser[0].location}",
                "body" to messageModel.message
        )
        notificationUtils.sendNotificationForEvent(observationID,
                senderID,
                notificationMap,
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
            ObservationAssignees.update({
                Observations.id eq id
            }) {
                it[ObservationAssignees.status] = status
            }
            val notificationMap = mapOf( "title" to "Observation status changed to $status by " +
                    "${UserEntity[senderID].designation}, ${UserEntity[senderID].location}",
                        "body" to Observation[id].title)
            val dataMap = mapOf("intentID" to id.toString(),
                    "title" to "Observation status changed to $status by " +
                            "${UserEntity[senderID].designation}, ${UserEntity[senderID].location}",
                    "body" to Observation[id].title)
            notificationUtils.sendNotificationForEvent(id, senderID, notificationMap, dataMap)
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
        reportID = this[Reports.id].value,
        timestamp = this[Observations.timestamp],
        assignedToUsers = Observation[this[Observations.id]].getObservationModel().assignedToUsers,
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
