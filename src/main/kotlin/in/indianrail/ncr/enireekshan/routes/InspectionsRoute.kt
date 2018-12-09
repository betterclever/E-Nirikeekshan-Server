package `in`.indianrail.ncr.enireekshan.routes

import `in`.indianrail.ncr.enireekshan.controller.InspectionController
import `in`.indianrail.ncr.enireekshan.model.InspectionCreateModel
import `in`.indianrail.ncr.enireekshan.model.MessageCreateModel
import `in`.indianrail.ncr.enireekshan.runVerifed
import com.google.firebase.auth.FirebaseAuth
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.patch
import io.ktor.routing.post
import io.ktor.util.error

fun Route.inspections(firebaseAuth: FirebaseAuth){
    val inspectionController = InspectionController()

    get("/markedTo/{userID}") {
        runVerifed(firebaseAuth, call) {
            val userID = call.parameters["userID"]
            if (userID != null) {
                try {
                    val num = userID.toLong()
                    call.respond(inspectionController.getInspectionAssignedTo(num))
                } catch (e: Exception) {
                   application.log.error(e.message)
                }
            } else call.respond(emptyArray<Int>())
        }
    }
    get("/submittedBy/{userID}") {
        runVerifed(firebaseAuth, call) {
            val userID = call.parameters["userID"]
            if (userID != null) {
                try {
                    val num = userID.toLong()
                    call.respond(inspectionController.getInspectionsSubmittedBy(num))
                } catch (e: Exception) {
                    call.respond(emptyArray<Int>())
                    application.log.error(e.message)
                }
            } else call.respond(emptyArray<Int>())
        }
    }

    get("/{id}") {
        runVerifed(firebaseAuth, call) {
            val idS = call.parameters["id"]
            if (idS != null) {
                val response = try {
                    val id = idS.toInt()
                    inspectionController.getInspectionByID(id)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    null
                }
                if(response!=null) {
                    call.respond(response)
                } else call.respond(HttpStatusCode(404, "Not Found"), "Server Error")
            }
        }
    }

    post("/new") {
        runVerifed(firebaseAuth, call) {
            val inspectionCreateModel = call.receive<InspectionCreateModel>()
            println(inspectionCreateModel)
            call.respond(inspectionController.addInspection(inspectionCreateModel))
        }
    }

    post("/{id}/messages") {
        runVerifed(firebaseAuth, call) {
            val idS = call.parameters["id"]
            val message = call.receive<MessageCreateModel>()
            if (idS != null) {
                try {
                    val id = idS.toInt()
                    call.respond(inspectionController.addMessage(message, it))
                } catch (expection: Exception) {
                    expection.printStackTrace()
                    call.respond(HttpStatusCode(404, "Not Found"), "Server Error")
                }
            }
        }
    }
    get("/{id}/messages") {
        runVerifed(firebaseAuth, call) {
            val idS = call.parameters["id"]
            if(idS!=null) {
                try {
                    val id = idS.toInt()
                    call.respond(inspectionController.getMessages(id))
                } catch (expection: Exception) {
                    expection.printStackTrace()
                    call.respond(HttpStatusCode(404, "Not Found"), "Server Error")
                }
            }
        }
    }

    patch("/{id}/updateStatus") {
        runVerifed(firebaseAuth, call) {
            val newStatus = call.receive<String>()
            val idS = call.parameters["id"]
            if (idS != null) {
                val response = try {
                    val id = idS.toInt()
                    inspectionController.updateInspectionStatus(id, newStatus)
                    "Success"
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    "Failed"
                }
                call.respond(response)
            }
        }
    }
}