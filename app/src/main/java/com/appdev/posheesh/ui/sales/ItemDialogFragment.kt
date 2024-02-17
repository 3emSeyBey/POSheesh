import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.appdev.posheesh.Classes.Products
import com.appdev.posheesh.R
import com.appdev.posheesh.ui.sales.SalesFragment

class ItemDialogFragment : DialogFragment() {

    companion object {
        private const val ITEM_NAME_KEY = "itemName"
        private const val ITEM_DESCRIPTION_KEY = "itemDescription"
        private const val ITEM_SELLING_PRICE_KEY = "itemSellingPrice"
        private const val ITEM_ID_KEY = "0"

        fun newInstance(item: Products): ItemDialogFragment {
            val fragment = ItemDialogFragment()
            val args = Bundle()
            args.putString(ITEM_NAME_KEY, item.name)
            args.putString(ITEM_DESCRIPTION_KEY, item.description)
            args.putDouble(ITEM_SELLING_PRICE_KEY, item.sellingPrice)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_item_options, null)

        // Find views
        val itemNameTextView: TextView = view.findViewById(R.id.itemNameTextView)
        val itemDescriptionTextView: TextView = view.findViewById(R.id.itemDescriptionTextView)
        val quantityEditText: EditText = view.findViewById(R.id.quantityEditText)
        val totalPriceTextView: TextView = view.findViewById(R.id.totalPriceTextView)
        val plusButton: ImageView = view.findViewById(R.id.plusButton)
        val minusButton: ImageView = view.findViewById(R.id.minusButton)
        val addToCartButton: Button = view.findViewById(R.id.addToCartButton)
        val cancelButton: Button = view.findViewById(R.id.cancelButton)

        // Retrieve item details from arguments
        val itemName = arguments?.getString(ITEM_NAME_KEY)
        val itemDescription = arguments?.getString(ITEM_DESCRIPTION_KEY)
        val itemSellingPrice = arguments?.getDouble(ITEM_SELLING_PRICE_KEY)

        // Set the item name and description
        itemNameTextView.text = itemName
        itemDescriptionTextView.text = itemDescription

        // Set initial total price
        totalPriceTextView.text = getString(R.string.total_price, itemSellingPrice)

        // Set click listeners for plus and minus buttons
        var quantity = 1 // Initial quantity
        plusButton.setOnClickListener {
            quantity++
            quantityEditText.setText(quantity.toString())
        }
        minusButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityEditText.setText(quantity.toString())
            }
        }

        // Update total price when quantity changes
        quantityEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used
            }

            override fun afterTextChanged(s: Editable?) {
                val quantityValue = s.toString().toIntOrNull() ?: 0
                val totalPrice = quantityValue * (itemSellingPrice ?: 0.0)
                totalPriceTextView.text = getString(R.string.total_price, totalPrice)
            }
        })

        // Set click listener for Add to Cart button
        addToCartButton.setOnClickListener {
            // Add your logic here to handle adding the item to the cart
            // You can access the quantity using quantityEditText.text.toString().toInt()
            // You can access other item details using itemName, itemDescription, and itemSellingPrice
            val itemId = arguments?.getInt(ITEM_ID_KEY)
            if (itemId != null) {
               // SalesFragment.addToCart(itemId, quantityEditText.text.toString().toInt())
                // Show a modal popup indicating the item has been added to the cart
                val alertDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Item Added to Cart")
                    .setMessage("The item has been successfully added to your cart.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss() // Dismiss the dialog when OK is clicked
                    }
                    .create()
                alertDialog.show()
            }
        dismiss()
        }

        // Set click listener for Cancel button
        cancelButton.setOnClickListener {
            // Add your logic here to handle canceling the operation
            // Dismiss the dialog when the cancel button is clicked
            dismiss()
        }

        // Set the view for the dialog
        builder.setView(view)

        // Add other configurations like title, buttons, etc.

        return builder.create()
    }
}
