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
    private val cartItems: MutableList<Map<String, Any>>,
    private val totalPriceListener: TotalPriceListener
) : ArrayAdapter<Map<String, Any>>(context, R.layout.list_item_cart, cartItems) {

    interface TotalPriceListener {
        fun onTotalPriceChanged(totalPrice: Double)
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(R.layout.list_item_cart, parent, false)
        }

        // Get the current cart item
        val currentCartItem = cartItems[position].toMutableMap()

        // Set data to views
        val itemNameTextView = itemView!!.findViewById<TextView>(R.id.itemNameTextView)
        val quantityTextView = itemView.findViewById<TextView>(R.id.quantityTextView)
        val itemImageView = itemView.findViewById<ImageView>(R.id.itemImageView)


        // Set the item id and quantity
        itemNameTextView.text = getProductByCode(currentCartItem.get("code") as String).name
        quantityTextView.text = currentCartItem["quantity"].toString()
        currentCartItem["code"]?.let { getProductByCode(it as String).imageUri }
            ?.let { itemImageView.setImageURI(Uri.fromFile(File(it))) }

        // Calculate total price and notify the listener
        val totalPrice = calculateTotalPrice()
        totalPriceListener.onTotalPriceChanged(totalPrice)

        val minusButton = itemView.findViewById<ImageView>(R.id.minusButton)
        val plusButton = itemView.findViewById<ImageView>(R.id.plusButton)

        // Click listener for minus button
        minusButton.setOnClickListener {
            SalesFragment.removeToCart(currentCartItem["code"] as String)
            notifyDataSetChanged() // Update the ListView
            notifyTotalPriceChanged() // Notify the total price change
        }

        // Click listener for plus button
        plusButton.setOnClickListener {
            SalesFragment.addToCart(currentCartItem["code"] as String, 1)
            notifyDataSetChanged() // Update the ListView
            notifyTotalPriceChanged() // Notify the total price change
        }

        return itemView
    }
    private fun notifyTotalPriceChanged() {
        val totalPrice = calculateTotalPrice()
        totalPriceListener.onTotalPriceChanged(totalPrice)
    }

    private fun calculateTotalPrice(): Double {
        var totalPrice = 0.0
        for (cartItem in cartItems) {
            val product = getProductByCode(cartItem["code"] as String)
            totalPrice += product.sellingPrice * cartItem["quantity"] as Int
        }
        return totalPrice
    }
    fun getProductByCode(itemCode: String): Products {
        // Initialize DatabaseHandler
        val dbHelper = DatabaseHandler(context)

        // Query the database to get the product by ID
        return dbHelper.getProductByCode(itemCode)!!
    }
}
