package `in`.indianrail.ncr.enireekshan.routes

import `in`.indianrail.ncr.enireekshan.model.UserModel
import `in`.indianrail.ncr.enireekshan.runVerifed
import `in`.indianrail.ncr.enireekshan.userController
import com.google.firebase.auth.FirebaseAuth
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post

fun Route.users(firebaseAuth: FirebaseAuth){
    get("/locations") {
        runVerifed(firebaseAuth, this.call) {
            call.respond(userController.getLocations())
        }
    }
    get("/{location}/departments") {
        runVerifed(firebaseAuth, call) {
            val location = call.parameters["location"]
            if (location != null) {
                call.respond(userController.getDepartments(location))
            } else {
                call.respond(emptyArray<String>())
            }
        }
    }
    get("/{location}/{department}/designations") {
        runVerifed(firebaseAuth, call) {
            val location = call.parameters["location"]
            val department = call.parameters["department"]
            if (location != null && department != null) {
                call.respond(userController.getDesignations(department, location))
            } else {
                call.respond(emptyArray<String>())
            }
        }
    }
    get("/") {
        runVerifed(firebaseAuth, call) {
            call.respond(userController.getAllUsers())
        }
    }
    get("/assignable"){
        runVerifed(firebaseAuth, call) {
            call.respond(userController.getAllVerifiedUsers())
        }
    }
    post("/") {
        runVerifed(firebaseAuth, call) {
            val user = call.receive<UserModel>()
            call.respond(userController.addUser(user))
        }
    }
    get("/{id}") {
        runVerifed(firebaseAuth, call) {
            val userID = call.parameters["id"]
            val result = if(userID!=null) {
                try {
                    val uid = userID.toLong()
                    val user = userController.getUser(uid)
                    println(user)
                    user
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    null
                }
            } else null
            if (result != null) call.respond(result) else {
                call.respond(HttpStatusCode(404, "Not Found"), "Not Found")
            }
        }
    }

    post("/{id}") {
        runVerifed(firebaseAuth, call) {
            val userID = call.parameters["id"]
            val result = if(userID!=null) {
                try {
                    val uid = userID.toLong()
                    val userModel = call.receive<UserModel>()
                    userController.updateUser(uid, userModel)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    null
                }
            } else null
            if (result != null) call.respond(result) else {
                call.respond(HttpStatusCode(401, "Invalid Operation"), "Invalid Operation")
            }
        }
    }
    post("{id}/updateFCMToken") {
        runVerifed(firebaseAuth, call) {
            val token = call.receive<String>().substringAfter("\"").substringBefore("\"")
            val id = call.parameters["id"]
            if (id != null) {
                call.respond(userController.updateFCMToken(id.toLong(), token))
            }
        }
    }
}