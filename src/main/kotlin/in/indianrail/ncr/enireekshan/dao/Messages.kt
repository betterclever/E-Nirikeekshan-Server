package `in`.indianrail.ncr.enireekshan.dao

import org.jetbrains.exposed.dao.*

object Messages: IntIdTable() {
    val message = text("String")
    val sender = reference("userPhone", Users)
    val inspection = reference("inspectionID", Inspections)
}

class Message(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Message>(Messages)

    var message by Messages.message
    var sender by UserEntity referencedOn Messages.sender
    var inspection by Inspection referencedOn Messages.inspection
}