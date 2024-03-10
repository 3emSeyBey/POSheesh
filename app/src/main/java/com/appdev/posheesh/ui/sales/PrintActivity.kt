import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.appcompat.app.AppCompatActivity
import com.appdev.posheesh.R
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Table
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class PrintActivity : AppCompatActivity() {

    fun printExcelFile(context: Context, file: File) {
        try {
            val inputStream = FileInputStream(file)
            val workbook = WorkbookFactory.create(inputStream)

            // Create PDF document
            val output = FileOutputStream(context.cacheDir.absolutePath + "/excel_data.pdf")
            val writer = PdfWriter(output)
            val pdf = com.itextpdf.kernel.pdf.PdfDocument(writer)
            val document = Document(pdf)

            // Add data from Excel to a table in the PDF
            val table = Table(10) // Adjust the number of columns as per your Excel structure
            val sheet = workbook.getSheetAt(0) // Assuming you want to print the first sheet
            for (row in sheet) {
                for (cell in row) {
                    val cellContent = when (cell.cellType) {
                        CellType.STRING -> cell.stringCellValue
                        CellType.NUMERIC -> cell.numericCellValue.toString()
                        else -> ""
                    }
                    table.addCell(cellContent)
                }
            }

            document.add(table)

            document.close()

            // Print the PDF document
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = context.getString(R.string.app_name) + " Document"
            printManager.print(jobName, object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    val info = PrintDocumentInfo.Builder(jobName)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<PageRange>,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal,
                    callback: WriteResultCallback
                ) {
                    val input = FileInputStream(context.cacheDir.absolutePath + "/excel_data.pdf")
                    val output = FileOutputStream(destination.fileDescriptor)
                    try {
                        input.copyTo(output)
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.message)
                    } finally {
                        input.close()
                        output.close()
                    }
                }
            }, null)

            workbook.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
