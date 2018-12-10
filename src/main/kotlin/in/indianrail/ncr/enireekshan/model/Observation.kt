package `in`.indianrail.ncr.enireekshan.model

import `in`.indianrail.ncr.enireekshan.TableWriterInterface
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.routes.getDateTime
import be.quodlibet.boxable.BaseTable
import be.quodlibet.boxable.image.Image
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.File
import javax.imageio.IIOException
import javax.imageio.ImageIO


val STATUS_UNSEEN = "unseen"
val STATUS_SEEN = "seen"
val STATUS_COMPLIED = "complied"
val baseDir = File("/home/enireekshan/server-uploads")
val marginList = listOf(5f,20f,14f,10f,8f,8f,35f)
val headerList = listOf("Sl.", "Title", "Assigned To", "Date", "Status", "Urgent", "Images")
data class ObservationModel(
        val assignedToUser: Long,
        val id: Int,
        val reportID: Int,
        val status: String,
        val timestamp: Long,
        val title: String,
        val urgent: Boolean,
        val seenByPCSO: Boolean,
        val seenBySrDSO: Boolean,
        val mediaItems: List<MediaItemsModel>,
        val submittedBy: UserModel
) : TableWriterInterface{
    override fun writeHeaderToPDF(dataTable: BaseTable) : BaseTable{
        val headerRow = dataTable.createRow(15f)
        headerList.forEachIndexed { index, s ->
            val cell = headerRow.createCell(marginList[index], s)
            cell.fillColor = Color.CYAN
        }
        dataTable.addHeaderRow(headerRow)
        return dataTable
    }
    override fun writeTableToPDF(dataTable: BaseTable, index : Int) : BaseTable{
        val marginList = listOf(5f,20f,14f,10f,8f,8f,35f)
        val headerList = listOf("Sl.", "Title", "Assigned To", "Date", "Status", "Urgent", "Images")

        val assignedToUserName = transaction { Users.select { Users.id eq assignedToUser }.map{
            it[Users.name]
        } }
        val row = dataTable.createRow(10f)
        row.createCell(marginList[0], index.toString())
        row.createCell(marginList[1], title)
        row.createCell(marginList[2], assignedToUserName[0])
        row.createCell(marginList[3], getDateTime(timestamp))
        row.createCell(marginList[4], status)
        row.createCell(marginList[5], if (urgent) "YES" else "NO")
        var numberofImages = 0
        mediaItems.forEach {listItem->
            if (listItem.filepath.split(".").last() in setOf("jpg", "jpeg", "png", "bmp", "bpg")) numberofImages+=1
        }
        if(numberofImages > 0) {
            val imageDimension = marginList[6] / numberofImages
            mediaItems.forEach { listItem ->
                if (listItem.filepath.split(".").last() in setOf("jpg", "jpeg", "png", "bmp", "bpg")) {
                    try {
                        val image = Image(ImageIO.read(File(baseDir, listItem.filepath)))
                        image.scaleByWidth(imageDimension)
                        image.scaleByHeight(imageDimension)
                        val cell = row.createImageCell(imageDimension, image)
                        cell.scaleToFit()
                    } catch (e: IIOException) {
                        println(e.message)
                    } catch (e: NullPointerException) {
                        println(e.message)
                    }
                }
            }
        }
        return dataTable
    }

    override fun writeTableToPDF(dataTable: BaseTable) : BaseTable{
        return dataTable
    }

    override fun writeReportToPDF() : String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

data class AssigneeRole(
        val location: String,
        val designation: String,
        val department: String
)

data class ObservationCreateModel(
        val title: String,
        val urgent: Boolean,
        val assignedToUser: Long,
        val reportID: Int,
        val status: String,
        val timestamp: Long,
        val mediaLinks: List<String>,
        val seenByPCSO: Boolean,
        val seenBySrDSO: Boolean
)