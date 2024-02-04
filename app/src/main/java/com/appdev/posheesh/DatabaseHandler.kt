package com.appdev.posheesh

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.BufferedReader
import java.io.InputStreamReader

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
        db.execSQL("INSERT INTO categories (name) VALUES ('Category 1'), ('Category 2'), ('Category 3')")

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

        // Populate the products table with dummy entries
        db.execSQL(
            "INSERT INTO products " +
                    "(name, description, category_id, quantity, selling_price, buying_price, image_url, code) " +
                    "VALUES " +
                    "('Product 1', 'Description 1', 1, 10, 50.0, 30.0, 'image1.jpg', 'P001'), " +
                    "('Product 2', 'Description 2', 2, 20, 60.0, 40.0, 'image2.jpg', 'P002'), " +
                    "('Product 3', 'Description 3', 1, 15, 70.0, 45.0, 'image3.jpg', 'P003'), " +
                    "('Product 4', 'Description 4', 3, 30, 80.0, 55.0, 'image4.jpg', 'P004'), " +
                    "('Product 5', 'Description 5', 2, 25, 90.0, 60.0, 'image5.jpg', 'P005')"
        )
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if necessary
    }

    fun getAllProductNames(): MutableList<String> {
        val productList = mutableListOf<String>()
        val db = readableDatabase
        val cursor: Cursor? = db.rawQuery("SELECT * FROM products", null)
        cursor?.use {
            val nameColumnIndex = it.getColumnIndex("name")
            if (nameColumnIndex != -1) {
                while (it.moveToNext()) {
                    val productName = it.getString(nameColumnIndex)
                    productList.add(productName)
                }
            }
        }
        cursor?.close()
        db.close()
        return productList
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "posheesh.db"

    }
}
