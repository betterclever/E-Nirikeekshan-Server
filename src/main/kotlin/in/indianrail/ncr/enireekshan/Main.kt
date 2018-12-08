package `in`.indianrail.ncr.enireekshan

import `in`.indianrail.ncr.enireekshan.controller.InspectionController
import `in`.indianrail.ncr.enireekshan.controller.ReportsController
import `in`.indianrail.ncr.enireekshan.controller.UserController
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.*
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ImportUserRecord
import com.google.firebase.auth.UserRecord
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.content.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.*
import io.ktor.util.error
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.createMissingTablesAndColumns
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.text.DateFormat

fun initDB() {
    val config = HikariConfig("/hikari.properties")
    val ds = HikariDataSource(config)
    Database.connect(ds)

    transaction {
        create(Users, Inspections, Messages, MediaItems)
    }
}

val topLevelClass = object : Any() {}.javaClass.enclosingClass
val userController = UserController()
val reportsController = ReportsController()

suspend inline fun runVerifed(firebaseAuth: FirebaseAuth, call: ApplicationCall, block: (phone: Long) -> Unit) {

    block(1234567890)
    /*try {
        val authToken = call.request.headers["Authorization"]
        val decodedToken = firebaseAuth.verifyIdTokenAsync(authToken).get()
        val phone = decodedToken.claims["phone_number"] as String
        val phoneNumber = phone.substringAfter("+91").toLong()
        if (userController.verifyUser(phoneNumber)) block(phoneNumber) else throw Exception("Unverified User")
    } catch (exception: Exception) {
        exception.printStackTrace()
        call.respond(HttpStatusCode(403, "Unauthorized access"), "Unauthorized")
    }*/
}

fun Application.main() {

    val serviceAccount = topLevelClass.classLoader
            .getResourceAsStream("e-nirikshan-firebase-adminsdk-qj7uv-a17a5c4ee5.json")

    val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://e-nirikshan.firebaseio.com")
            .build()

    FirebaseApp.initializeApp(options)
    val firebaseAuth = FirebaseAuth.getInstance()

    install(Compression)
    install(CORS) {
        anyHost()
    }
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }
    initDB()
    transaction {
        val authInstance = FirebaseAuth.getInstance()
        val users = mutableListOf<ImportUserRecord>()

        UserEntity.all().forEach {
            if(users.size == 999) {
                val result = authInstance.importUsers(users)
                println("Import ${users.size} Users")
                println(result)
                users.clear()
            } else {
                users.add(ImportUserRecord.builder()
                        .setUid(it.phone.value.toString())
                        .setPhoneNumber("+91${it.phone.value}")
                        .setDisplayName(it.name)
                        .build()
                )
            }
        }

        println("Import ${users.size} Users")
        val result = authInstance.importUsers(users)
        println(result)
        users.clear()
    }

    val uploadDir = File("/home/enireekshan/server-uploads")

    install(Routing) {
        route("/api") {
            route("/users") {
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


                post("/updateFCMToken") {
                    runVerifed(firebaseAuth, call) {
                        val token = call.receive<String>()
                        call.respond(userController.updateFCMToken(it, token))
                    }
                }
            }
            route("/inspections") {
                val inspectionController = InspectionController()

                get("/markedTo/{userID}") {
                    runVerifed(firebaseAuth, call) {
                        val userID = call.parameters["userID"]
                        if (userID != null) {
                            try {
                                val num = userID.toLong()
                                call.respond(inspectionController.getInspectionAssignedTo(num))
                            } catch (e: Exception) {
                                log.error(e)
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
                                log.error(e)
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

                post("/{id}/messages") {
                    runVerifed(firebaseAuth, call) {
                        val idS = call.parameters["id"]
                        val message = call.receive<String>()
                        if(idS!=null) {
                            try {
                                val id = idS.toInt()
                                call.respond(inspectionController.addMessage(id, message, it))
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


                post("/new") {
                    runVerifed(firebaseAuth, call) {
                        val inspectionCreateModel = call.receive<InspectionCreateModel>()
                        println(inspectionCreateModel)
                        call.respond(inspectionController.addInspection(inspectionCreateModel))
                    }
                }
            }
            route("/files") {

                val baseDir =File("/home/enireekshan/server-uploads")
                get("/{name}") {
                    runVerifed(firebaseAuth, call, {
                        val name = call.parameters["name"]
                        if (name != null) {
                            call.respondFile(baseDir, name)
                        }
                    })
                }

                post("/new") {

                    runVerifed(firebaseAuth, call) {

                        val multipart = call.receiveMultipart()
                        var title = ""
                        var videoFile: File? = null

                        var uploadName = ""

                        multipart.forEachPart { part ->
                            when (part) {
                                is PartData.FormItem -> {
                                    title = part.value
                                    println(part.value)
                                }
                                is PartData.FileItem -> {
                                    val ext = File(part.originalFileName).extension
                                    uploadName = "upload-${System.currentTimeMillis()}-" +
                                            "${part.originalFileName!!.hashCode()}.$ext"
                                    val file = File(uploadDir, "upload-${System.currentTimeMillis()}-" +
                                            "${part.originalFileName!!.hashCode()}.$ext")
                                    part.streamProvider().use { its -> file.outputStream().buffered().use { its.copyTo(it) } }
                                    videoFile = file
                                }
                            }
                            part.dispose()
                        }

                        call.respond(FileInfo(
                                type = "photo",
                                storageRef = uploadName
                        ))

                    }
                }
            }
            route("/reports"){
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
        }
    }
}
