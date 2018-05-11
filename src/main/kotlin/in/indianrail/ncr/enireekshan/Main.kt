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
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.content.PartData
import io.ktor.content.forEachPart
import io.ktor.features.*
import io.ktor.gson.gson
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

fun Application.main() {

    val serviceAccount = topLevelClass.classLoader
            .getResourceAsStream("e-nirikshan-firebase-adminsdk-qj7uv-8f4c1dee28.json")
    //val serviceAccount = FileInputStream("e-nirikshan-firebase-adminsdk-qj7uv-8f4c1dee28.json")

    val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://e-nirikshan.firebaseio.com")
            .build()

    FirebaseApp.initializeApp(options)

    val firebaseAuth = FirebaseAuth.getInstance()
    //val idToken = ""
    //val decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(idToken).get()
    //val uid = decodedToken.uid

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

        val uploadDir = File("/home/betterclever/Videos")

        install(Routing) {
            route("/api") {
                route("/users") {
                    val userController = UserController()
                    get("/locations") {
                        //val auth = call.request.headers["Authorization"]
                        //println("auth: $auth")
                        call.respond(userController.getLocations())
                    }
                    get("/{location}/departments") {
                        //val authToken = call.request.headers["Authorization"]

                        //val decodedToken = firebaseAuth.verifyIdTokenAsync(authToken).get()
                        //val uid = decodedToken.uid
                        //println(uid)

                        val location = call.parameters["location"]
                        if (location != null) {
                            call.respond(userController.getDepartments(location))
                        } else {
                            call.respond(emptyArray<String>())
                        }
                    }
                    get("/{location}/{department}/designations") {
                        val location = call.parameters["location"]
                        val department = call.parameters["department"]
                        if (location != null && department != null) {
                            call.respond(userController.getDesignations(department, location))
                        } else {
                            call.respond(emptyArray<String>())
                        }
                    }
                    get("/") {
                        call.respond(userController.getAllUsers())
                    }
                    post("/") {
                        val user = call.receive<UserModel>()
                        call.respond(userController.addUser(user))
                    }
                }
                route("/inspections") {
                    val inspectionController = InspectionController()

                    get("/markedTo/{userID}") {
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
                    get("/submittedBy/{userID}") {
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

                    get("/{id}") {
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
                    }

                    patch("/{id}/updateStatus") {
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


                    post("/new") {
                        val inspectionCreateModel = call.receive<InspectionCreateModel>()
                        call.respond(inspectionController.addInspection(inspectionCreateModel))
                    }
                }
                route("/files") {
                    post("/new") {
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
    }
}