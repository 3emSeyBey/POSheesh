import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appdev.posheesh.Classes.Supplies
import com.appdev.posheesh.R
import com.appdev.posheesh.ui.inventory.InventoryFragment
import android.app.AlertDialog
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class SupplyListAdapter(private val context: Context, private val items: Array<Supplies>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<SupplyListAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onEditClick(item: Supplies)
    }

    fun updateSupplyQuantity(supplyId: Int, newQuantity: Int) {
        // Find the supply item in the list and update its quantity
        val index = items.indexOfFirst { it.supplyID == supplyId }
        if (index != -1) {
            items[index].supplyQuantity = newQuantity
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.supply_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Set item data to views
        holder.bind(currentItem)
        holder.itemView.setOnClickListener {
            listener.onEditClick(currentItem)
        }
    }
    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.textItemName)
        private val itemDescriptionTextView: TextView = itemView.findViewById(R.id.textDescription)
        private val textRemainingPieces: TextView = itemView.findViewById(R.id.textRemainingPieces)

        fun bind(item: Supplies) {
            itemNameTextView.text = item.supplyName
            itemDescriptionTextView.text = item.supplyDescription
            textRemainingPieces.text = "Remaining: " + item.supplyQuantity.toString() + " "+item.supplyUnit
            // You can set other item details here if needed
        }
    }
}
