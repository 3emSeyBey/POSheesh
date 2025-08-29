package com.appdev.posheesh.ui.sales

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

public class ExcelHandler {
    
    fun exportToExcel(context: Context, database: SQLiteDatabase): File {
        return exportToExcel(context, database, null, null)
    }
    
    fun exportToExcel(context: Context, database: SQLiteDatabase, startDate: String?, endDate: String?): File {
        // Create a workbook
        val workbook = XSSFWorkbook()
        
        // Create sheets
        val salesSheet = workbook.createSheet("Sales Report")
        val inventorySheet = workbook.createSheet("Inventory Report")
        val analyticsSheet = workbook.createSheet("Analytics Summary")
        
        // Export sales data
        exportSalesData(workbook, salesSheet, database, startDate, endDate)
        
        // Export inventory data
        exportInventoryData(workbook, inventorySheet, database)
        
        // Export analytics summary
        exportAnalyticsSummary(workbook, analyticsSheet, database, startDate, endDate)
        
        // Save the workbook to a file in internal storage
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "POSheesh_Report_$timestamp.xlsx"
        val file = File(context.filesDir, fileName)
        
        try {
            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        return file
    }
    
    private fun exportSalesData(workbook: Workbook, sheet: Sheet, database: SQLiteDatabase, startDate: String?, endDate: String?) {
        // Create header style
        val headerStyle = createHeaderStyle(workbook)
        
        // Create headers
        val headers = listOf("Order ID", "Date", "Time", "Total Amount", "Payment Method", "Customer Name")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
        
        // Query sales data
        val query = if (startDate != null && endDate != null) {
            "SELECT salesID, orderDate, orderTime, totalAmount, paymentMethod, customerName FROM sales WHERE isActive = 1 AND orderDate BETWEEN ? AND ? ORDER BY orderDate DESC"
        } else {
            "SELECT salesID, orderDate, orderTime, totalAmount, paymentMethod, customerName FROM sales WHERE isActive = 1 ORDER BY orderDate DESC"
        }
        
        val cursor = if (startDate != null && endDate != null) {
            database.rawQuery(query, arrayOf(startDate, endDate))
        } else {
            database.rawQuery(query, null)
        }
        
        var rowNum = 1
        while (cursor.moveToNext()) {
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(cursor.getInt(0).toDouble())
            row.createCell(1).setCellValue(cursor.getString(1))
            row.createCell(2).setCellValue(cursor.getString(2))
            row.createCell(3).setCellValue(cursor.getDouble(4))
            row.createCell(4).setCellValue(cursor.getString(4))
            row.createCell(5).setCellValue(cursor.getString(5) ?: "")
        }
        cursor.close()
        
        // Auto-size columns
        headers.indices.forEach { sheet.autoSizeColumn(it) }
    }
    
    private fun exportInventoryData(workbook: Workbook, sheet: Sheet, database: SQLiteDatabase) {
        // Create header style
        val headerStyle = createHeaderStyle(workbook)
        
        // Create headers
        val headers = listOf("Product ID", "Product Name", "Category", "Quantity", "Selling Price", "Buying Price", "Status")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }
        
        // Query inventory data
        val query = """
            SELECT p.id, p.name, c.name as category, p.quantity, p.selling_price, p.buying_price, 
                   CASE WHEN p.quantity <= 10 THEN 'Low Stock' WHEN p.quantity = 0 THEN 'Out of Stock' ELSE 'In Stock' END as status
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.isActive = 1
            ORDER BY p.quantity ASC
        """.trimIndent()
        
        val cursor = database.rawQuery(query, null)
        
        var rowNum = 1
        while (cursor.moveToNext()) {
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(cursor.getInt(0).toDouble())
            row.createCell(1).setCellValue(cursor.getString(1))
            row.createCell(2).setCellValue(cursor.getString(2) ?: "")
            row.createCell(3).setCellValue(cursor.getInt(3).toDouble())
            row.createCell(4).setCellValue(cursor.getDouble(4))
            row.createCell(5).setCellValue(cursor.getDouble(5))
            row.createCell(6).setCellValue(cursor.getString(6))
        }
        cursor.close()
        
        // Auto-size columns
        headers.indices.forEach { sheet.autoSizeColumn(it) }
    }
    
    private fun exportAnalyticsSummary(workbook: Workbook, sheet: Sheet, database: SQLiteDatabase, startDate: String?, endDate: String?) {
        // Create header style
        val headerStyle = createHeaderStyle(workbook)
        
        // Create summary section
        var rowNum = 0
        
        // Title
        val titleRow = sheet.createRow(rowNum++)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("POSheesh Business Analytics Summary")
        titleCell.cellStyle = headerStyle
        
        rowNum++ // Empty row
        
        // Date range
        val dateRangeRow = sheet.createRow(rowNum++)
        dateRangeRow.createCell(0).setCellValue("Report Period:")
        if (startDate != null && endDate != null) {
            dateRangeRow.createCell(1).setCellValue("$startDate to $endDate")
        } else {
            dateRangeRow.createCell(1).setCellValue("All Time")
        }
        
        rowNum++ // Empty row
        
        // Key metrics
        val metrics = getKeyMetrics(database, startDate, endDate)
        metrics.forEach { (label, value) ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(label)
            row.createCell(1).setCellValue(value)
        }
        
        rowNum++ // Empty row
        
        // Top products
        val topProductsRow = sheet.createRow(rowNum++)
        topProductsRow.createCell(0).setCellValue("Top Selling Products")
        topProductsRow.getCell(0).cellStyle = headerStyle
        
        val topProducts = getTopProducts(database, startDate, endDate)
        topProducts.forEach { (product, sales) ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(product)
            row.createCell(1).setCellValue(sales.toDouble())
        }
        
        // Auto-size columns
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
    }
    
