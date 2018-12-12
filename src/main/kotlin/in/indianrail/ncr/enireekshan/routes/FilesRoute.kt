package `in`.indianrail.ncr.enireekshan.routes

import `in`.indianrail.ncr.enireekshan.runVerified
import com.google.firebase.auth.FirebaseAuth
import io.ktor.application.call
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import java.io.File

val uploadDir = File("/home/enireekshan/server-uploads")

fun Route.files(firebaseAuth: FirebaseAuth){
    val baseDir = File("/home/enireekshan/server-uploads")
    get("/{name}") {
        runVerified(firebaseAuth, call) {
            val name = call.parameters["name"]
            if (name != null) {
                call.respondFile(baseDir, name)
            }
        }
    }

    post("/new") {

        runVerified(firebaseAuth, call) {

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
                        val file = File(uploadDir, uploadName)
                        part.streamProvider().use { its -> file.outputStream().buffered().use { its.copyTo(it) } }
                        videoFile = file
                    }
                }
                part.dispose()
            }

            call.respond(uploadName)
        }
    }
}