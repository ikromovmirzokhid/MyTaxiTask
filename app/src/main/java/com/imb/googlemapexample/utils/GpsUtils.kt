package com.imb.googlemapexample.utils

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.imb.googlemapexample.utils.Constants.Companion.GPS_REQUEST
// this class is checking gps status and inform about this, if gps is disabled request turning on the gps from user
class GpsUtils(var context: Context) {

    var mSettingsClient: SettingsClient
    var mLocationSettingsRequest: LocationSettingsRequest
    var locationManager: LocationManager
    var locationRequest: LocationRequest

    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mSettingsClient = LocationServices.getSettingsClient(context)
        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .interval = 10 * 1000
        locationRequest.fastestInterval = 2 * 1000
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        mLocationSettingsRequest = builder.build()
        builder.setAlwaysShow(true)
    }

    fun turnGPSOn(onGpsStatus: OnGpsStatusListener) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onGpsStatus.onGpsStatusListener(true)
        } else {
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(context as Activity) {
                    onGpsStatus.onGpsStatusListener(true)
                }
                .addOnFailureListener(context as Activity) {
                    val e = it as ApiException
                    val statusCode = e.statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            val rae = it as ResolvableApiException
                            rae.startResolutionForResult(context as Activity, GPS_REQUEST)
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMes = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings."
                            Toast.makeText(context, errorMes, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        }
    }

    interface OnGpsStatusListener {
        fun onGpsStatusListener(gpsStatus: Boolean)
    }

}