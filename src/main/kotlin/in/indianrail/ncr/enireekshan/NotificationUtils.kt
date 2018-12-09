package `in`.indianrail.ncr.enireekshan

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message

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
}