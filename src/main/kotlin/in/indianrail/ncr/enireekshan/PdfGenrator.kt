package `in`.indianrail.ncr.enireekshan

import `in`.indianrail.ncr.enireekshan.model.MediaItemsModel
import `in`.indianrail.ncr.enireekshan.routes.uploadDir
import be.quodlibet.boxable.BaseTable
import be.quodlibet.boxable.image.Image
import be.quodlibet.boxable.utils.PDStreamUtils
import com.google.common.io.Files
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.jetbrains.exposed.sql.exposedLogger
import java.awt.Color
import java.io.File
import java.util.*
import javax.imageio.IIOException
import javax.imageio.ImageIO

class PdfGenrator {
    val baseDir = File("/home/enireekshan/server-uploads")
    fun getPDF(filenme: String,startWithString: String, header_map: LinkedHashMap<String, String>, content: MutableList<MutableList<Any?>>): String{
        val myPage = PDPage(PDRectangle.A4)
        val mainDocument = PDDocument()
        val contentStream = PDPageContentStream(mainDocument, myPage)
        val font = PDType1Font.HELVETICA
        val leftMargin = myPage.artBox.width * 0.05f
        val titleFontSize = 10.0f
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
                if (s is List<*>){
                    s.forEach {listItem->
                        if ( listItem is MediaItemsModel) {
                            val imageDimension = cellWidthList[index] / s.size
                            if (listItem.filepath.split(".").last() in setOf("jpg", "jpeg", "png", "bmp", "bpg")) {
                                try{
                                    val f = File(baseDir, listItem.filepath)
                                    println("basedir $baseDir \n filepath ${listItem.filepath}")
                                    println("file $f")
                                    val bi = ImageIO.read(f)
                                    println("bi: $bi")
                                    val image = Image(bi)
                                    image.scaleByWidth(imageDimension)
                                    image.scaleByHeight(imageDimension)
                                    val cell = row.createImageCell(imageDimension, image)
                                } catch (e : IIOException){
                                    println(e.message)
                                } catch(e: NullPointerException){
                                    println(e.message)
                                }
                            }
                        }
                    }
                } else if( s is String){
                    val cell = row.createCell(cellWidthList[index], s)
                } else if ( s is Int){
                    val cell = row.createCell(cellWidthList[index], s.toString())
                } else if (s is Long){
                    val cell = row.createCell(cellWidthList[index], s.toString())
                }
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