package `in`.indianrail.ncr.enireekshan.model

import `in`.indianrail.ncr.enireekshan.TableWriterInterface
import `in`.indianrail.ncr.enireekshan.controller.prepareUserModel
import `in`.indianrail.ncr.enireekshan.createStringFromCollection
import `in`.indianrail.ncr.enireekshan.dao.UserEntity
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.routes.getDateTime
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


data class ReportModel(
        val id: Int,
        val submittedBy: Long,
        val observations: List<ObservationModel>,
        val timestamp: Long,
        val title: String
) : TableWriterInterface{
    override fun writeReportToPDF() : String {
        val filename = "reports-$id.pdf"
        val myPage = PDPage(PDRectangle.A4)
        val mainDocument = PDDocument()
        val contentStream = PDPageContentStream(mainDocument, myPage)
        val font = PDType1Font.HELVETICA
        val leftMargin = myPage.artBox.width * 0.05f
        val titleFontSize = 10.0f
        var yposition = myPage.artBox.height * 0.05f
        val submitterModel = transaction {
            `in`.indianrail.ncr.enireekshan.dao.Users.select { Users.id eq submittedBy }.map { usr ->
                usr.prepareUserModel()
            }
        }
        val submitterDesignation = submitterModel[0].designation + ", " + submitterModel[0].location
        val preTableString = "INSPECTION OF ${title.toUpperCase()} BY UNDERSIGNED ON ${getDateTime(timestamp)}"
        PDStreamUtils.write(contentStream, preTableString, font, titleFontSize, leftMargin, yposition, Color.BLACK)
        mainDocument.addPage(myPage)
        val yStart = myPage.artBox.upperRightY * 0.80f
        val yStartNewPage = myPage.artBox.upperRightY * 0.95f
        val bottomMargin = 10.0f
        val tableWidth = myPage.mediaBox.width * 0.9f
        val margin = myPage.mediaBox.width * 0.05f
        var dataTable = BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, mainDocument, myPage, true, true)
        dataTable = this.observations[0].writeHeaderToPDF(dataTable)
        var copyToUsers = mutableSetOf<String>()
        this.observations.forEachIndexed { index, observationModel ->
            dataTable = observationModel.writeTableToPDF(dataTable, index)
            observationModel.assignedToUsers.forEach{
                copyToUsers.add(UserEntity[it].designation)
            }
        }
        val numberOfPages = dataTable.document.numberOfPages
        yposition = dataTable.document.getPage(numberOfPages).artBox.height
        dataTable.draw()
        val submitterName = "( ${submitterModel[0].name} )"
        PDStreamUtils.write(contentStream, submitterName, font, titleFontSize, myPage.artBox.width * 0.9f, yposition + 10f, Color.BLACK)
        yposition += 10f
        PDStreamUtils.write(contentStream, submitterDesignation, font, titleFontSize, myPage.artBox.width * 0.9f, yposition + 10f, Color.BLACK)
        yposition += 10f
        PDStreamUtils.write(contentStream, "COPY: " + createStringFromCollection(copyToUsers), font, titleFontSize, myPage.artBox.width * 0.1f, yposition + 10f, Color.BLACK)
        contentStream.close()
        val file = File(uploadDir, filename)
        Files.createParentDirs(file)
        println("Sample file saved at : " + file.getAbsolutePath())
        mainDocument.save(file)
        mainDocument.close()
        return file.absolutePath
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

