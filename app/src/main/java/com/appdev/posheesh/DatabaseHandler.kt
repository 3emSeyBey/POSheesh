package com.appdev.posheesh

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.appdev.posheesh.Classes.Category
import com.appdev.posheesh.Classes.Products
import com.appdev.posheesh.Classes.Supplies
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Random

class DatabaseHandler(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    init {
        // Force database creation/upgrade
        writableDatabase
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        // Create the categories table
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL" +
                    ")"
        )

        // Populate the categories table with dummy entries
        db.execSQL("INSERT INTO categories (name) VALUES ('P39 Coffee')")

        // Create the products table
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS products (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "isActive INTEGER DEFAULT 1," +
                    "category_id INTEGER NOT NULL," +
                    "quantity INTEGER DEFAULT 0," +
                    "selling_price REAL DEFAULT 0.0," +
                    "buying_price REAL DEFAULT 0.0," +
                    "image_url TEXT," +
                    "code TEXT," +
                    "required_supplies TEXT," +
                    "FOREIGN KEY (category_id) REFERENCES categories(id)" +
                    ")"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS users (" +
                    "userid INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "employeeId TEXT," +
                    "usercode TEXT NOT NULL," +
                    "role TEXT NOT NULL," +
                    "isActive INTEGER DEFAULT 1," +
                    "creationDate TEXT" +
                    ")"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS supplies (" +
                    "supplyID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "supplyName TEXT," +
                    "supplyDescription TEXT NOT NULL," +
                    "img_url TEXT NOT NULL," +
                    "supplyQuantity INTEGER DEFAULT 0," +
                    "supplyUnit TEXT NOT NULL," +
                    "crticalLevel INTEGER DEFAULT 0," +
                    "isActive INTEGER DEFAULT 1," +
                    "creationDate TEXT" +
                    ")"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS sales (" +
                    "salesID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "totalAmount REAL NOT NULL," +
                    "orderDate TEXT NOT NULL," +
                    "orderTime TEXT NOT NULL," +
                    "paymentMethod TEXT NOT NULL," +
                    "customerName TEXT," +
                    "isActive INTEGER DEFAULT 1," +
                    "creationDate TEXT" +
                    ")"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS order_items (" +
                    "orderItemID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "salesID INTEGER NOT NULL," +
                    "productCode TEXT NOT NULL," +
                    "productName TEXT NOT NULL," +
                    "quantity INTEGER NOT NULL," +
                    "unitPrice REAL NOT NULL," +
                    "totalPrice REAL NOT NULL," +
                    "FOREIGN KEY (salesID) REFERENCES sales(salesID)" +
                    ")"
        )

        // Insert a record for the first user with admin role
        val firstUserCode = "admin"
        val currentTime = System.currentTimeMillis()
        val contentValues = ContentValues().apply {
            put("usercode", firstUserCode)
            put("employeeId", "000001")
            put("role", "admin")
            put("creationDate", currentTime.toString())
        }

        db.insert("users", null, contentValues)

        // Insert product entries
        val products = mutableListOf<Products>()

        // Add the product entries to the list
        val icedCaramelUri = saveImageToInternalStorage(R.drawable.iced_caramel)
        val icedCaramelPath = icedCaramelUri?.let { getFilePathFromUri(context, it) }
        icedCaramelPath?.let {
            products.add(Products(name = "Iced Caramel Macchiatos", description = "Wapako katilaw ani, libre pls hehe",
                categoryId = 1, sellingPrice = 39.0, imageUri = it, code = "P001"))
        }

        val donDarkoUri = saveImageToInternalStorage(R.drawable.don_darko)
        val donDarkoPath = donDarkoUri?.let { getFilePathFromUri(context, it) }
        donDarkoPath?.let {
            products.add(Products(name = "Don Darko", description = "tsikolet", categoryId = 1, sellingPrice = 39.0,
                imageUri = it, code = "P002"))
        }

        val donyaBerryUri = saveImageToInternalStorage(R.drawable.donya_berry)
        val donyaBerryPath = donyaBerryUri?.let { getFilePathFromUri(context, it) }
        donyaBerryPath?.let {
            products.add(Products(name = "Donya Berry", description = "Kape nga penkpenk", categoryId = 1, sellingPrice = 39.0,
                imageUri = it, code = "P003"))
        }

        val donMatchattoUri = saveImageToInternalStorage(R.drawable.don_matchato)
        val donMatchattoPath = donMatchattoUri?.let { getFilePathFromUri(context, it) }
        donMatchattoPath?.let {
            products.add(Products(name = "Don Matchatto", description = "Green nga Kape gikan Japan", categoryId = 1, sellingPrice = 39.0,
                imageUri = it, code = "P004"))
        }

        // Insert product entries into the database
        for (product in products) {
            val contentValues = ContentValues().apply {
                put("name", product.name)
                put("description", product.description)
                put("category_id", product.categoryId)
                put("selling_price", product.sellingPrice)
                put("image_url", product.imageUri.toString()) // Convert Uri to string
                put("code", product.code)
            }

            db.insert("products", null, contentValues)
        }

        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('Plastic Cups', 'Disposable plastic cups for serving beverages', 'https://example.com/plastic_cups_image.jpg', 500, 'piece', 50, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('Straw', 'Disposable straws for drinking', 'https://example.com/straw_image.jpg', 500, 'piece', 50, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('Chocolate Syrup', 'Chocolate-flavored syrup for flavoring beverages', 'https://example.com/chocolate_syrup_image.jpg', 5000, 'ml', 500, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('Strawberry Jam', 'Strawberry-flavored jam for flavoring beverages', 'https://example.com/strawberry_jam_image.jpg', 5000, 'ml', 500, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('Matcha Syrup', 'Matcha-flavored syrup for flavoring beverages', 'https://example.com/matcha_syrup_image.jpg', 5000, 'ml', 500, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('Ice', 'Ice cubes for chilling beverages', 'https://example.com/ice_image.jpg', 10000, 'ml', 1000, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('Coffee', 'Coffee concentrate for making coffee-based beverages', 'https://example.com/coffee_image.jpg', 5000, 'ml', 500, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('Milk', 'Milk for making milk-based beverages', 'https://example.com/milk_image.jpg', 10000, 'ml', 1000, 1, CURRENT_TIMESTAMP);")

        // Insert sample sales data for analytics
        val analyticsTime = System.currentTimeMillis()
        val analyticsCalendar = Calendar.getInstance()
        val random = Random()
        
        // Insert sales for the last 7 days
        for (i in 6 downTo 0) {
            analyticsCalendar.timeInMillis = analyticsTime
            analyticsCalendar.add(Calendar.DAY_OF_YEAR, -i)
            val orderDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(analyticsCalendar.time)
            val orderTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(analyticsCalendar.time)
            
            // Generate random sales data
            val totalAmount = 500.0 + random.nextDouble() * 1500.0 // Random between 500.0 and 2000.0
            val paymentMethod = if (i % 2 == 0) "Cash" else "QR"
            val customerNumber = 1001 + random.nextInt(8999) // Random between 1001 and 9999
            val customerName = "Customer $customerNumber"
            
            val salesContentValues = ContentValues().apply {
                put("totalAmount", totalAmount)
                put("orderDate", orderDate)
                put("orderTime", orderTime)
                put("paymentMethod", paymentMethod)
                put("customerName", customerName)
                put("creationDate", analyticsTime.toString())
            }
            
            val salesId = db.insert("sales", null, salesContentValues)
            
            // Insert order items for each sale
            val products = listOf("P001", "P002", "P003", "P004")
            val randomProductCount = 1 + random.nextInt(3) // Random between 1 and 3
            val randomProducts = products.shuffled().take(randomProductCount)
            
            for (productCode in randomProducts) {
                val product = getProductByCode(productCode)
                if (product != null) {
                    val quantity = 1 + random.nextInt(3) // Random between 1 and 3
                    val unitPrice = product.sellingPrice
                    val totalPrice = unitPrice * quantity
                    
                    val orderItemContentValues = ContentValues().apply {
                        put("salesID", salesId)
                        put("productCode", productCode)
                        put("productName", product.name)
                        put("quantity", quantity)
                        put("unitPrice", unitPrice)
                        put("totalPrice", totalPrice)
                    }
                    
                    db.insert("order_items", null, orderItemContentValues)
                }
            }
        }
        
    }
    
    fun addUser(role: String, employeeId: String): String? {
        val userCode = generateUniqueUserCode()
        return if (userCode != null) {
            val currentTime = System.currentTimeMillis()
            val contentValues = ContentValues().apply {
                put("usercode", userCode)
                put("role", role)
                put("employeeId", employeeId)
                put("creationDate", currentTime.toString())
            }

            val db = writableDatabase
            val success = db.insert("users", null, contentValues) != -1L
            db.close()

            if (success) userCode else null
        } else {
            null // Handle case where unique user code could not be generated
        }
    }

    @SuppressLint("Range")
    private fun generateUniqueUserCode(): String? {
        val db = readableDatabase
        val userCodes = mutableListOf<String>()

        // Query all existing user codes
        val cursor = db.rawQuery("SELECT usercode FROM users", null)
        while (cursor.moveToNext()) {
            val userCode = cursor.getString(cursor.getColumnIndex("usercode"))
            userCodes.add(userCode)
        }
        cursor.close()
        db.close()

        // Generate a random 6-digit number string and check if it's unique
        var randomUserCode: String
        do {
            randomUserCode = generateUserCode()
        } while (userCodes.contains(randomUserCode))

        return randomUserCode
    }
    private fun generateUserCode(): String {
        // Generate a random 6-digit number string
        val random = Random()
        return (100000 + random.nextInt(900000)).toString()
    }
    private fun saveImageToInternalStorage(resourceId: Int): Uri? {
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        val imagesDir = context.filesDir
        val imageFile = File(imagesDir, "image_${System.currentTimeMillis()}.jpg")

        return try {
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                Uri.fromFile(imageFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if necessary
    }
    fun getAllCategory(): MutableList<Category> {
        val categoryList = mutableListOf<Category>()
        val db = readableDatabase
        val cursor: Cursor? = db.rawQuery("SELECT * FROM categories", null)
        cursor?.use {
            val idColumnIndex = it.getColumnIndex("id")
            val nameColumnIndex = it.getColumnIndex("name")
            while(it.moveToNext()) {
                val id = it.getInt(idColumnIndex)
                val name = it.getString(nameColumnIndex)
                val category = Category(id, name)
                categoryList.add(category)
            }
        }
        cursor?.close()
        db.close()
        return categoryList
    }
    fun getProductsByCategoryId(categoryId: Int): MutableList<Products> {
        val productList = mutableListOf<Products>()
        val db = readableDatabase
        var cursor: Cursor? = null

        if (categoryId == 0) {
            cursor = db.rawQuery("SELECT * FROM products", null)
        } else {
            val selectionArgs = arrayOf(categoryId.toString())
            cursor = db.rawQuery("SELECT * FROM products WHERE category_id = ?", selectionArgs)
        }

        cursor?.use {
            val idColumnIndex = it.getColumnIndex("id")
            val nameColumnIndex = it.getColumnIndex("name")
            val descriptionColumnIndex = it.getColumnIndex("description")
            val isActiveColumnIndex = it.getColumnIndex("isActive")
            val categoryIdColumnIndex = it.getColumnIndex("category_id")
            val sellingPriceColumnIndex = it.getColumnIndex("selling_price")
            val imageUrlColumnIndex = it.getColumnIndex("image_url")
            val codeColumnIndex = it.getColumnIndex("code")

            while (it.moveToNext()) {
                val id = it.getInt(idColumnIndex)
                val name = it.getString(nameColumnIndex)
                val description = it.getString(descriptionColumnIndex)
                val isActive = it.getInt(isActiveColumnIndex) == 1 // Assuming 1 for true, 0 for false
                val categoryId = it.getInt(categoryIdColumnIndex)
                val sellingPrice = it.getDouble(sellingPriceColumnIndex)
                val imageUrl = it.getString(imageUrlColumnIndex)
                val code = it.getString(codeColumnIndex)

                // Log the values of properties before creating the Products object
                Log.d("ProductProperties", "ID: $id")
                Log.d("ProductProperties", "Name: $name")
                Log.d("ProductProperties", "Description: $description")
                Log.d("ProductProperties", "isActive: $isActive")
                Log.d("ProductProperties", "Category ID: $categoryId")
                Log.d("ProductProperties", "Selling Price: $sellingPrice")
                Log.d("ProductProperties", "Image URL: $imageUrl")
                Log.d("ProductProperties", "Code: $code")

// Create the Products object
                val product = try {
                    Products(name, description, categoryId, sellingPrice, imageUrl, code)
                } catch (e: Exception) {
                    // Log the exception if one occurs during object creation
                    Log.e("ProductCreationError", "Error creating product: ${e.message}")
                    null // Return null if product creation fails
                }

// Check if the product is null after creation
                if (product != null) {
                    // Proceed with using the product
                    Log.d("ProductCreation", "Product created successfully: $product")
                    productList.add(product)
                } else {
                    // Handle the case where product creation failed
                    Log.w("ProductCreation", "Product creation failed: Product is null")
                }

            }
        }
        cursor?.close()
        db.close()
        return productList
    }
    fun getProductsByNameSearch(searchString: String, categoryId: Int): MutableList<Products> {
        val productList = mutableListOf<Products>()
        val db = readableDatabase
        var cursor: Cursor?

        if (categoryId == 0) {
            cursor = db.rawQuery("SELECT * FROM products WHERE name LIKE ?", arrayOf("%$searchString%"))
        } else {
            val selectionArgs = arrayOf(categoryId.toString(), "%$searchString%")
            cursor = db.rawQuery("SELECT * FROM products WHERE category_id = ? AND name LIKE ?", selectionArgs)
        }


        cursor?.use {
            val idColumnIndex = it.getColumnIndex("id")
            val nameColumnIndex = it.getColumnIndex("name")
            val descriptionColumnIndex = it.getColumnIndex("description")
            val isActiveColumnIndex = it.getColumnIndex("isActive")
            val categoryIdColumnIndex = it.getColumnIndex("category_id")
            val sellingPriceColumnIndex = it.getColumnIndex("selling_price")
            val imageUrlColumnIndex = it.getColumnIndex("image_url")
            val codeColumnIndex = it.getColumnIndex("code")

            while (it.moveToNext()) {
                val id = it.getInt(idColumnIndex)
                val name = it.getString(nameColumnIndex)
                val description = it.getString(descriptionColumnIndex)
                val isActive = it.getInt(isActiveColumnIndex) == 1 // Assuming 1 for true, 0 for false
                val categoryId = it.getInt(categoryIdColumnIndex)
                val sellingPrice = it.getDouble(sellingPriceColumnIndex)
                val imageUrl = it.getString(imageUrlColumnIndex)
                val code = it.getString(codeColumnIndex)

                val product = Products(name, description, categoryId, sellingPrice, imageUrl, code)
                productList.add(product)
            }
        }
        cursor?.close()
        db.close()
        return productList
    }

    fun getSuppliesByNameSearch(searchString: String): MutableList<Supplies> {
        val supplyList = mutableListOf<Supplies>()
        val db = readableDatabase
        var cursor: Cursor?
        cursor = db.rawQuery("SELECT * FROM supplies WHERE supplyName LIKE ?", arrayOf("%$searchString%"))

        cursor?.use {
            val supplyIDColumnIndex = it.getColumnIndex("supplyID")
            val supplyNameColumnIndex = it.getColumnIndex("supplyName")
            val supplyDescriptionColumnIndex = it.getColumnIndex("supplyDescription")
            val img_urlColumnIndex = it.getColumnIndex("img_url")
            val supplyQuantityColumnIndex = it.getColumnIndex("supplyQuantity")
            val supplyUnitColumnIndex = it.getColumnIndex("supplyUnit")
            val crticalLevelColumnIndex = it.getColumnIndex("crticalLevel")
            val isActiveColumnIndex = it.getColumnIndex("isActive")
            val creationDateColumnIndex = it.getColumnIndex("creationDate")

            while (it.moveToNext()) {
                val supplyID = it.getInt(supplyIDColumnIndex)
                val supplyName = it.getString(supplyNameColumnIndex)
                val supplyDescription = it.getString(supplyDescriptionColumnIndex)
                val img_url = it.getString(img_urlColumnIndex) // Assuming 1 for true, 0 for false
                val supplyQuantity = it.getInt(supplyQuantityColumnIndex)
                val supplyUnit = it.getString(supplyUnitColumnIndex)
                val crticalLevel = it.getInt(crticalLevelColumnIndex)
                val isActive = it.getInt(isActiveColumnIndex)
                val creationDate = it.getString(creationDateColumnIndex)

                val product = Supplies(supplyID, supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate)
                supplyList.add(product)
            }
        }
        cursor?.close()
        db.close()
        return supplyList
    }
    fun getProductByCode(itemCode: Any): Products? {
        val db = readableDatabase
        val cursor: Cursor? = db.rawQuery("SELECT * FROM products WHERE code = ?", arrayOf(itemCode.toString()))

        var product: Products? = null

        cursor?.use {
            val idColumnIndex = it.getColumnIndex("id")
            val nameColumnIndex = it.getColumnIndex("name")
            val descriptionColumnIndex = it.getColumnIndex("description")
            val isActiveColumnIndex = it.getColumnIndex("isActive")
            val categoryIdColumnIndex = it.getColumnIndex("category_id")
            val sellingPriceColumnIndex = it.getColumnIndex("selling_price")
            val imageUrlColumnIndex = it.getColumnIndex("image_url")
            val codeColumnIndex = it.getColumnIndex("code")

            if (it.moveToFirst()) {
                val id = it.getInt(idColumnIndex)
                val name = it.getString(nameColumnIndex)
                val description = it.getString(descriptionColumnIndex)
                val isActive = it.getInt(isActiveColumnIndex) == 1 // Assuming 1 for true, 0 for false
                val categoryId = it.getInt(categoryIdColumnIndex)
                val sellingPrice = it.getDouble(sellingPriceColumnIndex)
                val imageUri = it.getString(imageUrlColumnIndex)
                val code = it.getString(codeColumnIndex)

                product = Products(name, description, categoryId, sellingPrice, imageUri, code)

            }
        }

        cursor?.close()
        db.close()

        return product
    }
    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val filePath: String?
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            filePath = cursor.getString(columnIndex)
            cursor.close()
        } else {
            filePath = uri.path // Fallback to URI path if cursor is null
        }
        return filePath
    }
    fun addProduct(product: Products) {
        val sql = "INSERT INTO products (name, description, category_id, selling_price, image_url, code) VALUES (?, ?, ?, ?, ?, ?)"

        val database = writableDatabase
        database.execSQL(sql, arrayOf(product.name, product.description, product.categoryId, product.sellingPrice, product.imageUri, product.code))
        database.close()
    }

    fun updateSupplyQuantity(supplyId: Int, newQuantity: Int): Boolean {
        val sql = "UPDATE supplies SET supplyQuantity = ? WHERE supplyID = ?"
        val database = writableDatabase
        val statement = database.compileStatement(sql)

        statement.bindLong(1, newQuantity.toLong())
        statement.bindLong(2, supplyId.toLong())

        val rowsAffected = statement.executeUpdateDelete()
        database.close()

        return rowsAffected > 0
    }
    @SuppressLint("Range")
    fun generateDatabaseDescription(): String {
        val databaseDescription = StringBuilder()

        // Get readable database
        val db = readableDatabase

        // Get all table names
        val cursor: Cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)

        // Iterate through table names
        while (cursor.moveToNext()) {
            val tableName = cursor.getString(0)
            val tableDescription = StringBuilder()
            tableDescription.append("On table $tableName with fields ")

            // Get column names for the current table
            val columnsCursor: Cursor = db.rawQuery("PRAGMA table_info($tableName)", null)

            val fieldList = mutableListOf<String>()
            // Iterate through column names
            while (columnsCursor.moveToNext()) {
                val columnName = columnsCursor.getString(columnsCursor.getColumnIndex("name"))
                fieldList.add(columnName)
            }
            columnsCursor.close()

            tableDescription.append(fieldList.joinToString(", "))

            // Get data from the current table
            val dataCursor: Cursor = db.rawQuery("SELECT * FROM $tableName", null)

            // Iterate through rows
            var rowNumber = 1
            while (dataCursor.moveToNext()) {
                tableDescription.append(" $rowNumber${getOrdinalSuffix(rowNumber)} entry has ")
                for (i in 0 until dataCursor.columnCount) {
                    val fieldName = dataCursor.getColumnName(i)
                    val fieldValue = dataCursor.getString(i)
                    tableDescription.append("$fieldName value of $fieldValue")
                    if (i < dataCursor.columnCount - 1) {
                        tableDescription.append(", ")
                    }
                }
                tableDescription.append(". ")
                rowNumber++
            }
            dataCursor.close()

            databaseDescription.append(tableDescription.toString())
        }
        cursor.close()
        db.close()

        return databaseDescription.toString()
    }
    private fun getOrdinalSuffix(number: Int): String {
        return when (number % 100) {
            11, 12, 13 -> "th"
            else -> when (number % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }
    }
    fun isUserCodeExists(userCode: String): Boolean {
        val db = readableDatabase
        val selection = "usercode = ?"
        val selectionArgs = arrayOf(userCode)
        
        val cursor = db.query("users", null, selection, selectionArgs, null, null, null)
        val count = cursor.count
        cursor.close()
        
        return count > 0
    }
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "posheesh.db"

    }

    // Analytics Query Methods
    fun getTotalSales(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM(totalAmount) FROM sales WHERE isActive = 1", null)
        var totalSales = 0.0
        
        cursor.use {
            if (it.moveToFirst()) {
                totalSales = it.getDouble(0)
            }
        }
        
        db.close()
        return totalSales
    }

    fun getTotalOrders(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM sales WHERE isActive = 1", null)
        var totalOrders = 0
        
        cursor.use {
            if (it.moveToFirst()) {
                totalOrders = it.getInt(0)
            }
        }
        
        db.close()
        return totalOrders
    }

    fun getAverageOrderValue(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT AVG(totalAmount) FROM sales WHERE isActive = 1", null)
        var averageOrderValue = 0.0
        
        cursor.use {
            if (it.moveToFirst()) {
                averageOrderValue = it.getDouble(0)
            }
        }
        
        db.close()
        return averageOrderValue
    }

    fun getSalesByDateRange(startDate: String, endDate: String): List<Map<String, Any>> {
        val db = readableDatabase
        val sales = mutableListOf<Map<String, Any>>()
        
        val query = """
            SELECT 
                orderDate,
                SUM(totalAmount) as dailySales,
                COUNT(*) as dailyOrders
            FROM sales 
            WHERE isActive = 1 
            AND orderDate BETWEEN ? AND ?
            GROUP BY orderDate
            ORDER BY orderDate
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        
        cursor.use {
            while (it.moveToNext()) {
                val sale = mapOf(
                    "date" to it.getString(0),
                    "sales" to it.getDouble(1),
                    "orders" to it.getInt(2)
                )
                sales.add(sale)
            }
        }
        
        db.close()
        return sales
    }

    fun getTopSellingProducts(limit: Int = 5): List<Map<String, Any>> {
        val db = readableDatabase
        val products = mutableListOf<Map<String, Any>>()
        
        val query = """
            SELECT 
                oi.productCode,
                oi.productName,
                SUM(oi.quantity) as totalQuantity,
                SUM(oi.totalPrice) as totalRevenue
            FROM order_items oi
            JOIN sales s ON oi.salesID = s.salesID
            WHERE s.isActive = 1
            GROUP BY oi.productCode, oi.productName
            ORDER BY totalRevenue DESC
            LIMIT ?
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(limit.toString()))
        
        cursor.use {
            while (it.moveToNext()) {
                val product = mapOf(
                    "productCode" to it.getString(0),
                    "productName" to it.getString(1),
                    "totalQuantity" to it.getInt(2),
                    "totalRevenue" to it.getDouble(3)
                )
                products.add(product)
            }
        }
        
        db.close()
        return products
    }

    fun getSalesByCategory(startDate: String, endDate: String): List<Map<String, Any>> {
        val db = readableDatabase
        val categories = mutableListOf<Map<String, Any>>()
        
        val query = """
            SELECT 
                c.name as categoryName,
                SUM(oi.totalPrice) as categorySales,
                COUNT(DISTINCT s.salesID) as orderCount
            FROM order_items oi
            JOIN sales s ON oi.salesID = s.salesID
            JOIN products p ON oi.productCode = p.code
            JOIN categories c ON p.category_id = c.id
            WHERE s.isActive = 1 
            AND s.orderDate BETWEEN ? AND ?
            GROUP BY c.id, c.name
            ORDER BY categorySales DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        
        cursor.use {
            while (it.moveToNext()) {
                val category = mapOf(
                    "categoryName" to it.getString(0),
                    "categorySales" to it.getDouble(1),
                    "orderCount" to it.getInt(2)
                )
                categories.add(category)
            }
        }
        
        db.close()
        return categories
    }

    fun getHourlySales(startDate: String, endDate: String): List<Map<String, Any>> {
        val db = readableDatabase
        val hourlySales = mutableListOf<Map<String, Any>>()
        
        val query = """
            SELECT 
                CAST(SUBSTR(orderTime, 1, 2) AS INTEGER) as hour,
                SUM(totalAmount) as hourlySales,
                COUNT(*) as hourlyOrders
            FROM sales 
            WHERE isActive = 1 
            AND orderDate BETWEEN ? AND ?
            GROUP BY CAST(SUBSTR(orderTime, 1, 2) AS INTEGER)
            ORDER BY hour
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        
        cursor.use {
            while (it.moveToNext()) {
                val hour = mapOf(
                    "hour" to it.getInt(0),
                    "sales" to it.getDouble(1),
                    "orders" to it.getInt(2)
                )
                hourlySales.add(hour)
            }
        }
        
        db.close()
        return hourlySales
    }

    fun getWeeklyTrends(weeks: Int = 4): List<Map<String, Any>> {
        val db = readableDatabase
        val weeklyTrends = mutableListOf<Map<String, Any>>()
        
        val query = """
            SELECT 
                strftime('%Y-%W', orderDate) as weekKey,
                MIN(orderDate) as weekStart,
                MAX(orderDate) as weekEnd,
                SUM(totalAmount) as weeklySales,
                COUNT(*) as weeklyOrders
            FROM sales 
            WHERE isActive = 1 
            AND orderDate >= date('now', '-${weeks * 7} days')
            GROUP BY strftime('%Y-%W', orderDate)
            ORDER BY weekStart
        """.trimIndent()
        
        val cursor = db.rawQuery(query, null)
        
        cursor.use {
            while (it.moveToNext()) {
                val week = mapOf(
                    "weekKey" to it.getString(0),
                    "weekStart" to it.getString(1),
                    "weekEnd" to it.getString(2),
                    "weeklySales" to it.getDouble(3),
                    "weeklyOrders" to it.getInt(4)
                )
                weeklyTrends.add(week)
            }
        }
        
        db.close()
        return weeklyTrends
    }

    fun getPaymentMethodDistribution(startDate: String, endDate: String): List<Map<String, Any>> {
        val db = readableDatabase
        val paymentMethods = mutableListOf<Map<String, Any>>()
        
        val query = """
            SELECT 
                paymentMethod,
                COUNT(*) as count,
                SUM(totalAmount) as totalAmount
            FROM sales 
            WHERE isActive = 1 
            AND orderDate BETWEEN ? AND ?
            GROUP BY paymentMethod
            ORDER BY count DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        
        cursor.use {
            while (it.moveToNext()) {
                val payment = mapOf(
                    "paymentMethod" to it.getString(0),
                    "count" to it.getInt(1),
                    "totalAmount" to it.getDouble(2)
                )
                paymentMethods.add(payment)
            }
        }
        
        db.close()
        return paymentMethods
    }

    fun getLowStockSupplies(): List<Map<String, Any>> {
        val db = readableDatabase
        val lowStockSupplies = mutableListOf<Map<String, Any>>()
        
        val query = """
            SELECT 
                supplyID,
                supplyName,
                supplyQuantity,
                crticalLevel,
                supplyUnit
            FROM supplies 
            WHERE isActive = 1 
            AND supplyQuantity <= crticalLevel
            ORDER BY (crticalLevel - supplyQuantity) DESC
        """.trimIndent()
        
        val cursor = db.rawQuery(query, null)
        
        cursor.use {
            while (it.moveToNext()) {
                val supply = mapOf(
                    "supplyID" to it.getInt(0),
                    "supplyName" to it.getString(1),
                    "supplyQuantity" to it.getInt(2),
                    "criticalLevel" to it.getInt(3),
                    "supplyUnit" to it.getString(4)
                )
                lowStockSupplies.add(supply)
            }
        }
        
        db.close()
        return lowStockSupplies
    }

    fun getInventoryValue(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM(selling_price * 10) FROM products WHERE isActive = 1", null)
        var inventoryValue = 0.0
        
        cursor.use {
            if (it.moveToFirst()) {
                inventoryValue = it.getDouble(0)
            }
        }
        
        db.close()
        return inventoryValue
    }
}
