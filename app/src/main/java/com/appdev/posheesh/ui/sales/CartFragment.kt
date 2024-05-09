import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.appdev.posheesh.R
import com.appdev.posheesh.ui.sales.CartListAdapter
import java.io.FileNotFoundException
import java.io.IOException

class CartFragment(private val items: MutableList<Map<String, Any>>) : DialogFragment(), CartListAdapter.TotalPriceListener {
    private lateinit var totalPriceTextView: TextView
    private lateinit var contentDesc: String
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

        val btnPayWithCash: Button = view.findViewById(R.id.btn_paywithcash)
        btnPayWithCash.setOnClickListener {
            showPaymentDialogCash(totalPriceTextView)
        }

        // Set click listener for the QR button
        val btnPayWithQR: Button = view.findViewById(R.id.btn_paywithqr)
        btnPayWithQR.setOnClickListener {
            showPaymentDialogQR()
        }

        return builder.create()
    }

    private fun showPaymentDialogCash(totalPriceTextView: TextView) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_paywithcash, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select Payment Method")

        val dialog = builder.create()

        // Find buttons in the dialog layout
        val btnPayWithCash = dialogView.findViewById<Button>(R.id.btnPayWithCash)
        dialogView.findViewById<TextView>(R.id.totalPrice).setText(totalPriceTextView.text)

        // Set click listeners for the buttons
        btnPayWithCash.setOnClickListener {
            // Handle payment with Cash
            dialog.dismiss()
            // Add your logic here
        }
        dialog.show()
    }

    private fun generateReceipt(){

    }
    private fun showPaymentDialogQR() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_paywithqr, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select Payment Method")

        val dialog = builder.create()

        // Find buttons in the dialog layout
        val btnPayWithGCash = dialogView.findViewById<Button>(R.id.btnPayWithGCash)
        val btnPayWithMaya = dialogView.findViewById<Button>(R.id.btnPayWithMaya)
        val btnSet = dialogView.findViewById<ImageButton>(R.id.btn_set)

// Find the ImageView and buttons
        val imageViewQRCode: ImageView = dialogView.findViewById(R.id.imageViewQRCode)

// Calculate the maxHeight for the ImageView



        // Set click listeners for the buttons
        btnPayWithGCash.setOnClickListener {
            contentDesc = "GCash"
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val buttonHeight = btnPayWithMaya.measuredHeight + btnPayWithGCash.measuredHeight + (8 * 2) // total button heights + margins
            val maxHeight = screenHeight - buttonHeight

// Set the maxHeight for the ImageView
            imageViewQRCode.maxHeight = maxHeight
            imageViewQRCode.setImageBitmap(getbitmap("gcash_qr_image.jpg"))
        }

        btnPayWithMaya.setOnClickListener {
            contentDesc = "Maya"
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val buttonHeight = btnPayWithMaya.measuredHeight + btnPayWithGCash.measuredHeight + (8 * 2) // total button heights + margins
            val maxHeight = screenHeight - buttonHeight

// Set the maxHeight for the ImageView
            imageViewQRCode.maxHeight = maxHeight
            imageViewQRCode.setImageBitmap(getbitmap("maya_qr_image.jpg"))
        }
        btnSet.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            //contentDesc = imageViewQRCode.contentDescription.toString()
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        dialog.show()
    }

    override fun onTotalPriceChanged(totalPrice: Double) {
        totalPriceTextView.text = "Total Price: $${String.format("%.2f", totalPrice)}"
    }

    private val PICK_IMAGE_REQUEST = 1

    // Override onActivityResult to handle the result of the image picker
    private fun getbitmap(fileName: String): Bitmap? {
        try {
            val fileInputStream = requireContext().openFileInput(fileName)
            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            fileInputStream.close()

            // Display the image using an ImageView or any other appropriate view
            return bitmap
        } catch (e: FileNotFoundException) {
            // Handle the case where the file is not found
            e.printStackTrace()
            Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
            return null
        } catch (e: IOException) {
            // Handle other I/O exceptions
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to open image", Toast.LENGTH_SHORT).show()
            return null
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // Get the URI of the selected image
            val imageUri: Uri? = data?.data

            // Check the contentDescription of imageViewQRCode
            when (contentDesc) {
                "GCash" -> saveImageToInternalStorage(imageUri, "gcash_qr_image.jpg")
                "Maya" -> saveImageToInternalStorage(imageUri, "maya_qr_image.jpg")
            }
        }
    }

    // Function to save the image to internal storage
    private fun saveImageToInternalStorage(imageUri: Uri?, fileName: String) {
        imageUri?.let {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val outputStream = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)

                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                // Notify the user that the image has been saved
                Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                // Notify the user if there was an error saving the image
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
