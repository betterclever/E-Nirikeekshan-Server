package `in`.indianrail.ncr.enireekshan.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Users : IntIdTable() {
    val phone = integer("phone").primaryKey()
    val department = text("department")
    val designation = text("designation")
    val location = text("location")
    val name = text("name")
    val assignable = bool("assignable")
}

class UserEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)
    var phone by Users.phone
    var name by Users.name
    var location by Users.location
    var designation by Users.designation
    var department by Users.department
    var assignable by Users.assignable
}

