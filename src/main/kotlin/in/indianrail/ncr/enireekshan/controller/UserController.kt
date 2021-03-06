package `in`.indianrail.ncr.enireekshan.controller

import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.ReportModel
import `in`.indianrail.ncr.enireekshan.model.UserModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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

    fun verifyUser(phoneNumber: Long): Boolean {
        try {
            return transaction {
                val d = Users.select({ Users.id eq phoneNumber})
                        .map { it }
                return@transaction d.isNotEmpty()
            }
        } catch (exception: Exception) {
            return false
        }
    }

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
                .select {
                    (Users.location eq location) and
                            (Users.assignable eq true)
                }
                .withDistinct(true)
                .map {
                    it[Users.department]
                }
    }


    fun getDesignations(department: String, location: String): List<String> = transaction {
        Users.slice(Users.designation)
                .select {
                    (Users.location eq location) and (Users.department eq department) and
                            (Users.assignable eq true)
                }
                .withDistinct(true)
                .map {
                    it[Users.designation]
                }
    }

    fun searchUser(searchString: String): List<UserModel> = transaction {
        return@transaction exec("select * from Users where " +
                "similarity(LOWER('$searchString'), LOWER(concat(designation, location))) > 0.6 " +
                "ORDER BY similarity(LOWER('sr dcm agra'), LOWER(concat(designation, location))) DESC;") {
                    val result = mutableListOf<UserModel>()
                    while (it.next()) {
                        result.add(UserModel(
                                phone = it.getLong(1),
                                name = it.getString(5),
                                department = it.getString(2),
                                designation = it.getString(3),
                                location = it.getString(4),
                                assignable = it.getBoolean(7),
                                fcmtoken = null
                        ))
                    }
                    return@exec result
                } ?: emptyList<UserModel>()
    }

    fun getUser(phone: Long): UserModel? = transaction {
        UserEntity.findById(phone)?.getUserModel()
    }

    fun updateUser(phone: Long, user: UserModel) = transaction {
        Users.update({ Users.id eq phone }) {
            it[name] = user.name
            it[location] = user.location
            it[designation] = user.designation
            it[department] = user.department
            it[assignable] = user.assignable
        }
    }

    fun getAllUsers(): List<UserModel> = transaction {
        UserEntity.all().map { it.getUserModel() }
    }

    fun getAllVerifiedUsers(): List<UserModel> = transaction {
        Users.select(Users.assignable eq true).map { it.prepareUserModel() }
    }

    fun updateFCMToken(userID: Long, fcmToken: String) = transaction {
        Users.update({ Users.id eq userID }) {
            it[Users.fcmToken] = fcmToken
        }
    }

    fun addUser(user: UserModel): UserModel = transaction {
        UserEntity.new(user.phone) {
            user.let {
                name = it.name
                department = it.department
                designation = it.designation
                location = it.location
                assignable = it.assignable
            }
        }.getUserModel()
    }
}