package com.imb.googlemapexample.pojos

data class Steps(
    val startLocation: Location,
    val endLocation: Location,
    val polyline: OverviewPolyLine
)