package com.appdev.posheesh.Classes

data class Products (
    val id: Int,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val categoryId: Int,
    val quantity: Int,
    val sellingPrice: Double,
    val buyingPrice: Double,
    val imageUrl: Int,
    val code: String
    )