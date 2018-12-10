package `in`.indianrail.ncr.enireekshan.routes

import `in`.indianrail.ncr.enireekshan.model.ReportCreateModel
import `in`.indianrail.ncr.enireekshan.reportsController
import `in`.indianrail.ncr.enireekshan.runVerifed
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

fun Route.reports(firebaseAuth: FirebaseAuth){
    get("/") {
        runVerifed(firebaseAuth, this.call) {
            call.respond(reportsController.getAllReports())
        }
    }
    post("/new") {
        runVerifed(firebaseAuth, call) {
            val report = call.receive<ReportCreateModel>()
            call.respond(reportsController.addReport(report))
        }
    }
    get("/{id}"){
        runVerifed(firebaseAuth, call) {
            val idS = call.parameters["id"]
            if (idS != null) {
                val response = try {
                    val id = idS.toInt()
                    reportsController.getReportsByID(id)
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
    get("/{id}/pdf"){
        runVerifed(firebaseAuth, call) {
            val idS = call.parameters["id"]
            if (idS != null) {
                val response = try {
                    val id = idS.toInt()
                    reportsController.getReportsByID(id)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    null
                }
                if(response!=null) {
                    val filePath = response.writeReportToPDF()
                    call.respondFile(File(filePath))
                } else
                    call.respond(HttpStatusCode(404, "Not Found"), "Server Error")
            }
        }
    }
    get("/getReportsByUser/{userId}"){
        runVerifed(firebaseAuth, call) {
            val uid = call.parameters["userId"]
            if (uid != null) {
                val response = try {
                    val uid = uid.toLong()
                    reportsController.getReportsByUser(uid)
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