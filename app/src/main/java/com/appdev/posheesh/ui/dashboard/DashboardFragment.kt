package com.appdev.posheesh.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.TextView
import android.widget.ImageView
import com.appdev.posheesh.AnalyticsManager
import com.appdev.posheesh.Classes.*
import com.appdev.posheesh.databinding.FragmentDashboardBinding
import com.appdev.posheesh.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var recentActivityAdapter: RecentActivityAdapter
    
    // Chart data
    private lateinit var salesAnalytics: SalesAnalytics
    private lateinit var inventoryAnalytics: InventoryAnalytics
    private lateinit var businessKPIs: BusinessKPIs

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        analyticsManager = AnalyticsManager(requireContext())
        setupDashboard()
        setupCharts()
        setupRecentActivity()
        updateLastUpdated()
    }

    private fun setupDashboard() {
        try {
            // Generate analytics data
            businessKPIs = analyticsManager.generateBusinessKPIs()
            salesAnalytics = analyticsManager.generateSalesAnalytics()
            inventoryAnalytics = analyticsManager.generateInventoryAnalytics()
            
            // Update KPI cards
            updateKPICards()
        } catch (e: Exception) {
            // Handle any errors during dashboard setup
            // You can show a toast or log the error here
        }
    }

    private fun updateKPICards() {
        binding.tvTotalSales.text = analyticsManager.formatCurrency(businessKPIs.totalRevenue)
        binding.tvTotalOrders.text = businessKPIs.totalOrders.toString()
        binding.tvProductsCount.text = inventoryAnalytics.totalProducts.toString()
        binding.tvLowStockCount.text = inventoryAnalytics.lowStockItems.toString()
    }

    private fun setupCharts() {
        try {
            setupSalesTrendChart()
            setupTopProductsChart()
            setupCategoryDistributionChart()
        } catch (e: Exception) {
            // Handle any errors during chart setup
            // You can show a toast or log the error here
        }
    }

    private fun setupSalesTrendChart() {
        val lineChart = binding.lineChartSales
        
        // Configure chart appearance
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        
        // Configure axes
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        
        val yAxis = lineChart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.axisMinimum = 0f
        
        lineChart.axisRight.isEnabled = false
        
        // Create data entries
        if (salesAnalytics.salesByDate.isNotEmpty()) {
            val entries = salesAnalytics.salesByDate.mapIndexed { index, dailySales ->
                Entry(index.toFloat(), dailySales.sales.toFloat())
            }
            
            val dataSet = LineDataSet(entries, "Daily Sales").apply {
                color = Color.parseColor("#4CAF50")
                setCircleColor(Color.parseColor("#4CAF50"))
                lineWidth = 3f
                circleRadius = 5f
                setDrawValues(true)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            
            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(dataSet)
            
            val lineData = LineData(dataSets)
            lineChart.data = lineData
            
            // Set X-axis labels
            val dateLabels = salesAnalytics.salesByDate.map { dailySales ->
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(dailySales.date)
            }
            xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
        } else {
            // Create empty chart if no data
            val emptyEntries = listOf(Entry(0f, 0f))
            val emptyDataSet = LineDataSet(emptyEntries, "No Data").apply {
                color = Color.parseColor("#CCCCCC")
                setCircleColor(Color.parseColor("#CCCCCC"))
                lineWidth = 2f
                circleRadius = 3f
                setDrawValues(false)
            }
            
            val emptyDataSets = ArrayList<ILineDataSet>()
            emptyDataSets.add(emptyDataSet)
            
            val emptyLineData = LineData(emptyDataSets)
            lineChart.data = emptyLineData
        }
        
        lineChart.invalidate()
    }

    private fun setupTopProductsChart() {
        val barChart = binding.barChartProducts
        
        // Configure chart appearance
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.setTouchEnabled(true)
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(true)
        barChart.setPinchZoom(true)
        
        // Configure axes
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -45f
        
        val yAxis = barChart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.axisMinimum = 0f
        
        barChart.axisRight.isEnabled = false
        
        // Create data entries
        if (salesAnalytics.topSellingProducts.isNotEmpty()) {
            val entries = salesAnalytics.topSellingProducts.mapIndexed { index, product ->
                BarEntry(index.toFloat(), product.revenue.toFloat())
            }
            
            val dataSet = BarDataSet(entries, "Product Revenue").apply {
                color = Color.parseColor("#2196F3")
                setDrawValues(true)
            }
            
            val barData = BarData(dataSet)
            barChart.data = barData
            
            // Set X-axis labels
            val productLabels = salesAnalytics.topSellingProducts.map { it.productName }
            xAxis.valueFormatter = IndexAxisValueFormatter(productLabels)
        } else {
            // Create empty chart if no data
            val emptyEntries = listOf(BarEntry(0f, 0f))
            val emptyDataSet = BarDataSet(emptyEntries, "No Data").apply {
                color = Color.parseColor("#CCCCCC")
                setDrawValues(false)
            }
            
            val emptyBarData = BarData(emptyDataSet)
            barChart.data = emptyBarData
        }
        
        barChart.invalidate()
    }

    private fun setupCategoryDistributionChart() {
        val pieChart = binding.pieChartCategories
        
        // Configure chart appearance
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        
        // Create data entries
        if (salesAnalytics.salesByCategory.isNotEmpty()) {
            val entries = salesAnalytics.salesByCategory.mapIndexed { index, category ->
                PieEntry(category.percentage.toFloat(), category.categoryName)
            }
            
            val dataSet = PieDataSet(entries, "Category Sales").apply {
                colors = analyticsManager.getChartColors()
                valueTextSize = 14f
                valueTextColor = Color.WHITE
                valueFormatter = PercentFormatter(pieChart)
            }
            
            val pieData = PieData(dataSet)
            pieChart.data = pieData
        } else {
            // Create empty chart if no data
            val emptyEntries = listOf(PieEntry(100f, "No Data"))
            val emptyDataSet = PieDataSet(emptyEntries, "No Data").apply {
                colors = listOf(Color.parseColor("#CCCCCC"))
                valueTextSize = 14f
                valueTextColor = Color.WHITE
                valueFormatter = PercentFormatter(pieChart)
            }
            
            val emptyPieData = PieData(emptyDataSet)
            pieChart.data = emptyPieData
        }
        
        pieChart.invalidate()
    }

    private fun setupRecentActivity() {
        val recyclerView = binding.rvRecentActivity
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Generate mock recent activity data
        val recentActivities = generateRecentActivities()
        recentActivityAdapter = RecentActivityAdapter(recentActivities)
        recyclerView.adapter = recentActivityAdapter
    }

    private fun generateRecentActivities(): List<RecentActivity> {
        return listOf(
            RecentActivity("New order received", "Order #1234 - ₱156.00", "2 minutes ago", ActivityType.ORDER),
            RecentActivity("Low stock alert", "Plastic cups running low", "15 minutes ago", ActivityType.INVENTORY),
            RecentActivity("Payment completed", "Order #1233 - Cash payment", "1 hour ago", ActivityType.PAYMENT),
            RecentActivity("New product added", "Iced Caramel Macchiato", "2 hours ago", ActivityType.PRODUCT),
            RecentActivity("Daily sales report", "Generated for today", "3 hours ago", ActivityType.REPORT)
        )
    }

    private fun updateLastUpdated() {
        binding.tvLastUpdated.text = "Last updated: ${analyticsManager.getCurrentTimestamp()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data class for recent activities
data class RecentActivity(
    val title: String,
    val description: String,
    val timeAgo: String,
    val type: ActivityType
)

enum class ActivityType {
    ORDER,
    INVENTORY,
    PAYMENT,
    PRODUCT,
    REPORT
}

// Adapter for recent activities
class RecentActivityAdapter(private val activities: List<RecentActivity>) : 
    androidx.recyclerview.widget.RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.tvActivityTitle)
        val descriptionText: TextView = view.findViewById(R.id.tvActivityDescription)
        val timeText: TextView = view.findViewById(R.id.tvActivityTime)
        val iconView: ImageView = view.findViewById(R.id.ivActivityIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]
        
        holder.titleText.text = activity.title
        holder.descriptionText.text = activity.description
        holder.timeText.text = activity.timeAgo
        
        // Set icon based on activity type
        val iconRes = when (activity.type) {
            ActivityType.ORDER -> R.drawable.ic_cart
            ActivityType.INVENTORY -> R.drawable.ic_inventory
            ActivityType.PAYMENT -> R.drawable.ic_cash
            ActivityType.PRODUCT -> R.drawable.ic_additem
            ActivityType.REPORT -> R.drawable.ic_reports
        }
        holder.iconView.setImageResource(iconRes)
    }

    override fun getItemCount() = activities.size
}