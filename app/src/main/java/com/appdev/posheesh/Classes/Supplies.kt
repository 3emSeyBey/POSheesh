package com.appdev.posheesh.Classes


data class Supplies(
    val supplyID: Int,
    val supplyName: String,
    val supplyDescription: String,
    val imgUrl: String,
    var supplyQuantity: Int,
    val supplyUnit: String,
    val crticalLevel: Int,
    val isActive: Int,
    val creationDate: String
)