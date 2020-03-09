package com.imb.googlemapexample.utils

import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.LatLng

// this class converts position to position's place name
class Place {
    companion object {
        fun getPlaceNameFromPosition(latLng: LatLng, geocoder: Geocoder): String {

            val LATITUDE: Double = latLng.latitude
            val LONGITUDE: Double = latLng.longitude

            var strAdd = ""
            var name = ""

            try {
                val addresses = geocoder.getFromLocation(
                    LATITUDE,
                    LONGITUDE, 1
                )

                if (addresses != null) {

                    val returnedAddress = addresses[0]

                    val strReturnedAddress = StringBuilder()
                    strAdd = returnedAddress.getAddressLine(0)
                    for (i in 0 until strAdd.length) {
                        if (strAdd[i] == ',')
                            break
                        strReturnedAddress.append(strAdd[i])
                    }

                    name = strReturnedAddress.toString()

                    Log.d(
                        "CurrentLocationAddress",
                        "" + strReturnedAddress.toString()
                    )
                } else {
                    Log.d("CurrentLocationAddress", "No Address returned!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("CurrentLocationAddress", "Cannot get Address! {${e.message}}")
            }

            return name
        }
    }
}