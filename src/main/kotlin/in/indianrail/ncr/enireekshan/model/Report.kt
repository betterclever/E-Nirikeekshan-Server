package `in`.indianrail.ncr.enireekshan.model

import `in`.indianrail.ncr.enireekshan.TableWriterInterface
import `in`.indianrail.ncr.enireekshan.controller.prepareUserModel
import `in`.indianrail.ncr.enireekshan.dao.Users
import `in`.indianrail.ncr.enireekshan.reportsController
import `in`.indianrail.ncr.enireekshan.routes.uploadDir
import be.quodlibet.boxable.BaseTable
import be.quodlibet.boxable.utils.PDStreamUtils
import com.google.common.io.Files
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.File


data class ReportModel(
        val id: Int,
        val submittedBy: Long,
        val inspections: List<InspectionModel>
) : TableWriterInterface{
    override fun writeReportToPDF() : String {
        val filename = "reports-$id.pdf"
        val myPage = PDPage(PDRectangle.A4)
        val mainDocument = PDDocument()
        val contentStream = PDPageContentStream(mainDocument, myPage)
        val font = PDType1Font.HELVETICA
        val leftMargin = myPage.artBox.width * 0.05f
        val titleFontSize = 10.0f
        val yposition = 700.00f
        val submitterModel = transaction {
            `in`.indianrail.ncr.enireekshan.dao.Users.select { Users.id eq submittedBy }.map { usr ->
                usr.prepareUserModel()
            }
        }
        val submitterName = submitterModel[0].name
        val submitterDesignation = submitterModel[0].designation
        val submitterLocation = submitterModel[0].location
        val preTableString = "Report submitted by $submitterName, $submitterDesignation, $submitterLocation"
        PDStreamUtils.write(contentStream, preTableString, font, titleFontSize, leftMargin, yposition, Color.BLACK)
        mainDocument.addPage(myPage)
        val yStart = myPage.artBox.upperRightY * 0.80f
        val yStartNewPage = myPage.artBox.upperRightY * 0.95f
        val bottomMargin = 10.0f
        val tableWidth = myPage.mediaBox.width * 0.9f
        val margin = myPage.mediaBox.width * 0.05f
        var dataTable = BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, mainDocument, myPage, true, true)
        dataTable = this.inspections[0].writeHeaderToPDF(dataTable)
        this.inspections.forEachIndexed { index, inspectionModel ->
            dataTable = inspectionModel.writeTableToPDF(dataTable, index)
        }
        dataTable.draw()
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
        val inspections: List<InspectionCreateModel>
)
