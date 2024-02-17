package com.appdev.posheesh.ui.sales

import CartFragment
import ItemListAdapter
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.posheesh.BarcodeScan
import com.appdev.posheesh.Classes.FragmentChangeListener
import com.appdev.posheesh.Classes.Products
import com.appdev.posheesh.R
import com.appdev.posheesh.databinding.FragmentSalesBinding
import com.appdev.posheesh.DatabaseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale
import java.util.Objects

class SalesFragment : Fragment(), ItemListAdapter.ItemClickListener  {
    companion object {
        val cart: MutableList<Map<String, Int>> = mutableListOf() // Change to MutableList

        fun addToCart(itemId: Int, itemQuantity: Int) {
            val map: Map<String, Int> = mapOf("id" to itemId, "quantity" to itemQuantity)
            cart.add(map)
        }
    }

    private var _binding: FragmentSalesBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: TextView
    private lateinit var dbHelper: DatabaseHandler
    private lateinit var fabShowCart: FloatingActionButton
    private lateinit var micImg: ImageView
    private val binding get() = _binding!!
    private var spinnerIndex: Int = 0
    private var fragmentChangeListener: FragmentChangeListener? = null
    private val REQUEST_CODE_SPEECH_INPUT = 1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        val view = binding.root

        dbHelper = DatabaseHandler(requireContext())

        fabShowCart = view.findViewById(R.id.fabShowCart)

        // Set OnClickListener to handle FAB clicks
        fabShowCart.setOnClickListener {
            // Add your logic here to show the cart's contents
            // For example, you can navigate to the cart fragment
            showCart()
        }


        // Find the ListView
        recyclerView = view.findViewById(R.id.recyclerViewSalesItems)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // Set up GridLayoutManager with 2 columns


        val scanBarcodeImg: ImageView = view.findViewById(R.id.cameraIcon)
        scanBarcodeImg.setOnClickListener {
            val intent = Intent(requireActivity(), BarcodeScan::class.java)
            startActivity(intent)
        }

        micImg = view.findViewById(R.id.microphoneIcon)
        micImg.setOnClickListener{
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast
                    .makeText(
                        this@SalesFragment.context, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }

        // Display items based on the category ID
        displayItemsByCategoryId(0)

        etSearch = view.findViewById(R.id.etSearch)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the items list based on the search text
                displayItemsByNameSearch(s.toString(), spinnerIndex)
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })
        return view
    }
    //All about Barcode Scanning//
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // in this method we are checking request
        // code with our result code.
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            // on below line we are checking if result code is ok
            if (resultCode == RESULT_OK && data != null) {

                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                // on below line we are setting data
                // to our output text view\
                etSearch.setText(
                    Objects.requireNonNull(res)[0]
                )
            }
        }
    }
    //End Barcode Scanning Function//
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangeListener) {
            fragmentChangeListener = context
        } else {
            throw RuntimeException("$context must implement FragmentChangeListener")
        }
    }
    override fun onStart() {
        super.onStart()
        fragmentChangeListener?.onFragmentChanged("Sales")
    }
    override fun onStop() {
        super.onStop()
        // Clear fragmentChangeListener reference to avoid memory leaks
        fragmentChangeListener = null
    }
    private fun displayItemsByCategoryId(categoryId: Int) {
        val items = dbHelper.getProductsByCategoryId(categoryId).toTypedArray()

        // Create and set the custom adapter with item click listener
        val adapter = ItemListAdapter(requireContext(), items, this)
        recyclerView.adapter = adapter
    }
    private fun displayItemsByNameSearch(nameString :String, categoryId: Int) {
        val items = dbHelper.getProductsByNameSearch(nameString, categoryId).toTypedArray()

        // Create and set the custom adapter
        // Create and set the custom adapter with item click listener
        val adapter = ItemListAdapter(requireContext(), items, this)
        recyclerView.adapter = adapter
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHandler(requireContext())

        // Initialize the Spinner and setup filter options
        val spinner: Spinner = view.findViewById(R.id.spinnerFilter)
        val categories = dbHelper.getAllCategory().toTypedArray()
        val categoryList = mutableListOf<String>()
        categoryList.add("All Items")
        categories.forEach { item ->
            categoryList.add(item.name)
        }
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryList)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinner.adapter = adapter

        // Set an item selection listener for the spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle item selection
                displayItemsByCategoryId(position)
                spinnerIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun showItemDialog(item: Products) {
        val dialog = ItemDialogFragment.newInstance(item)
        dialog.show(parentFragmentManager, "ItemDialogFragment")
    }
    private fun showCart(){
        val dialog = CartFragment(cart)
        dialog.show(parentFragmentManager, "CartFragment")
    }
    override fun onItemClick(item: Products) {
        showItemDialog(item)
    }
}
