package `in`.indianrail.ncr.enireekshan

import `in`.indianrail.ncr.enireekshan.dao.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.select

class NotificationUtils {
    val messagingInstance = FirebaseMessaging.getInstance()

    fun sendNotification(notificationData: Map<String, String>, messageData: Map<String, String>, recipientsFCMTokenList: List<String?>) {

        val messages = recipientsFCMTokenList.map {
            Message.builder()
                    .putAllData(messageData)
                    .setNotification(Notification(notificationData["title"], notificationData["body"]))
                    .setToken(it)
                    .build()
        }

        messages.forEach {
            val id = messagingInstance.sendAsync(it).get()
            println(id)
        }
    }

    fun sendNotificationForEvent(observationId: Int,
                                 senderID: Long,
                                 message: Map<String, String>,
                                 messageData: Map<String, String> = emptyMap()) {
        val sentByUser = UserEntity[senderID]
        val observationID = EntityID(observationId, Observations)
        val userAssignedForObservation = Observation[observationID].assignedToUser.map { it.phone.value }
        val observationCreatedByUser = (Observations innerJoin Reports).select { Observations.id eq observationID }.map {
            it[Reports.submittedBy].value
        }[0]
        val assignableUserTokenList = with(userAssignedForObservation
                .filter { it != sentByUser.phone.value }
                .map { UserEntity[it].fcmToken }){

            if (observationCreatedByUser != sentByUser.phone.value)
                plus(UserEntity[observationCreatedByUser].fcmToken)
            else
                this
        }.filter { it != null }

        println("USER TOKEN LIST: $assignableUserTokenList")
        sendNotification(message, messageData, assignableUserTokenList)
    }
}