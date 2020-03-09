package com.imb.googlemapexample.pojos

import com.google.gson.annotations.SerializedName

data class Route(@SerializedName("overview_polyline") val overviewPolyLine: OverviewPolyLine, val legs: List<Legs>)