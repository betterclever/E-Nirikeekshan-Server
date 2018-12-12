package `in`.indianrail.ncr.enireekshan.model

import `in`.indianrail.ncr.enireekshan.TableWriterInterface
import `in`.indianrail.ncr.enireekshan.controller.prepareUserModel
import `in`.indianrail.ncr.enireekshan.createStringFromCollection
import `in`.indianrail.ncr.enireekshan.dao.UserEntity
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.routes.getDate
import `in`.indianrail.ncr.enireekshan.routes.uploadDir
import be.quodlibet.boxable.BaseTable
import be.quodlibet.boxable.utils.PDStreamUtils
import com.google.common.io.Files
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.File
import java.lang.Float.max

data class ReportModel(
        val id: Int,
        val submittedBy: Long,
        val observations: List<ObservationModel>,
        val timestamp: Long,
        val title: String
) : TableWriterInterface{
    override fun writeReportToPDF() : String = transaction{
        val filename = "report-$id.pdf"
        val myPage = PDPage(PDRectangle.A4)
        val mainDocument = PDDocument()
        val contentStream = PDPageContentStream(mainDocument, myPage)
        val font = PDType1Font.HELVETICA
        val leftMargin = myPage.artBox.width * 0.05f
        val titleFontSize = 10.0f
        var yposition = myPage.artBox.height * 0.95f
        val submitterModel = transaction {
            `in`.indianrail.ncr.enireekshan.dao.Users.select { Users.id eq submittedBy }.map { usr ->
                usr.prepareUserModel()
            }
        }
        val submitterDesignation = submitterModel[0].designation + ", " + submitterModel[0].location
        val preTableString = "INSPECTION OF ${title.toUpperCase()} BY UNDERSIGNED ON ${getDate(timestamp)}"
        PDStreamUtils.write(contentStream, preTableString, font, titleFontSize, leftMargin, yposition, Color.BLACK)
        mainDocument.addPage(myPage)
        val yStart = myPage.artBox.upperRightY * 0.90f
        val yStartNewPage = myPage.artBox.upperRightY * 0.93f
        val bottomMargin = 10.0f
        val tableWidth = myPage.mediaBox.width * 0.9f
        val margin = myPage.mediaBox.width * 0.05f
        var dataTable = BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, mainDocument, myPage, true, true)
        dataTable = this@ReportModel.observations[0].writeHeaderToPDF(dataTable)
        var copyToUsers = mutableSetOf<String>()
        this@ReportModel.observations.forEachIndexed { index, observationModel ->
            dataTable = observationModel.writeTableToPDF(dataTable, index)
            observationModel.assignedToUsers.forEach {
                copyToUsers.add(UserEntity[it.assignedUser].designation)
            }
        }
        yposition = max(yStart - dataTable.headerAndDataHeight - 50f, myPage.artBox.height * 0.4f)
        dataTable.draw()
        val submitterName = "( ${submitterModel[0].name} )"
        PDStreamUtils.write(contentStream, submitterName, font, titleFontSize, myPage.artBox.width * 0.68f, yposition, Color.BLACK)
        yposition -= 20f
        val diffStringLength = ( submitterName.length.toFloat() - submitterDesignation.length.toFloat() ) * 2.0f
        PDStreamUtils.write(contentStream, submitterDesignation, font, titleFontSize, myPage.artBox.width * 0.68f + diffStringLength, yposition + 10f, Color.BLACK)
        yposition -= 20f
        PDStreamUtils.write(contentStream, "COPY: " + createStringFromCollection(copyToUsers), font, titleFontSize, margin, yposition + 10f, Color.BLACK)
        contentStream.close()
        val file = File(uploadDir, filename)
        Files.createParentDirs(file)
        println("Sample file saved at : " + file.getAbsolutePath())
        mainDocument.save(file)
        mainDocument.close()
        file.absolutePath
    }

    override fun writeHeaderToPDF(baseTable: BaseTable): BaseTable {
        return baseTable
    }

    override fun writeTableToPDF(baseTable: BaseTable): BaseTable {
        return baseTable
    }

    override fun writeTableToPDF(baseTable: BaseTable, index: Int): BaseTable {
        return baseTable
    }
}

data class ReportCreateModel(
        val submittedBy: Long,
        val timestamp: Long,
        val observations: List<ObservationCreateModel>,
        val title: String
)

