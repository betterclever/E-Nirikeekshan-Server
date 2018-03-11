package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.dao.UserEntity
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.model.UserModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserController {

    /*fun updateUser(user: User) {
        return transaction {
            Users.update({ Users.phone eq user.phone }) {
                it[name] = user.name
                it[department] = user.department
                it[designation] = user.designation
                it[location] = user.location
            }
        }
    }*/

    fun getLocations(): List<String> {
        return transaction {
            Users.slice(Users.location)
                    .selectAll()
                    .withDistinct(true)
                    .groupBy(Users.location).map {
                        it[Users.location]
                    }
        }
    }

    fun getDepartments(location: String): List<String> {
        return transaction {
            Users.slice(Users.department)
                    .select({
                        (Users.location eq location) and
                                (Users.assignable eq true)
                    })
                    .withDistinct(true)
                    .map {
                        it[Users.department]
                    }
        }
    }

    fun getDesignations(department: String, location: String): List<String> {
        return transaction {
            Users.slice(Users.designation)
                    .select({
                        (Users.location eq location) and (Users.department eq department) and
                                (Users.assignable eq true)
                    })
                    .withDistinct(true)
                    .map {
                        it[Users.designation]
                    }
        }
    }

    fun getUser(phone: Long): UserEntity? {
        return transaction {
            UserEntity.findById(phone)
        }
    }

    fun getAllUsers(): List<UserModel> {
        return transaction {
            UserEntity.all().map { it.getUserModel() }
        }
    }

    fun addUser(user: UserModel): UserModel {
        return transaction {
            UserEntity.new(user.phone, {
                user.let {
                    name = it.name
                    department = it.department
                    designation = it.designation
                    department = it.department
                    location = it.location
                    assignable = it.assignable
                }
            }).getUserModel()
        }
    }
}