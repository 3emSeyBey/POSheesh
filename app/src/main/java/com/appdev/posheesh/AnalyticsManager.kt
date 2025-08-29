package com.appdev.posheesh

import android.content.Context

import com.appdev.posheesh.Classes.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import java.util.Random

class AnalyticsManager(private val context: Context) {
    
    private lateinit var dbHelper: DatabaseHandler
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
                    init {
                    dbHelper = DatabaseHandler(context)
                }
    
                    // Generate comprehensive business KPIs
                fun generateBusinessKPIs(): BusinessKPIs {
                    val totalRevenue = dbHelper.getTotalSales()
                    val totalOrders = dbHelper.getTotalOrders()
                    val averageOrderValue = dbHelper.getAverageOrderValue()
                    val customerCount = 15 // TODO: Implement customer count query
                    val topPerformingDay = "Friday" // TODO: Implement top performing day query
                    val bestSellingProduct = getBestSellingProduct()

                    return BusinessKPIs(
                        totalRevenue = totalRevenue,
                        totalOrders = totalOrders,
                        averageOrderValue = averageOrderValue,
                        customerCount = customerCount,
                        topPerformingDay = topPerformingDay,
                        bestSellingProduct = bestSellingProduct
                    )
                }

    private fun getBestSellingProduct(): String {
        val topProducts = dbHelper.getTopSellingProducts(1)
        return if (topProducts.isNotEmpty()) {
            topProducts[0]["productName"] as String
        } else {
            "No products"
        }
    }
    
    // Generate sales analytics
    fun generateSalesAnalytics(): SalesAnalytics {
        try {
            val totalSales = dbHelper.getTotalSales()
            val totalOrders = dbHelper.getTotalOrders()
            val averageOrderValue = dbHelper.getAverageOrderValue()
            
            // Get top selling products from database
            val topSellingProductsData = dbHelper.getTopSellingProducts(5)
            val topSellingProducts = topSellingProductsData.map { productData ->
                ProductPerformance(
                    productName = productData["productName"] as String,
                    productCode = productData["productCode"] as String,
                    quantitySold = productData["totalQuantity"] as Int,
                    revenue = productData["totalRevenue"] as Double,
                    percentageOfTotal = 0.0 // Will be calculated below
                )
            }
            
            // Calculate percentages
            val totalRevenue = topSellingProducts.sumOf { it.revenue }
            val topSellingProductsWithPercentages = topSellingProducts.map { product ->
                product.copy(percentageOfTotal = if (totalRevenue > 0) (product.revenue / totalRevenue * 100).roundToInt().toDouble() else 0.0)
            }
            
            // Get daily sales for last 7 days
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val salesByDateData = dbHelper.getSalesByDateRange(startDate, endDate)
            val salesByDate = salesByDateData.map { saleData ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(saleData["date"] as String) ?: Date()
                DailySales(
                    date = date,
                    sales = saleData["sales"] as Double,
                    orders = saleData["orders"] as Int
                )
            }
            
            // Get category sales from database
            val salesByCategoryData = dbHelper.getSalesByCategory(startDate, endDate)
            val salesByCategory = salesByCategoryData.map { categoryData ->
                val categorySales = categoryData["categorySales"] as Double
                CategorySales(
                    categoryName = categoryData["categoryName"] as String,
                    sales = categorySales,
                    percentage = if (totalSales > 0) (categorySales / totalSales * 100).roundToInt().toDouble() else 0.0
                )
            }
            
            return SalesAnalytics(
                totalSales = totalSales,
                totalOrders = totalOrders,
                averageOrderValue = averageOrderValue,
                topSellingProducts = topSellingProductsWithPercentages,
                salesByDate = salesByDate,
                salesByCategory = salesByCategory
            )
        } catch (e: Exception) {
            // Return default values if database access fails
            return SalesAnalytics(
                totalSales = 0.0,
                totalOrders = 0,
                averageOrderValue = 0.0,
                topSellingProducts = emptyList(),
                salesByDate = emptyList(),
                salesByCategory = emptyList()
            )
        }
    }
    
    // Generate inventory analytics
    fun generateInventoryAnalytics(): InventoryAnalytics {
        try {
            val products = dbHelper.getProductsByCategoryId(0)
            val supplies = dbHelper.getSuppliesByNameSearch("")
            val categories = dbHelper.getAllCategory()
            
            val totalProducts = products.size
            val lowStockItems = supplies.count { it.supplyQuantity <= it.crticalLevel }
            val outOfStockItems = supplies.count { it.supplyQuantity == 0 }
            val inventoryValue = dbHelper.getInventoryValue()
            
            // Generate category distribution
            val categoryDistribution = categories.map { category ->
                val categoryProducts = products.filter { it.categoryId == category.id }
                CategoryInventory(
                    categoryName = category.name,
                    productCount = categoryProducts.size,
                    totalValue = categoryProducts.sumOf { it.sellingPrice * 10 }
                )
            }
            
            // Generate stock levels
            val stockLevels = supplies.map { supply ->
                val status = when {
                    supply.supplyQuantity == 0 -> StockStatus.OUT_OF_STOCK
                    supply.supplyQuantity <= supply.crticalLevel -> StockStatus.LOW_STOCK
                    supply.supplyQuantity > supply.crticalLevel * 2 -> StockStatus.OVERSTOCKED
                    else -> StockStatus.NORMAL
                }
                
                StockLevel(
                    supplyName = supply.supplyName,
                    currentQuantity = supply.supplyQuantity,
                    criticalLevel = supply.crticalLevel,
                    status = status
                )
            }
            
            return InventoryAnalytics(
                totalProducts = totalProducts,
                lowStockItems = lowStockItems,
                outOfStockItems = outOfStockItems,
                inventoryValue = inventoryValue,
                categoryDistribution = categoryDistribution,
                stockLevels = stockLevels
            )
        } catch (e: Exception) {
            // Return default values if database access fails
            return InventoryAnalytics(
                totalProducts = 0,
                lowStockItems = 0,
                outOfStockItems = 0,
                inventoryValue = 0.0,
                categoryDistribution = emptyList(),
                stockLevels = emptyList()
            )
        }
    }
    
