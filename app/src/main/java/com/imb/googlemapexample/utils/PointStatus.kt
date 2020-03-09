package com.imb.googlemapexample.utils

import com.google.android.gms.maps.model.LatLng
// this class determines whether requested position is in polygon or not
class PointStatus {
    companion object {
        fun isPointInPolygon(tap: LatLng, vertices: ArrayList<LatLng>): Boolean {
            var intersectCount = 0
            for (j in 0 until vertices.size - 1) {
                if (rayCastIntersect(
                        tap,
                        vertices[j],
                        vertices[j + 1]
                    )
                ) {
                    intersectCount++
                }
            }

            return (intersectCount % 2) == 1
        }

        // ray casting algorithm
        private fun rayCastIntersect(tap: LatLng, vertA: LatLng, vertB: LatLng): Boolean {
            val aY = vertA.latitude
            val bY = vertB.latitude
            val aX = vertA.longitude
            val bX = vertB.longitude
            val pY = tap.latitude
            val pX = tap.longitude

            if (aY > pY && bY > pY || aY < pY && bY < pY
                || aX < pX && bX < pX
            ) {
                return false // a and b can't both be above or below pt.y, and a or b must be east of pt.x
            }

            val m = (aY - bY) / (aX - bX) // Rise over run
            val bee = -aX * m + aY // y = mx + b
            val x = (pY - bee) / m // algebra is neat!

            return x > pX

        }

    }
}