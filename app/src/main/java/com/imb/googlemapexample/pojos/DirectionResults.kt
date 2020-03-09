package com.imb.googlemapexample.pojos

import com.google.gson.annotations.SerializedName

// It is main model for getting results from routes api. Other models such as Legs, Location, Steps,
// OverviewPolyline, Route are the part of this model class
data class DirectionResults(@SerializedName("routes") val routes: List<Route>)