    // Generate time-based analytics
    fun generateTimeAnalytics(): TimeAnalytics {
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        val hourlySales = generateHourlySales(startDate, endDate)
        val weeklyTrends = generateWeeklyTrends()
        val monthlyComparison = generateMonthlyComparison()
        
        return TimeAnalytics(
            hourlySales = hourlySales,
            weeklyTrends = weeklyTrends,
            monthlyComparison = monthlyComparison
        )
    }
    
    // Generate mock daily sales data
    private fun generateDailySales(days: Int): List<DailySales> {
        val calendar = Calendar.getInstance()
        val sales = mutableListOf<DailySales>()
        val random = Random()
        
        for (i in days downTo 1) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val date = calendar.time
            val salesAmount = 1000.0 + random.nextDouble() * 4000.0 // Random between 1000.0 and 5000.0
            val orders = 5 + random.nextInt(21) // Random between 5 and 25
            
            sales.add(DailySales(date, salesAmount, orders))
        }
        
        return sales.sortedBy { it.date }
    }
    
    // Generate hourly sales data from database
    private fun generateHourlySales(startDate: String, endDate: String): List<HourlySales> {
        val hourlySalesData = dbHelper.getHourlySales(startDate, endDate)
        
        // Fill in missing hours with zero values
        val hourlySalesMap = hourlySalesData.associate { 
            (it["hour"] as Int) to HourlySales(
                hour = it["hour"] as Int,
                sales = it["sales"] as Double,
                orders = it["orders"] as Int
            )
        }
        
        return (6..22).map { hour ->
            hourlySalesMap[hour] ?: HourlySales(hour, 0.0, 0)
        }
    }
    
    // Generate weekly trends from database
    private fun generateWeeklyTrends(): List<WeeklyTrend> {
        val weeklyTrendsData = dbHelper.getWeeklyTrends(4)
        
        return weeklyTrendsData.map { weekData ->
            val weekStart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(weekData["weekStart"] as String) ?: Date()
            val weekEnd = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(weekData["weekEnd"] as String) ?: Date()
            
            WeeklyTrend(
                weekStart = weekStart,
                weekEnd = weekEnd,
                totalSales = weekData["weeklySales"] as Double,
                totalOrders = weekData["weeklyOrders"] as Int,
                growthRate = 0.0 // TODO: Calculate growth rate from previous weeks
            )
        }.sortedBy { it.weekStart }
    }
    
    // Generate mock monthly comparison
    private fun generateMonthlyComparison(): List<MonthlyComparison> {
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val random = Random()
        
        return months.map { month ->
            val sales = 30000.0 + random.nextDouble() * 50000.0 // Random between 30000.0 and 80000.0
            val orders = 150 + random.nextInt(251) // Random between 150 and 400
            val previousYearSales = sales * (0.8 + random.nextDouble() * 0.4) // Random between 0.8 and 1.2
            val growthPercentage = ((sales - previousYearSales) / previousYearSales * 100).roundToInt().toDouble()
            
            MonthlyComparison(
                month = month,
                year = currentYear,
                sales = sales,
                orders = orders,
                previousYearSales = previousYearSales,
                growthPercentage = growthPercentage
            )
        }
    }
    
    // Get chart colors for consistent theming
    fun getChartColors(): List<Int> {
        return listOf(
            0xFF4CAF50.toInt(), // Green
            0xFF2196F3.toInt(), // Blue
            0xFFFF9800.toInt(), // Orange
            0xFFF44336.toInt(), // Red
            0xFF9C27B0.toInt(), // Purple
            0xFF00BCD4.toInt(), // Cyan
            0xFFFFEB3B.toInt(), // Yellow
            0xFF795548.toInt()  // Brown
        )
    }
    
    // Format currency
    fun formatCurrency(amount: Double): String {
        return "₱${String.format("%.2f", amount)}"
    }
    
    // Format percentage
    fun formatPercentage(value: Double): String {
        return "${String.format("%.1f", value)}%"
    }
    
    // Get current timestamp for last updated
    fun getCurrentTimestamp(): String {
        val now = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return timeFormat.format(now.time)
    }

    // Get date range for analytics (last 7 days)
    fun getLast7DaysDateRange(): Pair<String, String> {
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return Pair(startDate, endDate)
    }

    // Get date range for analytics (last 30 days)
    fun getLast30DaysDateRange(): Pair<String, String> {
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -29)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return Pair(startDate, endDate)
    }

    // Wrapper methods for database queries
    fun getSalesByDateRange(startDate: String, endDate: String): List<Map<String, Any>> {
        return dbHelper.getSalesByDateRange(startDate, endDate)
    }

    fun getTopSellingProducts(limit: Int = 5): List<Map<String, Any>> {
        return dbHelper.getTopSellingProducts(limit)
    }

    fun getSalesByCategory(startDate: String, endDate: String): List<Map<String, Any>> {
        return dbHelper.getSalesByCategory(startDate, endDate)
    }

    fun getLowStockSupplies(): List<Map<String, Any>> {
        return dbHelper.getLowStockSupplies()
    }
}
