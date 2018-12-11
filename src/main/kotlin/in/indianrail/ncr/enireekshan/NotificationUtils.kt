package `in`.indianrail.ncr.enireekshan

import `in`.indianrail.ncr.enireekshan.controller.prepareUserModel
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.MessageModel
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select

class NotificationUtils {
    val messagingInstance = FirebaseMessaging.getInstance()

    fun sendNotification(messageTitle: String, messageData: Map<String, String>, recipients: List<String?>) {

        val messages = recipients.map {
            Message.builder()
                    .putAllData(messageData)
                    .setNotification(Notification(messageTitle, "asd"))
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
                                 message: String,
                                 messageData: Map<String, String> = emptyMap()) {
        val sentByUser = UserEntity[senderID]
        val observationID = EntityID(observationId, Observations)
        val userAssignedForObservation = Observation[observationID].assignedToUser.map { it.phone.value }
        val observationCreatedByUser = (Observations innerJoin Reports).select { Observations.id eq observationID }.map {
            it[Reports.submittedBy].value
        }[0]
        val assignable = with(userAssignedForObservation
                .filter { it != sentByUser.phone.value }
                .map { UserEntity[it].fcmToken }){

            if (observationCreatedByUser != sentByUser.phone.value)
                plus(UserEntity[observationCreatedByUser].fcmToken)
            else
                this
        }.filter { it != null }

        println("USER TOKEN LIST: $assignable")
        assignable.filter { it != null }
        if (assignable[0] != null) {
            sendNotification(message, messageData, assignable)
        }
    }
}