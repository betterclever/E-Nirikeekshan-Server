package `in`.indianrail.ncr.enireekshan.routes

import `in`.indianrail.ncr.enireekshan.model.ReportCreateModel
import `in`.indianrail.ncr.enireekshan.reportsController
import `in`.indianrail.ncr.enireekshan.runVerified
import com.google.firebase.auth.FirebaseAuth
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun Route.reports(firebaseAuth: FirebaseAuth) {
    get("/") {
        runVerified(firebaseAuth, this.call) {
            call.respond(reportsController.getAllReports())
        }
    }
    post("/new") {
        runVerified(firebaseAuth, call) {
            println(call.request)
            val report = call.receive<ReportCreateModel>()
            println(report)
            call.respond(reportsController.addReport(report))
        }
    }
    get("/{id}") {
        runVerified(firebaseAuth, call) {
            val idS = call.parameters["id"]
            if (idS != null) {
                val response = try {
                    val id = idS.toInt()
                    reportsController.getReportsByID(id)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    null
                }
                if (response != null) {
                    call.respond(response)
                } else call.respond(HttpStatusCode(404, "Not Found"), "Server Error")
            }
        }
    }
    get("/{id}/pdf") {
        runVerified(firebaseAuth, call) {
            val reportID = call.parameters["id"]
            if (reportID != null) {
                val response = try {
                    val reportID = reportID.toInt()
                    reportsController.getReportsByID(reportID)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    null
                }
                if (response != null) {
                    val filePath = response.writeReportToPDF()
                    call.respondFile(File(filePath))
                } else
                    call.respond(HttpStatusCode(404, "Not Found"), "Server Error")
            }
        }
    }
    get("/sent") {
        runVerified(firebaseAuth, call) {phone->
            val response = try {
                val timeStamp = call.parameters["afterTime"]
                reportsController.getReportsByUser(phone, timeStamp?.toLong())
            } catch (exception: Exception) {
                exception.printStackTrace()
                null
            }
            if (response != null) {
                call.respond(response)
            } else call.respond(HttpStatusCode(404, "Not Found"), "Server Error")
        }
    }

    get("{reportID}/getAllObservations/") {
        runVerified(firebaseAuth, call) {
            val reportId = call.parameters["reportID"]
            if (reportId != null) {
                val response = try {
                    val uid = reportId.toInt()
                    reportsController.getAllObservations(uid)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    null
                }
                if (response != null) {
                    call.respond(response)
                } else call.respond(HttpStatusCode(404, "Not Found"), "Server Error")
            }
        }
    }
    get("/assigned"){
        runVerified(firebaseAuth, call){phone->
            val response = try{
                val timeStamp = call.parameters["timeStamp"]?.toLong()
                if(phone != null)
                    reportsController.getAllAssignedReports(phone, timeStamp)
                else
                    null
            } catch (e: Exception){
                println(e.message)
                null
            }
            if(response != null){
                call.respond(response)
            }
        }
    }
}

fun getDateTime(s: Long): String? {
    return try {
        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm")
        val netDate = Date(s)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.toString()
    }
}

fun getDate(s: Long): String? {
    return try {
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val netDate = Date(s)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.toString()
    }
}