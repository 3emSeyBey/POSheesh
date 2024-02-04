import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.appdev.posheesh.R


class ItemListAdapter(context: Context, private val items: Array<String>) :
    ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_layout, parent, false)
        }

        // Get the item at the specified position
        val currentItem = items[position]

        // Set the item name in the TextView
        val itemNameTextView: TextView? = itemView?.findViewById(R.id.textItemName)
        itemNameTextView?.text = currentItem

        // You can set other item details here if needed

        return itemView!!
    }
}
