package com.imb.googlemapexample.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.imb.googlemapexample.R
import com.imb.googlemapexample.dialogs.RegionNotSupportedDialog
import com.imb.googlemapexample.utils.*
import com.imb.googlemapexample.viewmodels.PlaceViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.supported_regions_bottom_sheet.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var geocoder: Geocoder
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isPermissionGranted = false
    private var isGpsOn = false
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var wayLatitude = 0.0
    private var wayLongtitude = 0.0
    private val UPDATE_INTERVAL = (10 * 1000).toLong()
    private val FASTEST_INTERVAL: Long = 2000
    private var currentPos: LatLng? = null

    private lateinit var tashkentLat: Array<String>
    private lateinit var tashkentLng: Array<String>
    private lateinit var vertices: ArrayList<LatLng>

    private val COLOR_RED_ARGB = Color.RED
    private val POLYGON_STROKE_WIDTH_PX = 5f

    private lateinit var mPlaceViewModel: PlaceViewModel

    private lateinit var regionSupportedBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var regionNotSupportedDialog: RegionNotSupportedDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // taking tashkent's lat and lng in order to draw polygon on the map
        tashkentLat = resources.getStringArray(R.array.tashkent_lat)
        tashkentLng = resources.getStringArray(R.array.tashkent_lng)
        vertices = VerticesBuilder.createVertices(tashkentLat, tashkentLng)

        // initialization of request of current location
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
        }

        // updated location will come to locationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                onLocationChanged(locationResult!!.lastLocation)
            }
        }

        // initialization of geoCoder for identify place names from place position
        geocoder = Geocoder(this, Locale.ENGLISH)

        // checking whether location access permission is granted or not
        checkPermissionGranted()

        //check whether gps on or not. If it is turned off then ask from user to turn on it
        checkGpsStatus()

        //installation of bottom sheet and bottom sheet dialog
        regionSupportedBottomSheetBehavior = BottomSheetBehavior.from(supportedRegionBottomSheet)
        regionNotSupportedDialog = RegionNotSupportedDialog()

        mPlaceViewModel = ViewModelProviders.of(this).get(PlaceViewModel::class.java)
        mPlaceViewModel.place.observe(this,
            androidx.lifecycle.Observer<String> {
                placeName.text = it
            })

        locationBtn.setOnClickListener {
            mFusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }

        navigationToggle.setOnClickListener {
            dLayout.openDrawer(Gravity.LEFT)
        }

        //getting map view on the screen
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getDeviceLocation()

        nView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.trips) {
                startActivity(Intent(this, TripHistoryActivity::class.java))
                dLayout.closeDrawers()
            }
            return@setNavigationItemSelectedListener true
        }

    }

    override fun onMapReady(p0: GoogleMap?) {

        if (p0 != null) {

            googleMap = p0
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(41.311081, 69.240562),
                    10f
                )
            )
            // if permission granted enabling my location
            if (isPermissionGranted) {
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = false
            }
            // drawing tashkent territory polygon
            drawPolygon(vertices)

            // tracking camera in order to determine marker's position
            googleMap.setOnCameraMoveStartedListener {
                progressBar.visibility = View.VISIBLE
            }

            googleMap.setOnCameraMoveListener {
                progressBar.visibility = View.VISIBLE
            }

            googleMap.setOnCameraMoveCanceledListener {
                progressBar.visibility = View.GONE
            }

            googleMap.setOnCameraIdleListener {
                // finding center position of camera because of marker is always in that point
                val latLng = googleMap.cameraPosition.target
                // Toast.makeText(activity, "working"+latLng.latitude+", "+latLng.longitude, Toast.LENGTH_SHORT).show()
                if (PointStatus.isPointInPolygon(latLng, vertices)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // determining markers place name through it's position
                        Observable.just(latLng).map { t ->
                            Place.getPlaceNameFromPosition(
                                t,
                                geocoder
                            )
                        }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                if (regionSupportedBottomSheetBehavior.isHideable) {
                                    regionSupportedBottomSheetBehavior.isHideable = false
                                    regionSupportedBottomSheetBehavior.state =
                                        BottomSheetBehavior.STATE_EXPANDED
                                }
                                mPlaceViewModel.setName(it)
                            }
                    }
                } else if (currentPos != null) {
                    regionSupportedBottomSheetBehavior.isHideable = true
                    regionSupportedBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    mPlaceViewModel.setName("Region is not supported")
                    regionNotSupportedDialog.show(
                        supportFragmentManager,
                        RegionNotSupportedDialog::class.java.name
                    )
                }
                progressBar.visibility = View.GONE
            }

        }

    }

    // this function checks permission for devices's location is granted or not
    private fun checkPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                Constants.locationRequestCode
            )
        } else {
            isPermissionGranted = true
        }
    }

    // this function is callback of GpsStatus. Determines whether gps is on or not if gps is off will be asked from user to enable it
    private fun checkGpsStatus() {
        GpsUtils(this)
            .turnGPSOn(object : GpsUtils.OnGpsStatusListener {
                override fun onGpsStatusListener(gpsStatus: Boolean) {
                    isGpsOn = gpsStatus
                }
            })
    }

    // updates device's current location
    private fun onLocationChanged(location: Location) {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)

        wayLatitude = location.latitude
        wayLongtitude = location.longitude

        // saving device's current position
        currentPos = LatLng(wayLatitude, wayLongtitude)

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 15f))
    }

    // this function gets device's current location if permission is granted and
    private fun getDeviceLocation() {
        if (isPermissionGranted || isGpsOn) {
            mFusedLocationProviderClient.lastLocation.addOnSuccessListener {
                // if location is came successfully then moving camera to that position
                if (it != null) {
                    wayLatitude = it.latitude
                    wayLongtitude = it.longitude
                    currentPos = LatLng(wayLatitude, wayLongtitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 15f))
                }
                // if current location is null updating location through locationRequest
                else {
                    mFusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                }
            }
        }
    }


    // this function draws polygon from list of positions
    private fun drawPolygon(vertices: ArrayList<LatLng>) {
        googleMap.addPolygon(
            PolygonOptions()
                .strokeColor(COLOR_RED_ARGB)
                .strokeWidth(POLYGON_STROKE_WIDTH_PX)
                .addAll(vertices)
        )
    }

    // result of request of permission about device's current location comes to this function
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 ->
                // if permission is granted then checking gps status and if gps is on identifying device's location
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = true
                    checkGpsStatus()
                    getDeviceLocation()
                } else {
                    isPermissionGranted = false
                    //locationBtn.visibility = View.GONE
                    Toast.makeText(this, "Permission is denied", Toast.LENGTH_SHORT).show()
                }
        }
    }

    //if gps is off, turning gps on will be asked from user if he/she accepts response comes here
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // if user turned gps on then determines user's location
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.GPS_REQUEST) {
                isGpsOn = true
                getDeviceLocation()
            }
        }
        // if rejects turning on of gps, will be asked enable gps until user does
        else {
            checkGpsStatus()
            getDeviceLocation()
        }
    }

}


