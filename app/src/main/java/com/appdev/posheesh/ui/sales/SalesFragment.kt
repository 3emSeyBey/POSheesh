package com.appdev.posheesh.ui.sales

import ItemListAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appdev.posheesh.R
import com.appdev.posheesh.databinding.FragmentSalesBinding
import com.appdev.posheesh.DatabaseHandler

class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private lateinit var listView: ListView
    private lateinit var dbHelper: DatabaseHandler
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        val view = binding.root

        dbHelper = DatabaseHandler(requireContext())

        // Find the ListView
        listView = view.findViewById(R.id.listViewSalesItems)

        // Set up sample data for the ListView
        val items = dbHelper.getAllProducts().toTypedArray()

        // Create and set the custom adapter
        val adapter = ItemListAdapter(requireContext(), items)
        listView.adapter = adapter

        // Set item click listener
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            // Handle item click here
            val selectedItem = items[position].name
            Toast.makeText(requireContext(), "Clicked: $selectedItem", Toast.LENGTH_SHORT).show()
            // Show the pop-up modal for modifying the quantity and adding the item to the cart
            showItemDialog(selectedItem)

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the Spinner and setup filter options
        val spinner: Spinner = view.findViewById(R.id.spinnerFilter)
        val filterOptions = arrayOf("Item", "Services", "Others")

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinner.adapter = adapter

        // Set an item selection listener for the spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle item selection
                val selectedOption = filterOptions[position]
                // Do something with the selected option
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

    private fun showItemDialog(itemName: String) {
        val dialog = ItemDialogFragment.newInstance(itemName)
        dialog.show(parentFragmentManager, "ItemDialogFragment")
        
    }
}
