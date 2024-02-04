import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.appdev.posheesh.Classes.Products
import com.appdev.posheesh.R
import com.squareup.picasso.Picasso


class ItemListAdapter(context: Context, private val items: Array<Products>) :
    ArrayAdapter<Products>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_layout, parent, false)
        }

        // Get the item at the specified position
        val currentItem = items[position]

        // Set the item in the item_layout
        //Image
        val imageItemImageView: ImageView? = itemView?.findViewById(R.id.imageItem)
        imageItemImageView?.setImageResource(currentItem.imageUrl)

        //Name
        val itemNameTextView: TextView? = itemView?.findViewById(R.id.textItemName)
        itemNameTextView?.text = currentItem.name

        //Price
        val itemPriceTextView: TextView? = itemView?.findViewById(R.id.textPrice)
        itemPriceTextView?.text = "Price:" +currentItem.sellingPrice.toString()

        //Stock Remaining
        val itemStockRemainingTextView: TextView? = itemView?.findViewById(R.id.textStockRemaining)
        itemStockRemainingTextView?.text ="Stock Remaining: " + currentItem.quantity.toString()

        // You can set other item details here if needed

        return itemView!!
    }
}
