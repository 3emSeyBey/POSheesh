package com.appdev.posheesh.ui.sales

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import android.util.Log
import android.widget.Toast
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

public class ExcelHandler {
    fun exportToExcel(context: Context, database: SQLiteDatabase): File {
        // Create a workbook
        val workbook = XSSFWorkbook()
        // Create a sheet
        val sheet: Sheet = workbook.createSheet("Data")

        // Query your SQLite database to get the data you want to export
        val cursor = database.rawQuery("SELECT * FROM products", null)
        var rowNum = 0
        while (cursor.moveToNext()) {
            val row = sheet.createRow(rowNum++)
            val columnCount = cursor.columnCount
            for (i in 0 until columnCount) {
                val cell = row.createCell(i)
                cell.setCellValue(cursor.getString(i))
            }
        }
        cursor.close()

        // Save the workbook to a file
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "data.xlsx")
        try {
            Toast.makeText(context, file.toString(), Toast.LENGTH_SHORT).show()
            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            outputStream.close()
        } catch (e: IOException) {
            Log.e("ExcelExporter", "Error exporting Excel file", e)
            Toast.makeText(context, "Error exporting Excel file", Toast.LENGTH_SHORT).show()
        } finally {
            cursor.close()
        }
        return file
    }
}