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
val marginList = listOf(10f,60f,30f)
val headerList = listOf("Sl.", "Title", "Assigned To")
data class ObservationModel(
        val assignedToUser: List<Long>,
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
        val assignedToUserNameList = transaction { Users.select { Users.id inList  assignedToUser.map { it } }.map{
            it[Users.designation]
        } }
        var assignedUserString = ""
        for(i in 0 until assignedToUserNameList.size){
             assignedUserString += assignedToUserNameList[i]
        }
        assignedUserString += assignedToUserNameList[assignedToUserNameList.size - 1]
        val row = dataTable.createRow(10f)
        row.createCell(marginList[0], index.toString())
        row.createCell(marginList[1], title)
        row.createCell(marginList[2], assignedUserString)
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
        val assignedToUser: List<Long>,
        val timestamp: Long,
        val mediaLinks: List<String>
)