import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.posheesh.Classes.Products
import com.appdev.posheesh.R
import com.squareup.picasso.Picasso

class ItemListAdapter(private val context: Context, private val items: Array<Products>, private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {

    interface ItemClickListener {
        fun onItemClick(item: Products)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Set item data to views
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }
        private val imageItemImageView: ImageView = itemView.findViewById(R.id.imageItem)
        private val itemNameTextView: TextView = itemView.findViewById(R.id.textItemName)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.textPrice)

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = items[position]
                itemClickListener.onItemClick(item)
            }
        }
        fun bind(item: Products) {
            // Bind item data to views
            Picasso.get().load(item.imageUrl).into(imageItemImageView)
            itemNameTextView.text = item.name
            itemPriceTextView.text = "Price: ${item.sellingPrice}"
            // You can set other item details here if needed
        }
    }
}
