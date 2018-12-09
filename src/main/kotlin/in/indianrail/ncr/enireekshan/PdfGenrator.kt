package `in`.indianrail.ncr.enireekshan

import `in`.indianrail.ncr.enireekshan.routes.uploadDir
import be.quodlibet.boxable.datatable.DataTable
import be.quodlibet.boxable.BaseTable
import be.quodlibet.boxable.Cell
import be.quodlibet.boxable.Row
import be.quodlibet.boxable.utils.PDStreamUtils
import com.google.common.io.Files
import com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.awt.Color
import java.io.File
import java.util.Arrays
import java.util.ArrayList
import java.util.Arrays.asList
import com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table
import org.jsoup.Connection
import com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table

class PdfGenrator {
    val baseDir = File("/home/enireekshan/server-uploads")
    fun getPDF(filenme: String,startWithString: String, header_map: LinkedHashMap<String, String>, content: MutableList<MutableList<String?>>): String{
        val myPage = PDPage(PDRectangle.A4)
        val mainDocument = PDDocument()
        val contentStream = PDPageContentStream(mainDocument, myPage)
        val font = PDType1Font.HELVETICA
        val leftMargin = myPage.artBox.width * 0.05f
        val titleFontSize = 10.0f
        val submittedBy = "ABC"
        val yposition = 700.00f
//        val startWithString = "Details of the Report submitted by " + submittedBy
        PDStreamUtils.write(contentStream, startWithString, font, titleFontSize, leftMargin, yposition, Color.BLACK)
        mainDocument.addPage(myPage)
        val yStart = myPage.artBox.upperRightY * 0.80f
        val yStartNewPage = myPage.artBox.upperRightY * 0.95f
        val bottomMargin = 10.0f
        val tableWidth = myPage.mediaBox.width * 0.9f
        val margin = myPage.mediaBox.width * 0.05f
        val dataTable = BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, mainDocument, myPage, true, true)
//        val header_map = linkedMapOf(
//                "header1" to "10",
//                "header2" to "10",
//                "header3" to "30",
//                "header4" to "50"
//        )
        val cellWidthList = mutableListOf<Float>()
        val headerRow = dataTable.createRow(15f)
        for ((k, v) in header_map) {
            val cell = headerRow.createCell(v.toFloat(), k)
            cellWidthList.add(v.toFloat())
            cell.fillColor = Color.CYAN
        }
        dataTable.addHeaderRow(headerRow)

//        var content = MutableList(100) {
//            mutableListOf("Data $it", "Some Value", "abcd", "abcj")
//        }

        content.forEach {
            val row = dataTable.createRow(10f)
            it.forEachIndexed { index, s ->
                val cell = row.createCell(cellWidthList[index], s)
            }
        }
        dataTable.draw()
        contentStream.close()
        val file = File(uploadDir, filenme)
        Files.createParentDirs(file)
        println("Sample file saved at : " + file.getAbsolutePath())
        mainDocument.save(file)
        mainDocument.close()
        return file.absolutePath
    }
}