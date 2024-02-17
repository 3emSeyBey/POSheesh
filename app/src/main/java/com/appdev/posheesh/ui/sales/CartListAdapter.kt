package com.appdev.posheesh.ui.sales

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.appdev.posheesh.Classes.Products
import com.appdev.posheesh.DatabaseHandler
import com.appdev.posheesh.R
import java.io.File

class CartListAdapter(
    context: Context,
    private val cartItems: MutableList<Map<String, Any>>
) : ArrayAdapter<Map<String, Any>>(context, R.layout.list_item_cart, cartItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(R.layout.list_item_cart, parent, false)
        }

        // Get the current cart item
        val currentCartItem = cartItems[position]

        // Set data to views
        val itemNameTextView = itemView!!.findViewById<TextView>(R.id.itemNameTextView)
        val quantityTextView = itemView.findViewById<TextView>(R.id.quantityTextView)
        val itemImageView = itemView.findViewById<ImageView>(R.id.itemImageView)


        // Set the item id and quantity
        itemNameTextView.text = getProductByCode(currentCartItem.get("code") as String).name
        quantityTextView.text = currentCartItem["quantity"].toString()
        currentCartItem["code"]?.let { getProductByCode(it as String).imageUri }
            ?.let { itemImageView.setImageURI(Uri.fromFile(File(it))) }

        return itemView
    }

    fun getProductByCode(itemCode: String): Products {
        // Initialize DatabaseHandler
        val dbHelper = DatabaseHandler(context)

        // Query the database to get the product by ID
        return dbHelper.getProductByCode(itemCode)!!
    }
}
