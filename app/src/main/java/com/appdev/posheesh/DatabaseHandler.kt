package com.appdev.posheesh

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.appdev.posheesh.Classes.Products

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
                    "image_url INTEGER," +
                    "code TEXT," +
                    "FOREIGN KEY (category_id) REFERENCES categories(id)" +
                    ")"
        )

        // Populate the products table with dummy entries
        db.execSQL(
            "INSERT INTO products " +
                    "(name, description, category_id, quantity, selling_price, buying_price, image_url, code) " +
                    "VALUES " +
                    "('Breakfast Steak', 'Our juicy burger patty charbroiled to perfection topped with our signature brown gravy. Served with sunny side up egg, french fries and garlic rice. Comes with your choice of of hot drinks (Hot Chocolate or Hot Coffee ), 12 oz. Iced Tea or 12 oz. Soft drink', 1, 10, 99.0, 60.0, "+R.drawable.breakfast_steak+", 'P001'), " +
                    "('Chorizo', 'Chorizo with Sunny Side Up Egg and Garlic Rice', 1, 20, 60.0, 40.0,"+R.drawable.new_chorizo+", 'P002'), " +
                    "('Longaniza', 'Longaniza with Sunny Side Up Egg and Garlic Rice', 1, 15, 70.0, 45.0,"+R.drawable.new_longaniza+", 'P003'), " +
                    "('Hotdog', 'Hotdog with Sunny Side Up Egg and Garlic Rice', 1, 30, 80.0, 55.0, "+R.drawable.new_hotdog+", 'P004'), " +
                    "('Tocino', 'Tocino with Sunny Side Up Egg and Garlic Rice', 1, 25, 90.0, 60.0, "+R.drawable.new_tocino+", 'P005')" +
                    "('PM 2 – BRUTE BURGER WITH REGULAR FRIES + DRINK', 'Brute Burger with regular french fries, 12 oz Softdrink or Iced tea', 2, 30, 80.0, 55.0, "+R.drawable.pm2_brute_burger_with_regular_french_fries+", 'P006'), " +
                    "('PM 4 – CHICKEN BURGER + DRINK', 'Chicken Burger', 2, 30, 80.0, 55.0, "+R.drawable.pm4_chicken_burger_1+", 'P007'), " +
                    "('PM 12 – HALF SPAGHETTI AND 1PC. CHICKEN BRUTUS + DRINK', 2, 30, 129.0, 55.0, "+R.drawable.pm12_1half_spaghetti_and_1pc__chicken_brutus_1+", 'P008'), " +
                    "('HOT CHOCO', 3, 30, 49.0, 20.0, "+R.drawable.hot_choco_1+", 'P009')"
        )
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if necessary
    }

    fun getImageUri(context: Context, resourceId: Int): Uri {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.resources.getResourcePackageName(resourceId) +
                "/" + context.resources.getResourceTypeName(resourceId) +
                "/" + context.resources.getResourceEntryName(resourceId))
    }
    fun getAllProducts(): MutableList<Products> {
        val productList = mutableListOf<Products>()
        val db = readableDatabase
        val cursor: Cursor? = db.rawQuery("SELECT * FROM products", null)
        cursor?.use {
            val idColumnIndex = it.getColumnIndex("id")
            val nameColumnIndex = it.getColumnIndex("name")
            val descriptionColumnIndex = it.getColumnIndex("description")
            val isActiveColumnIndex = it.getColumnIndex("isActive")
            val categoryIdColumnIndex = it.getColumnIndex("category_id")
            val quantityColumnIndex = it.getColumnIndex("quantity")
            val sellingPriceColumnIndex = it.getColumnIndex("selling_price")
            val buyingPriceColumnIndex = it.getColumnIndex("buying_price")
            val imageUrlColumnIndex = it.getColumnIndex("image_url")
            val codeColumnIndex = it.getColumnIndex("code")

            while (it.moveToNext()) {
                val id = it.getInt(idColumnIndex)
                val name = it.getString(nameColumnIndex)
                val description = it.getString(descriptionColumnIndex)
                val isActive = it.getInt(isActiveColumnIndex) == 1 // Assuming 1 for true, 0 for false
                val categoryId = it.getInt(categoryIdColumnIndex)
                val quantity = it.getInt(quantityColumnIndex)
                val sellingPrice = it.getDouble(sellingPriceColumnIndex)
                val buyingPrice = it.getDouble(buyingPriceColumnIndex)
                val imageUrl = it.getInt(imageUrlColumnIndex)
                val code = it.getString(codeColumnIndex)

                val product = Products(id, name, description, isActive, categoryId, quantity, sellingPrice, buyingPrice, imageUrl, code)
                productList.add(product)
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
