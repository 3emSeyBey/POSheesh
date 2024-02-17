package com.appdev.posheesh

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
                    "FOREIGN KEY (category_id) REFERENCES categories(id)" +
                    ")"
        )

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
    }

    fun saveImageToInternalStorage(resourceId: Int): Uri? {
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
        var cursor: Cursor? = null

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
    fun getFilePathFromUri(context: Context, uri: Uri): String? {
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
    private fun getUriFromResourceId(resourceId: Int): Uri {
        val uri: Uri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.resources.getResourcePackageName(resourceId)
                + '/' + context.resources.getResourceTypeName(resourceId)
                + '/' + context.resources.getResourceEntryName(resourceId))
        return uri
    }
    fun addProduct(product: Products) {
        val sql = "INSERT INTO products (name, description, category_id, selling_price, image_url, code) VALUES (?, ?, ?, ?, ?, ?)"

        val database = writableDatabase
        database.execSQL(sql, arrayOf(product.name, product.description, product.categoryId, product.sellingPrice, product.imageUri, product.code))
        database.close()
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "posheesh.db"

    }
}
