package com.appdev.posheesh.Classes

data class Products (
    val name: String,
    val description: String?,
    val categoryId: Int,
    val sellingPrice: Double,
    val imageUri: String,
    val code: String
    )