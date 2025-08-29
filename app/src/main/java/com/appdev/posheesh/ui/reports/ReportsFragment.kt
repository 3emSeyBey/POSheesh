package com.appdev.posheesh.ui.reports

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.appdev.posheesh.DatabaseHandler
import java.io.File
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.appdev.posheesh.AnalyticsManager
import com.appdev.posheesh.Classes.*
import com.appdev.posheesh.databinding.FragmentReportsBinding
import com.appdev.posheesh.ui.sales.ExcelHandler
import com.appdev.posheesh.R
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var detailedStatsAdapter: DetailedStatsAdapter
    
    // Date range for filtering
    private var startDate: Date? = null
    private var endDate: Date? = null
    
    // Analytics data
    private lateinit var salesAnalytics: SalesAnalytics
    private lateinit var inventoryAnalytics: InventoryAnalytics
    private lateinit var timeAnalytics: TimeAnalytics
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        analyticsManager = AnalyticsManager(requireContext())
        setupDatePickers()
        setupButtons()
        setupCharts()
        setupDetailedStats()
        
        // Set default date range (last 30 days)
        setDefaultDateRange()
    }

    private fun setupDatePickers() {
        binding.btnStartDate.setOnClickListener {
            showDatePicker(true)
        }
        
        binding.btnEndDate.setOnClickListener {
            showDatePicker(false)
        }
    }

    private fun setupButtons() {
        binding.btnGenerateReport.setOnClickListener {
            generateReport()
        }
        
        binding.btnExportExcel.setOnClickListener {
            exportToExcel()
        }
        

    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = calendar.time
                
                if (isStartDate) {
                    startDate = selectedDate
                    binding.btnStartDate.text = dateFormat.format(selectedDate)
                } else {
                    endDate = selectedDate
                    binding.btnEndDate.text = dateFormat.format(selectedDate)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }

    private fun setDefaultDateRange() {
        val calendar = Calendar.getInstance()
        
        // Set end date to today
        endDate = calendar.time
        binding.btnEndDate.text = dateFormat.format(endDate!!)
        
        // Set start date to 30 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        startDate = calendar.time
        binding.btnStartDate.text = dateFormat.format(startDate!!)
    }

    private fun generateReport() {
        if (startDate == null || endDate == null) {
            Toast.makeText(context, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (startDate!!.after(endDate!!)) {
            Toast.makeText(context, "Start date cannot be after end date", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Generate analytics data for the selected date range
        salesAnalytics = analyticsManager.generateSalesAnalytics()
        inventoryAnalytics = analyticsManager.generateInventoryAnalytics()
        timeAnalytics = analyticsManager.generateTimeAnalytics()
        
        // Update all charts with real data
        updateChartsWithRealData()
        updateDetailedStats()
        
        Toast.makeText(context, "Report generated successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun updateChartsWithRealData() {
        val startDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate!!)
        val endDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate!!)
        
        // Update sales performance chart with real data
        updateSalesPerformanceChart(startDateStr, endDateStr)
        
        // Update product performance chart with real data
        updateProductPerformanceChart(startDateStr, endDateStr)
        
        // Update inventory status chart with real data
        updateInventoryStatusChart()
        
        // Update revenue analysis chart with real data
        updateRevenueAnalysisChart(startDateStr, endDateStr)
    }

    private fun setupCharts() {
        setupSalesPerformanceChart()
        setupProductPerformanceChart()
        setupInventoryStatusChart()
        setupRevenueAnalysisChart()
    }

    private fun setupSalesPerformanceChart() {
        val combinedChart = binding.combinedChartSales
        
        // Configure chart appearance
        combinedChart.description.isEnabled = false
        combinedChart.legend.isEnabled = true
        combinedChart.setTouchEnabled(true)
        combinedChart.isDragEnabled = true
        combinedChart.setScaleEnabled(true)
        combinedChart.setPinchZoom(true)
        
        // Configure axes
        val xAxis = combinedChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        
        val yAxis = combinedChart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.axisMinimum = 0f
        
        combinedChart.axisRight.isEnabled = false
        
        // Create line data (sales trend)
        val salesEntries = (0..6).map { Entry(it.toFloat(), (1000..5000).random().toFloat()) }
        val salesDataSet = LineDataSet(salesEntries, "Sales Trend").apply {
            color = Color.parseColor("#4CAF50")
            setCircleColor(Color.parseColor("#4CAF50"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        // Create bar data (orders)
        val orderEntries = (0..6).map { BarEntry(it.toFloat(), (5..25).random().toFloat()) }
        val orderDataSet = BarDataSet(orderEntries, "Orders").apply {
            color = Color.parseColor("#2196F3")
            setDrawValues(false)
        }
        
        val lineData = LineData(salesDataSet)
        val barData = BarData(orderDataSet)
        
        val combinedData = CombinedData()
        combinedData.setData(lineData)
        combinedData.setData(barData)
        
        combinedChart.data = combinedData
        
        // Set X-axis labels
        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        
        combinedChart.invalidate()
    }

    private fun setupProductPerformanceChart() {
        val horizontalBarChart = binding.horizontalBarChartProducts
        
        // Configure chart appearance
        horizontalBarChart.description.isEnabled = false
        horizontalBarChart.legend.isEnabled = true
        horizontalBarChart.setTouchEnabled(true)
        horizontalBarChart.isDragEnabled = true
        horizontalBarChart.setScaleEnabled(true)
        horizontalBarChart.setPinchZoom(true)
        
        // Configure axes
        val xAxis = horizontalBarChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        
        val yAxis = horizontalBarChart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.axisMinimum = 0f
        
        horizontalBarChart.axisRight.isEnabled = false
        
        // Create data entries
        val entries = (0..4).map { BarEntry(it.toFloat(), (100..500).random().toFloat()) }
        val dataSet = BarDataSet(entries, "Product Revenue").apply {
            color = Color.parseColor("#FF9800")
            setDrawValues(true)
        }
        
        val barData = BarData(dataSet)
        horizontalBarChart.data = barData
        
        // Set Y-axis labels (product names)
        val productLabels = listOf("Iced Caramel", "Don Darko", "Donya Berry", "Don Matchatto", "Espresso")
        horizontalBarChart.axisLeft.valueFormatter = IndexAxisValueFormatter(productLabels)
        
        horizontalBarChart.invalidate()
    }

    private fun setupInventoryStatusChart() {
        val radarChart = binding.radarChartInventory

        // Configure chart appearance
        radarChart.description.isEnabled = false
        radarChart.legend.isEnabled = true
        radarChart.setTouchEnabled(true)
        radarChart.isRotationEnabled = true

        // Create data entries using RadarEntry
        val entries = (0..5).map { RadarEntry((50..100).random().toFloat()) } // Changed Entry to RadarEntry
        val dataSet = RadarDataSet(entries, "Stock Levels").apply {
            color = Color.parseColor("#9C27B0")
            fillColor = Color.parseColor("#9C27B0")
            fillAlpha = 150
            lineWidth = 2f
            setDrawValues(true)
        }

        val radarData = RadarData(dataSet)
        radarChart.data = radarData

        // Set labels for radar chart
        val labels = listOf("Cups", "Straws", "Syrups", "Coffee", "Milk", "Ice")
        radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        radarChart.invalidate()
    }


    private fun setupRevenueAnalysisChart() {
        val pieChart = binding.doughnutChartRevenue
        
        // Configure chart appearance
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        
        // Create data entries
        val entries = (0..3).map { PieEntry((20..40).random().toFloat(), "Category ${it + 1}") }
        
        val dataSet = PieDataSet(entries, "Revenue by Category").apply {
            colors = analyticsManager.getChartColors()
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(pieChart)
        }
        
        val pieData = PieData(dataSet)
        pieChart.data = pieData
        
        pieChart.invalidate()
    }

    private fun updateCharts() {
        // Refresh all charts with new data
        binding.combinedChartSales.invalidate()
        binding.horizontalBarChartProducts.invalidate()
        binding.radarChartInventory.invalidate()
        binding.doughnutChartRevenue.invalidate()
    }

    private fun updateSalesPerformanceChart(startDate: String, endDate: String) {
        val salesData = analyticsManager.getSalesByDateRange(startDate, endDate)
        val combinedChart = binding.combinedChartSales
        
        if (salesData.isNotEmpty()) {
            // Create line data (sales trend)
            val salesEntries = salesData.mapIndexed { index, saleData ->
                Entry(index.toFloat(), (saleData["sales"] as Double).toFloat())
            }
            val salesDataSet = LineDataSet(salesEntries, "Sales Trend").apply {
                color = Color.parseColor("#4CAF50")
                setCircleColor(Color.parseColor("#4CAF50"))
                lineWidth = 3f
                circleRadius = 5f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            // Create bar data (orders)
            val orderEntries = salesData.mapIndexed { index, saleData ->
                BarEntry(index.toFloat(), (saleData["orders"] as Int).toFloat())
            }
            val orderDataSet = BarDataSet(orderEntries, "Orders").apply {
                color = Color.parseColor("#2196F3")
                setDrawValues(false)
            }
            
            val lineData = LineData(salesDataSet)
            val barData = BarData(orderDataSet)
            
            val combinedData = CombinedData()
            combinedData.setData(lineData)
            combinedData.setData(barData)
            
            combinedChart.data = combinedData
            
            // Set X-axis labels
            val dateLabels = salesData.map { saleData ->
                val date = saleData["date"] as String
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)?.let {
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(it)
                } ?: date
            }
            combinedChart.xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
            
            combinedChart.invalidate()
        }
    }

    private fun updateProductPerformanceChart(startDate: String, endDate: String) {
        val topProducts = analyticsManager.getTopSellingProducts(5)
        val horizontalBarChart = binding.horizontalBarChartProducts
        
        if (topProducts.isNotEmpty()) {
            // Create data entries
            val entries = topProducts.mapIndexed { index, productData ->
                BarEntry(index.toFloat(), (productData["totalRevenue"] as Double).toFloat())
            }
            val dataSet = BarDataSet(entries, "Product Revenue").apply {
                color = Color.parseColor("#FF9800")
                setDrawValues(true)
            }
            
            val barData = BarData(dataSet)
            horizontalBarChart.data = barData
            
            // Set Y-axis labels (product names)
            val productLabels = topProducts.map { it["productName"] as String }
            horizontalBarChart.axisLeft.valueFormatter = IndexAxisValueFormatter(productLabels)
            
            horizontalBarChart.invalidate()
        }
    }

    private fun updateInventoryStatusChart() {
        val lowStockSupplies = analyticsManager.getLowStockSupplies()
        val radarChart = binding.radarChartInventory
        
        if (lowStockSupplies.isNotEmpty()) {
            // Create data entries
            val entries = lowStockSupplies.mapIndexed { index, supplyData ->
                val currentQty = supplyData["supplyQuantity"] as Int
                val criticalLevel = supplyData["criticalLevel"] as Int
                val percentage = if (criticalLevel > 0) (currentQty.toFloat() / criticalLevel.toFloat()) * 100 else 0f
                RadarEntry(percentage)
            }
            val dataSet = RadarDataSet(entries, "Stock Levels").apply {
                color = Color.parseColor("#9C27B0")
                fillColor = Color.parseColor("#9C27B0")
                fillAlpha = 150
                lineWidth = 2f
                setDrawValues(true)
            }
            
            val radarData = RadarData(dataSet)
            radarChart.data = radarData
            
            // Set labels for radar chart
            val labels = lowStockSupplies.map { it["supplyName"] as String }
            radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            
            radarChart.invalidate()
        }
    }

    private fun updateRevenueAnalysisChart(startDate: String, endDate: String) {
        val categorySales = analyticsManager.getSalesByCategory(startDate, endDate)
        val pieChart = binding.doughnutChartRevenue
        
        if (categorySales.isNotEmpty()) {
            // Create data entries
            val entries = categorySales.mapIndexed { index, categoryData ->
                val sales = categoryData["categorySales"] as Double
                val categoryName = categoryData["categoryName"] as String
                PieEntry(sales.toFloat(), categoryName)
            }
            
            val dataSet = PieDataSet(entries, "Revenue by Category").apply {
                colors = analyticsManager.getChartColors()
                valueTextSize = 14f
                valueTextColor = Color.WHITE
                valueFormatter = PercentFormatter(pieChart)
            }
            
            val pieData = PieData(dataSet)
            pieChart.data = pieData
            
            pieChart.invalidate()
        }
    }

    private fun setupDetailedStats() {
        val recyclerView = binding.rvDetailedStats
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Initialize with empty data
        val emptyStats = listOf<DetailedStat>()
        detailedStatsAdapter = DetailedStatsAdapter(emptyStats)
        recyclerView.adapter = detailedStatsAdapter
    }

    private fun updateDetailedStats() {
        val stats = generateDetailedStats()
        detailedStatsAdapter.updateData(stats)
    }

    private fun generateDetailedStats(): List<DetailedStat> {
        return listOf(
            DetailedStat("Total Revenue", analyticsManager.formatCurrency(salesAnalytics.totalSales), "₱"),
            DetailedStat("Total Orders", salesAnalytics.totalOrders.toString(), ""),
            DetailedStat("Average Order Value", analyticsManager.formatCurrency(salesAnalytics.averageOrderValue), "₱"),
            DetailedStat("Top Product", salesAnalytics.topSellingProducts.firstOrNull()?.productName ?: "N/A", ""),
            DetailedStat("Low Stock Items", inventoryAnalytics.lowStockItems.toString(), ""),
            DetailedStat("Inventory Value", analyticsManager.formatCurrency(inventoryAnalytics.inventoryValue), "₱")
        )
    }

    private fun exportToExcel() {
        try {
            val dbHelper = DatabaseHandler(requireContext())
            val database = dbHelper.writableDatabase
            val excelHandler = ExcelHandler()
            
            val startDateStr = startDate?.let { dateFormat.format(it) }
            val endDateStr = endDate?.let { dateFormat.format(it) }
            
            val file = excelHandler.exportToExcel(requireContext(), database, startDateStr, endDateStr)
            
            Toast.makeText(context, "Excel report exported successfully: ${file.name}", Toast.LENGTH_LONG).show()
            
            // Share the file
            shareFile(file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            
        } catch (e: Exception) {
            Toast.makeText(context, "Error exporting Excel: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    
    private fun shareFile(file: File, mimeType: String) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "com.appdev.posheesh.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "POSheesh Business Report")
                putExtra(Intent.EXTRA_TEXT, "Please find attached the POSheesh business report.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(intent, "Share Report"))
            
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data class for detailed statistics
data class DetailedStat(
    val label: String,
    val value: String,
    val unit: String
)

// Adapter for detailed statistics
class DetailedStatsAdapter(private var stats: List<DetailedStat>) : 
    androidx.recyclerview.widget.RecyclerView.Adapter<DetailedStatsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val labelText: TextView = view.findViewById(R.id.tvStatLabel)
        val valueText: TextView = view.findViewById(R.id.tvStatValue)
        val unitText: TextView = view.findViewById(R.id.tvStatUnit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detailed_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stat = stats[position]
        
        holder.labelText.text = stat.label
        holder.valueText.text = stat.value
        holder.unitText.text = stat.unit
    }

    override fun getItemCount() = stats.size
    
    fun updateData(newStats: List<DetailedStat>) {
        stats = newStats
        notifyDataSetChanged()
    }
}