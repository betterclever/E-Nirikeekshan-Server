package `in`.indianrail.ncr.enireekshan

import `in`.indianrail.ncr.enireekshan.controller.UserController
import `in`.indianrail.ncr.enireekshan.dao.Inspections
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.model.UserModel
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.DateFormat


fun initDB() {
    val config = HikariConfig("/hikari.properties")
    val ds = HikariDataSource(config)
    Database.connect(ds)

    transaction {
        create(Users, Inspections)
    }
}

fun main(args: Array<String>) {

    embeddedServer(Netty, 8080) {

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

        install(Routing) {
            route("/api") {
                route("/users") {
                    val userController = UserController()
                    get("/") {
                        call.respond(userController.getAllUsers())
                    }
                    post("/") {
                        val user = call.receive<UserModel>()
                        call.respond(userController.addUser(user))
                    }
                }
            }
        }

    }.start(wait = true)
}