package com.appdev.posheesh.ui.inventory

import EditSupplyDialog
import SupplyListAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appdev.posheesh.Classes.Supplies
import com.appdev.posheesh.DatabaseHandler
import com.appdev.posheesh.R
import com.appdev.posheesh.databinding.FragmentInventoryBinding

class InventoryFragment : Fragment(), SupplyListAdapter.OnItemClickListener{

    interface EditSupplyDialogListener {
        fun onSupplyQuantityUpdated(supplyId: Int, newQuantity: Int)
    }

    private var _binding: FragmentInventoryBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: TextView
    private lateinit var dbHelper: DatabaseHandler

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)

        val view = binding.root
        dbHelper = DatabaseHandler(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewInventoryItems)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // Set up GridLayoutManager with 2 columns

        etSearch = view.findViewById(R.id.etSearch)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the items list based on the search text
                displayItemsByNameSearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })
        displayItemsByNameSearch("")

        return binding.root


    }
     fun onSupplyQuantityUpdated(supplyId: Int, newQuantity: Int) {
        // Update the UI to reflect the changes
        val adapter = recyclerView.adapter as SupplyListAdapter
        adapter.updateSupplyQuantity(supplyId, newQuantity)
    }
    override fun onEditClick(item: Supplies) {
        // Show edit dialog
        val editDialog = EditSupplyDialog(item, this)
        editDialog.show(childFragmentManager, "EditSupplyDialog")
    }
    private fun displayItemsByNameSearch(nameString :String) {
        val items = dbHelper.getSuppliesByNameSearch(nameString).toTypedArray()

        // Create and set the custom adapter
        // Create and set the custom adapter with item click listener
        val adapter = SupplyListAdapter(requireContext(), items, this)
        recyclerView.adapter = adapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}