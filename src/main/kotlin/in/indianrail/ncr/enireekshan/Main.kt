package `in`.indianrail.ncr.enireekshan

import `in`.indianrail.ncr.enireekshan.controller.InspectionController
import `in`.indianrail.ncr.enireekshan.controller.UserController
import `in`.indianrail.ncr.enireekshan.dao.InspectionAssignees
import `in`.indianrail.ncr.enireekshan.dao.Inspections
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.model.FileInfo
import `in`.indianrail.ncr.enireekshan.model.InspectionCreateModel
import `in`.indianrail.ncr.enireekshan.model.UserModel
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.content.PartData
import io.ktor.content.forEachPart
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.util.error
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.text.DateFormat

fun initDB() {
    val config = HikariConfig("/hikari.properties")
    val ds = HikariDataSource(config)
    Database.connect(ds)

    transaction {
        create(Users, Inspections, InspectionAssignees)
    }
}

val topLevelClass = object : Any() {}.javaClass.enclosingClass
val userController = UserController()

suspend inline fun runVerifed(firebaseAuth: FirebaseAuth, call: ApplicationCall, block: (phone: Long) -> Unit) {
    try {
        val authToken = call.request.headers["Authorization"]
        val decodedToken = firebaseAuth.verifyIdTokenAsync(authToken).get()
        val phone = decodedToken.claims["phone_number"] as String
        val phoneNumber = phone.substringAfter("+91").toLong()
        if (userController.verifyUser(phoneNumber)) block(phoneNumber) else throw Exception("Unverified User")
    } catch (exception: Exception) {
        exception.printStackTrace()
        call.respond(HttpStatusCode(500, "Unauthorized access"), "Unauthorized")
    }
}

fun Application.main() {

    val serviceAccount = topLevelClass.classLoader
            .getResourceAsStream("e-nirikshan-firebase-adminsdk-qj7uv-8f4c1dee28.json")

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

    val uploadDir = File("/home/enireekshan/server-uploads")

    install(Routing) {
        route("/api") {
            route("/users") {
                get("/locations") {
                    runVerifed(firebaseAuth, call, {
                        call.respond(userController.getLocations())
                    })
                }
                get("/{location}/departments") {
                    runVerifed(firebaseAuth, call, {
                        val location = call.parameters["location"]
                        if (location != null) {
                            call.respond(userController.getDepartments(location))
                        } else {
                            call.respond(emptyArray<String>())
                        }
                    })
                }
                get("/{location}/{department}/designations") {
                    runVerifed(firebaseAuth, call, {
                        val location = call.parameters["location"]
                        val department = call.parameters["department"]
                        if (location != null && department != null) {
                            call.respond(userController.getDesignations(department, location))
                        } else {
                            call.respond(emptyArray<String>())
                        }
                    })
                }
                get("/") {
                    runVerifed(firebaseAuth, call, {
                        call.respond(userController.getAllUsers())
                    })
                }
                post("/") {
                    runVerifed(firebaseAuth, call, {
                        val user = call.receive<UserModel>()
                        call.respond(userController.addUser(user))
                    })
                }

                post("/updateFCMToken") {
                    runVerifed(firebaseAuth, call, {
                        val token = call.receive<String>()
                        call.respond(userController.updateFCMToken(it, token))
                    })
                }
            }
            route("/inspections") {
                val inspectionController = InspectionController()

                get("/markedTo/{userID}") {
                    runVerifed(firebaseAuth, call, {
                        val userID = call.parameters["userID"]
                        if (userID != null) {
                            try {
                                val num = userID.toLong()
                                call.respond(inspectionController.getInspectionAssignedTo(num))
                            } catch (e: Exception) {
                                log.error(e)
                            }
                        } else call.respond(emptyArray<Int>())
                    })
                }
                get("/submittedBy/{userID}") {
                    runVerifed(firebaseAuth, call, {
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
                    })
                }

                get("/{id}") {
                    runVerifed(firebaseAuth, call, {
                        val idS = call.parameters["id"]
                        if (idS != null) {
                            val response = try {
                                val id = idS.toInt()
                                inspectionController.getInspectionByID(id)
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                                null
                            }
                            call.respond(response ?: "Not Found")
                        }
                    })
                }

                patch("/{id}/updateStatus") {
                    runVerifed(firebaseAuth, call, {
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
                    })
                }


                post("/new") {
                    runVerifed(firebaseAuth, call, {
                        val inspectionCreateModel = call.receive<InspectionCreateModel>()
                        call.respond(inspectionController.addInspection(inspectionCreateModel))
                    })
                }
            }
            route("/files") {

                post("/new") {

                    runVerifed(firebaseAuth, call, {

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

                    })
                }
            }
        }
    }
}