package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.MessageModel
import org.jetbrains.exposed.dao.*

object Messages: IntIdTable() {
    val message = text("String")
    val sender = reference("userPhone", Users)
    val observation = reference("observationID", Observations)
    val timestamp = long("timestamp")
}

class Message(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Message>(Messages)

    var messageID by Messages.id
    var message by Messages.message
    var timestamp by Messages.timestamp
    var sender by UserEntity referencedOn Messages.sender
    var inspection by Observation referencedOn Messages.observation

    fun getIMessageModel() = MessageModel(
            id = messageID.value,
            message = message,
            sender = sender.getUserModel(),
            observationID = inspection.observationID.value,
            timestamp = timestamp
    )
}