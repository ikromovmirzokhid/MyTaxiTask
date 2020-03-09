package com.imb.googlemapexample.utils

import com.google.android.gms.maps.model.LatLng

class VerticesBuilder {
    companion object {
        fun createVertices(
            tashkentLat: Array<String>,
            tashkentLng: Array<String>
        ): ArrayList<LatLng> {
            var latLng: LatLng
            val verticesDemo = ArrayList<LatLng>()
            for (i in 0 until tashkentLat.size) {
                latLng = LatLng(tashkentLat[i].toDouble(), tashkentLng[i].toDouble())
                verticesDemo.add(latLng)
            }

            return verticesDemo
        }
    }

}