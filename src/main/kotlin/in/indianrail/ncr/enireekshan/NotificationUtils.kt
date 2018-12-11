package `in`.indianrail.ncr.enireekshan

import `in`.indianrail.ncr.enireekshan.controller.prepareUserModel
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.MessageModel
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select

class NotificationUtils {
    val messagingInstance = FirebaseMessaging.getInstance()

    fun sendNotification(messageTitle: String, messageData: Map<String, String>, recipients: List<String?>) {

        val messages = recipients.map { Message.builder()
                .putAllData(messageData)
                .putData("title",messageTitle)
                .setToken(it)
                .build()
        }

        messages.forEach {
            messagingInstance.sendAsync(it)
        }
    }

    fun sendNotificationForEvent(observationId: Int,
                                 senderID : Long,
                                 message: String,
                                 messageData : Map<String, String> = emptyMap()){
        val sentByUser =  Users.select{ Users.id eq senderID}.map{
            it.prepareUserModel()
        }
        val observationID = EntityID(observationId, Observations)
        val userAssignedForObservation = Observation[observationID].assignedToUser.map{ it.phone.value}
        val observationCreatedByUser = (Observations innerJoin Reports).select { Observations.id eq observationID}.map{
            it[Reports.submittedBy].value
        }[0]
        var assignedUserTokenList = mutableListOf<String?>()
        userAssignedForObservation.forEach {
            if (it != sentByUser[0].phone) {
                assignedUserTokenList.add(UserEntity[it].fcmToken)
            }
        }
        if(observationCreatedByUser != sentByUser[0].phone)
            assignedUserTokenList.add(UserEntity[observationCreatedByUser].fcmToken)

        println("USER TOKEN LIST: $assignedUserTokenList")
        if(assignedUserTokenList[0] != null) {
            sendNotification(message, messageData, assignedUserTokenList)
        }
    }
}