package com.appdev.posheesh.Classes

import java.util.Date

// Data class for sales analytics
data class SalesAnalytics(
    val totalSales: Double,
    val totalOrders: Int,
    val averageOrderValue: Double,
    val topSellingProducts: List<ProductPerformance>,
    val salesByDate: List<DailySales>,
    val salesByCategory: List<CategorySales>
)

// Data class for product performance
data class ProductPerformance(
    val productName: String,
    val productCode: String,
    val quantitySold: Int,
    val revenue: Double,
    val percentageOfTotal: Double
)

// Data class for daily sales
data class DailySales(
    val date: Date,
    val sales: Double,
    val orders: Int
)

// Data class for category sales
data class CategorySales(
    val categoryName: String,
    val sales: Double,
    val percentage: Double
)

// Data class for inventory analytics
data class InventoryAnalytics(
    val totalProducts: Int,
    val lowStockItems: Int,
    val outOfStockItems: Int,
    val inventoryValue: Double,
    val categoryDistribution: List<CategoryInventory>,
    val stockLevels: List<StockLevel>
)

// Data class for category inventory
data class CategoryInventory(
    val categoryName: String,
    val productCount: Int,
    val totalValue: Double
)

// Data class for stock levels
data class StockLevel(
    val supplyName: String,
    val currentQuantity: Int,
    val criticalLevel: Int,
    val status: StockStatus
)

// Enum for stock status
enum class StockStatus {
    NORMAL,
    LOW_STOCK,
    OUT_OF_STOCK,
    OVERSTOCKED
}

// Data class for business KPIs
data class BusinessKPIs(
    val totalRevenue: Double,
    val totalOrders: Int,
    val averageOrderValue: Double,
    val customerCount: Int,
    val topPerformingDay: String,
    val bestSellingProduct: String
)

// Data class for time-based analytics
data class TimeAnalytics(
    val hourlySales: List<HourlySales>,
    val weeklyTrends: List<WeeklyTrend>,
    val monthlyComparison: List<MonthlyComparison>
)

// Data class for hourly sales
data class HourlySales(
    val hour: Int,
    val sales: Double,
    val orders: Int
)

// Data class for weekly trends
data class WeeklyTrend(
    val weekStart: Date,
    val weekEnd: Date,
    val totalSales: Double,
    val totalOrders: Int,
    val growthRate: Double
)

// Data class for monthly comparison
data class MonthlyComparison(
    val month: String,
    val year: Int,
    val sales: Double,
    val orders: Int,
    val previousYearSales: Double,
    val growthPercentage: Double
)
