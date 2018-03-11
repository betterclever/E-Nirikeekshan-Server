package `in`.indianrail.ncr.enireekshan.dao

import `in`.indianrail.ncr.enireekshan.model.UserModel
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object Users : IdTable<Long>() {
    override val id: Column<EntityID<Long>> = long("phone").primaryKey().entityId()
    val department = text("department")
    val designation = text("designation")
    val location = text("location")
    val name = text("name")
    val assignable = bool("assignable")
}

class UserEntity(phone: EntityID<Long>) : Entity<Long>(phone) {
    companion object : EntityClass<Long, UserEntity>(Users)

    var phone by Users.id
    var name by Users.name
    var location by Users.location
    var designation by Users.designation
    var department by Users.department
    var assignable by Users.assignable

    fun getUserModel() = UserModel(
            name = name,
            phone = phone.value,
            location = location,
            designation = designation,
            department = department,
            assignable = assignable
    )
}

