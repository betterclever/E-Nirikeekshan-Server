package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.dao.UserEntity
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.model.UserModel
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

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

    fun getLocations(): List<String> = transaction {
        Users.slice(Users.location)
                .selectAll()
                .withDistinct(true)
                .groupBy(Users.location).map {
            it[Users.location]
        }
    }


    fun getDepartments(location: String): List<String> = transaction {
        Users.slice(Users.department, Users.location)
                .select({
                    (Users.location eq location) and
                            (Users.assignable eq false)
                })
                .withDistinct(true)
                .map {
                    it[Users.department]
                }
    }


    fun getDesignations(department: String, location: String): List<String> = transaction {
        Users.slice(Users.designation)
                .select({
                    (Users.location eq location) and (Users.department eq department) and
                            (Users.assignable eq false)
                })
                .withDistinct(true)
                .map {
                    it[Users.designation]
                }
    }


    fun getUser(phone: Long): UserEntity? = transaction {
        UserEntity.findById(phone)
    }


    fun getAllUsers(): List<UserModel> = transaction {
        UserEntity.all().map { it.getUserModel() }
    }

    fun updateFCMToken(userID: Long, fcmToken: String) = transaction {
        Users.update({ Users.id eq userID }) {
            it[Users.fcmToken] = fcmToken
        }
    }

    fun addUser(user: UserModel): UserModel = transaction {
        UserEntity.new(user.phone, {
            user.let {
                name = it.name
                department = it.department
                designation = it.designation
                location = it.location
                assignable = it.assignable
            }
        }).getUserModel()
    }

}