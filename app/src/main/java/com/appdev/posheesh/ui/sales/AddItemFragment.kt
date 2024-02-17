package com.appdev.posheesh.ui.sales

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.appdev.posheesh.Classes.Products
import com.appdev.posheesh.DatabaseHandler
import com.appdev.posheesh.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AddItemFragment : DialogFragment() {

    interface OnItemAddedListener {
        fun onItemAdded()
    }
    private var listener: OnItemAddedListener? = null
    private lateinit var dbHelper: DatabaseHandler
    private var selectedImageUri: String? = null // URI to store the selected image
    private var capturedPhotoUri: Uri? = null // URI to store the captured image
    private var currentPhotoPath: String? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view: View = inflater.inflate(R.layout.dialog_additem, null)

        // Initialize database helper
        dbHelper = DatabaseHandler(requireContext())

        val itemNameEditText: EditText = view.findViewById(R.id.editTextItemName)
        val descriptionEditText: EditText = view.findViewById(R.id.editTextDescription)
        val sellingPriceEditText: EditText = view.findViewById(R.id.editTextSellingPrice)
        val addItemButton: Button = view.findViewById(R.id.buttonAddItem)
        val barcodeEditText: EditText = view.findViewById(R.id.editTextBarcode)
        val uploadImageButton: Button = view.findViewById(R.id.buttonFilePicker)
        val captureImageButton: Button = view.findViewById(R.id.buttonCameraPicker)

        // Add item to database when the "Add Item" button is clicked
        addItemButton.setOnClickListener {
            val itemName = itemNameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val sellingPrice = sellingPriceEditText.text.toString().toDoubleOrNull()
            val barcode = barcodeEditText.text.toString().trim()

            if (itemName.isNotEmpty() && sellingPrice != null && barcode.isNotEmpty()) {
                val product = selectedImageUri?.let { it1 ->
                    Products(
                        name = itemName,
                        description = description,
                        categoryId = 1, // You need to replace this with the actual category ID
                        sellingPrice = sellingPrice,
                        imageUri = it1,
                        code = barcode
                    )
                }
                // Send the Product object to the Database Handler for insertion
                if (product != null) {
                    dbHelper.addProduct(product)
                    listener?.onItemAdded() // Notify the listener
                }
                dismiss() // Close the dialog after adding the item
            } else {
                Toast.makeText(requireContext(), "Please check inputs", Toast.LENGTH_SHORT).show()
            }
        }
        // Handle image upload button click
        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }
        captureImageButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile: File? = try {
                createImageFile() // You need to implement createImageFile() method to create a file to store the image
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.appdev.posheesh.fileprovider",
                    it
                )
                capturedPhotoUri = photoURI // Save the URI to the member variable
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST_CODE)
        }
        builder.setView(view)
        return builder.create()
    }
    fun setOnItemAddedListener(listener: OnItemAddedListener) {
        this.listener = listener
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
    private fun copyImageToInternalStorage(originalImageUri: Uri): Uri? {
        val inputStream = context?.contentResolver?.openInputStream(originalImageUri)
        val fileName = "copied_image_${System.currentTimeMillis()}.jpg"
        val outputFile = context?.filesDir?.let { File(it, fileName) }
        val outputStream = FileOutputStream(outputFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return Uri.fromFile(outputFile)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val originalImageUri: Uri? = data.data
            val copiedImageUri: Uri? = originalImageUri?.let { copyImageToInternalStorage(it) }
            selectedImageUri = copiedImageUri?.let { getFilePathFromUri(it) }
        } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Check if the captured image URI is not null
            if(capturedPhotoUri != null) {
                // Save the captured image to internal storage
                val filePath = copyImageToInternalStorage(capturedPhotoUri!!)

                // Update selectedImageUri with the file path
                selectedImageUri = getFilePathFromUri(filePath!!)
            }
        }

    }
    fun getFilePathFromUri(uri: Uri): String? {
        var filePath: String? = null
        val context = context // Replace applicationContext with your actual context reference

        // Using ContentResolver to query MediaStore for file path
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex: Int = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                filePath = it.getString(columnIndex)
            }
        }
        cursor?.close()

        // If file path is still null, try using FileProvider
        if (filePath == null) {
            filePath = uri.path // Fallback to URI path if cursor is null
        }

        return filePath
    }
    companion object {
        // Define request codes for image selection and capture
        private const val PICK_IMAGE_REQUEST_CODE = 1
        private const val CAPTURE_IMAGE_REQUEST_CODE = 2
    }
}
