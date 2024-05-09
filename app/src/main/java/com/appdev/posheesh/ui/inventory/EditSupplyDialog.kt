import android.app.AlertDialog
import android.app.Dialog
import com.appdev.posheesh.R
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.appdev.posheesh.Classes.Supplies
import com.appdev.posheesh.DatabaseHandler
import com.appdev.posheesh.ui.inventory.InventoryFragment

class EditSupplyDialog(private val supply: Supplies, private val listener: InventoryFragment) : DialogFragment() {

    private lateinit var dbHelper: DatabaseHandler

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_supply, null)
        dbHelper = DatabaseHandler(requireContext())


        val editTextQuantity = view.findViewById<EditText>(R.id.editTextQuantity)
        editTextQuantity.setText(supply.supplyQuantity.toString())

        builder.setView(view)
            .setTitle("Edit Supply Quantity")
            .setPositiveButton("Save") { dialog, _ ->
                val newQuantity = editTextQuantity.text.toString().toIntOrNull()
                if (newQuantity != null) {
                    // Update the supply quantity in the database
                    val updated = dbHelper.updateSupplyQuantity(supply.supplyID, newQuantity)
                    if (updated) {
                        // Notify the listener or update UI as needed
                        listener.onSupplyQuantityUpdated(supply.supplyID, newQuantity)
                    } else {
                        // Handle database update failure
                    }
                } else {
                    // Handle invalid input
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }
}
