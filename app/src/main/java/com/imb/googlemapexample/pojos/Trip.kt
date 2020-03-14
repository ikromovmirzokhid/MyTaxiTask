package com.imb.googlemapexample.pojos

data class Trip(
    val startAddress: String,
    val endAddress: String,
    val latA: Double,
    val lngA: Double,
    val latB: Double,
    val lngB: Double,
    val phoneNum: String
)