package `in`.indianrail.ncr.enireekshan.routes

import `in`.indianrail.ncr.enireekshan.PdfGenrator
import `in`.indianrail.ncr.enireekshan.controller.prepareUserModel
import `in`.indianrail.ncr.enireekshan.dao.Users
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
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionScope
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val pdfGenerator = PdfGenrator()
fun Route.reports(firebaseAuth: FirebaseAuth){
    get("/") {
        runVerifed(firebaseAuth, this.call) {
            call.respond(reportsController.getAllReports())
        }
    }
    post("/") {
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
                    val filename = "reports-" + response.id + ".pdf"
                    val submitterID = response.submittedBy
                    val submitterModel = transaction {
                        Users.select { Users.id eq submitterID }.map { usr ->
                            usr.prepareUserModel()
                        }
                    }
                    val submitterName = submitterModel[0].name
                    val submitterDesignation = submitterModel[0].designation
                    val submitterLocation = submitterModel[0].location
                    val preTableString = "Report submitted by $submitterName, $submitterDesignation, $submitterLocation"
                    val header_map = linkedMapOf(
                            "Sl." to "5",
                            "Title" to "30",
                            "Assigned To" to "20",
                            "Date" to "10",
                            "Status" to "5",
                            "Urgent" to "5",
                            "Images" to "25"
                    )
                    val assignedUserIdList = response.inspections.map {
                        it.assignedToUser
                    }
                    val assignedUserMap = transaction {
                        Users.select { Users.id inList assignedUserIdList }.map {
                            it[Users.id].value to it[Users.name]
                        }.toMap()
                    }
                    val inspections = response.inspections
                    var content = MutableList(response.inspections.size) {
                        mutableListOf("$it"
                                , inspections[it].title
                                , assignedUserMap[inspections[it].assignedToUser.toLong()]
                                , getDateTime(inspections[it].timestamp)
                                , inspections[it].status
                                , inspections[it].urgent.toString()
                                , "Images")
                    }
                    val filePath = pdfGenerator.getPDF(filename, preTableString, header_map, content)
                    println(filePath)
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
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val netDate = Date(s)
        sdf.format(netDate)
    } catch (e: Exception) {
        e.toString()
    }
}