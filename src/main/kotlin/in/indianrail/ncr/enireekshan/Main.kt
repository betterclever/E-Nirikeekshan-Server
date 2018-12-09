package `in`.indianrail.ncr.enireekshan

import `in`.indianrail.ncr.enireekshan.controller.InspectionController
import `in`.indianrail.ncr.enireekshan.controller.ReportsController
import `in`.indianrail.ncr.enireekshan.controller.UserController
import `in`.indianrail.ncr.enireekshan.dao.*
import `in`.indianrail.ncr.enireekshan.model.*
import `in`.indianrail.ncr.enireekshan.routes.files
import `in`.indianrail.ncr.enireekshan.routes.inspections
import `in`.indianrail.ncr.enireekshan.routes.reports
import `in`.indianrail.ncr.enireekshan.routes.users
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
        createMissingTablesAndColumns(Users, Inspections, Messages, MediaItems)
    }
}

val topLevelClass = object : Any() {}.javaClass.enclosingClass
val userController = UserController()
val reportsController = ReportsController()

suspend inline fun runVerifed(firebaseAuth: FirebaseAuth, call: ApplicationCall, block: (phone: Long) -> Unit) {
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
    block(1234567890)
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
    /* Importing Users using firebase Auth */
//    transaction {
//        val authInstance = FirebaseAuth.getInstance()
//        val users = mutableListOf<ImportUserRecord>()
//
//        UserEntity.all().forEach {
//            if(users.size == 999) {
//                val result = authInstance.importUsers(users)
//                println("Import ${users.size} Users")
//                println(result)
//                users.clear()
//            } else {
//                users.add(ImportUserRecord.builder()
//                        .setUid(it.phone.value.toString())
//                        .setPhoneNumber("+91${it.phone.value}")
//                        .setDisplayName(it.name)
//                        .build()
//                )
//            }
//        }

//        println("Import ${users.size} Users")
//        val result = authInstance.importUsers(users)
//        println(result)
//        users.clear()
//    }
    install(Routing) {
        route("/api") {
            route("/users") {
                users(firebaseAuth)
            }
            route("/inspections") {
                inspections(firebaseAuth)
            }
            route("/files") {
                files(firebaseAuth)
            }
            route("reports") {
                reports(firebaseAuth)
            }
        }
    }
}

