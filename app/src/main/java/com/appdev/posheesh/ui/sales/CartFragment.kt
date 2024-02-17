import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import com.appdev.posheesh.R
import com.appdev.posheesh.ui.sales.CartListAdapter

class CartFragment(private val items: MutableList<Map<String, Any>>) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_cart, null)

        // Find the ListView
        val listViewCartItems: ListView = view.findViewById(R.id.listViewCartItems)

        // Create and set the custom adapter with items from SalesFragment.cart
        val adapter = CartListAdapter(requireContext(), items)
        listViewCartItems.adapter = adapter

        // Set the view for the dialog
        builder.setView(view)

        // Add other configurations like title, buttons, etc.

        return builder.create()
    }
}
