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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHandler(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
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

        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('plastic cups', 'Disposable plastic cups for serving beverages', 'https://example.com/plastic_cups_image.jpg', 500, 'per piece', 50, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('straw', 'Disposable straws for drinking', 'https://example.com/straw_image.jpg', 500, 'per piece', 50, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('chocolate syrup', 'Chocolate-flavored syrup for flavoring beverages', 'https://example.com/chocolate_syrup_image.jpg', 5000, 'ml', 500, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('strawberry jam', 'Strawberry-flavored jam for flavoring beverages', 'https://example.com/strawberry_jam_image.jpg', 5000, 'ml', 500, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('matcha syrup', 'Matcha-flavored syrup for flavoring beverages', 'https://example.com/matcha_syrup_image.jpg', 5000, 'ml', 500, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('ice', 'Ice cubes for chilling beverages', 'https://example.com/ice_image.jpg', 10000, 'ml', 1000, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('coffee', 'Coffee concentrate for making coffee-based beverages', 'https://example.com/coffee_image.jpg', 5000, 'ml', 500, 1, CURRENT_TIMESTAMP);")
        db.execSQL("INSERT INTO supplies (supplyName, supplyDescription, img_url, supplyQuantity, supplyUnit, crticalLevel, isActive, creationDate) VALUES ('milk', 'Milk for making milk-based beverages', 'https://example.com/milk_image.jpg', 10000, 'ml', 1000, 1, CURRENT_TIMESTAMP);")
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
        return (100000..999999).random().toString()
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
        db.close()
        return count > 0
    }
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "posheesh.db"

    }
}
