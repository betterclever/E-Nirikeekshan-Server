package `in`.indianrail.ncr.enireekshan.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Observations: IntIdTable() {
    val observationID = integer("observationID").autoIncrement().primaryKey()
    val title = text("title")
    val status = text("status")
}

class Observation(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Observation>(Observations)
    val assignees by ObservationAssignee referrersOn Observations.id
}

