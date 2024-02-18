import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.appdev.posheesh.R
import com.appdev.posheesh.ui.sales.CartListAdapter

class CartFragment(private val items: MutableList<Map<String, Any>>) : DialogFragment(), CartListAdapter.TotalPriceListener {
    private lateinit var totalPriceTextView: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_cart, null)

        // Find the ListView
        val listViewCartItems: ListView = view.findViewById(R.id.listViewCartItems)

        // Create and set the custom adapter with items from SalesFragment.cart
        val adapter = CartListAdapter(requireContext(), items, this)
        listViewCartItems.adapter = adapter


        // Set the view for the dialog
        builder.setView(view)

        // Add other configurations like title, buttons, etc.
        totalPriceTextView = view.findViewById(R.id.textViewTotalPrice)

        return builder.create()
    }

    override fun onTotalPriceChanged(totalPrice: Double) {
        totalPriceTextView.text = "Total Price: $${String.format("%.2f", totalPrice)}"
    }
}
