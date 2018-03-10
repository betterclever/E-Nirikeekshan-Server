package `in`.indianrail.ncr.enireekshan.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ObservationAssignees : IntIdTable() {
    val relationID = integer("relID").primaryKey()
    val observationID = reference("observationID", Observations)
    val userID = reference("userID", Users)
}

class ObservationAssignee(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ObservationAssignee>(ObservationAssignees)
    var user by UserEntity referencedOn Users.id
    var observation by Observation referencedOn Observations.id
}