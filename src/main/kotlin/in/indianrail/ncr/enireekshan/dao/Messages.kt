package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.MessageModel
import org.jetbrains.exposed.dao.*

object Messages: IntIdTable() {
    val message = text("String")
    val sender = reference("userPhone", Users)
    val inspection = reference("inspectionID", Inspections)
    val timestamp = long("timestamp")
}

class Message(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Message>(Messages)

    var message by Messages.message
    var timestamp by Messages.timestamp
    var sender by UserEntity referencedOn Messages.sender
    var inspection by Inspection referencedOn Messages.inspection

    fun getIMessageModel() = MessageModel(
            message = message,
            sender = sender.getUserModel(),
            inspectionID = inspection.inspectionID.value,
            timestamp = timestamp
    )
}