    private fun getKeyMetrics(database: SQLiteDatabase, startDate: String?, endDate: String?): List<Pair<String, String>> {
        val metrics = mutableListOf<Pair<String, String>>()
        
        // Total sales
        val salesQuery = if (startDate != null && endDate != null) {
            "SELECT SUM(totalAmount) FROM sales WHERE isActive = 1 AND orderDate BETWEEN ? AND ?"
        } else {
            "SELECT SUM(totalAmount) FROM sales WHERE isActive = 1"
        }
        
        val salesCursor = if (startDate != null && endDate != null) {
            database.rawQuery(salesQuery, arrayOf(startDate, endDate))
        } else {
            database.rawQuery(salesQuery, null)
        }
        
        if (salesCursor.moveToFirst()) {
            val totalSales = salesCursor.getDouble(0)
            metrics.add("Total Sales" to "₱${String.format("%.2f", totalSales)}")
        }
        salesCursor.close()
        
        // Total orders
        val ordersQuery = if (startDate != null && endDate != null) {
            "SELECT COUNT(*) FROM sales WHERE isActive = 1 AND orderDate BETWEEN ? AND ?"
        } else {
            "SELECT COUNT(*) FROM sales WHERE isActive = 1"
        }
        
        val ordersCursor = if (startDate != null && endDate != null) {
            database.rawQuery(ordersQuery, arrayOf(startDate, endDate))
        } else {
            database.rawQuery(ordersQuery, null)
        }
        
        if (ordersCursor.moveToFirst()) {
            val totalOrders = ordersCursor.getInt(0)
            metrics.add("Total Orders" to totalOrders.toString())
        }
        ordersCursor.close()
        
        // Average order value
        val avgQuery = if (startDate != null && endDate != null) {
            "SELECT AVG(totalAmount) FROM sales WHERE isActive = 1 AND orderDate BETWEEN ? AND ?"
        } else {
            "SELECT AVG(totalAmount) FROM sales WHERE isActive = 1"
        }
        
        val avgCursor = if (startDate != null && endDate != null) {
            database.rawQuery(avgQuery, arrayOf(startDate, endDate))
        } else {
            database.rawQuery(avgQuery, null)
        }
        
        if (avgCursor.moveToFirst()) {
            val avgOrder = avgCursor.getDouble(0)
            metrics.add("Average Order Value" to "₱${String.format("%.2f", avgOrder)}")
        }
        avgCursor.close()
        
        // Low stock items
        val lowStockCursor = database.rawQuery("SELECT COUNT(*) FROM products WHERE quantity <= 10 AND isActive = 1", null)
        if (lowStockCursor.moveToFirst()) {
            val lowStock = lowStockCursor.getInt(0)
            metrics.add("Low Stock Items" to lowStock.toString())
        }
        lowStockCursor.close()
        
        return metrics
    }
    
    private fun getTopProducts(database: SQLiteDatabase, startDate: String?, endDate: String?): List<Pair<String, Int>> {
        val query = if (startDate != null && endDate != null) {
            """
            SELECT oi.productName, SUM(oi.quantity) as totalSold
            FROM order_items oi
            JOIN sales s ON oi.salesID = s.salesID
            WHERE s.isActive = 1 AND s.orderDate BETWEEN ? AND ?
            GROUP BY oi.productName
            ORDER BY totalSold DESC
            LIMIT 10
            """.trimIndent()
        } else {
            """
            SELECT oi.productName, SUM(oi.quantity) as totalSold
            FROM order_items oi
            JOIN sales s ON oi.salesID = s.salesID
            WHERE s.isActive = 1
            GROUP BY oi.productName
            ORDER BY totalSold DESC
            LIMIT 10
            """.trimIndent()
        }
        
        val cursor = if (startDate != null && endDate != null) {
            database.rawQuery(query, arrayOf(startDate, endDate))
        } else {
            database.rawQuery(query, null)
        }
        
        val products = mutableListOf<Pair<String, Int>>()
        while (cursor.moveToNext()) {
            val productName = cursor.getString(0)
            val totalSold = cursor.getInt(1)
            products.add(productName to totalSold)
        }
        cursor.close()
        
        return products
    }
    
    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.color = IndexedColors.WHITE.index
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.DARK_BLUE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        return style
    }
}