package com.appdev.posheesh.Classes

import android.net.Uri

data class Products (
    val name: String,
    val description: String?,
    val categoryId: Int,
    val sellingPrice: Double,
    val imageUri: String,
    val code: String
    )