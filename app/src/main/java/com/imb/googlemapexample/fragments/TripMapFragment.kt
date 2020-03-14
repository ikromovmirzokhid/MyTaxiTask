package com.imb.googlemapexample.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.imb.googlemapexample.R
import com.imb.googlemapexample.api.RouteApi
import com.imb.googlemapexample.pojos.DirectionResults
import com.imb.googlemapexample.pojos.Route
import com.imb.googlemapexample.pojos.Steps
import com.imb.googlemapexample.pojos.Trip
import com.imb.googlemapexample.utils.Constants
import com.imb.googlemapexample.utils.RouteDecode
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.trip_bottom_sheet.*
import kotlinx.android.synthetic.main.trip_map_fragment.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class TripMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var startPos: LatLng
    private lateinit var endPos: LatLng
    private lateinit var retrofit: Retrofit
    private lateinit var api: RouteApi
    private lateinit var cD: CompositeDisposable

    private val COLOR_BLUE_ARGB = Color.BLUE
    private val POLYLINE_STROKE_WIDTH_PX = 8f

    private lateinit var tripDescBehavior: BottomSheetBehavior<View>

    private lateinit var trip: Trip

    private lateinit var navController: NavController

    private lateinit var dialIntent: Intent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialization of Route API through Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        api = retrofit.create(RouteApi::class.java)
        cD = CompositeDisposable()

        val tripJson = arguments!!.getString("tripInfo")
        if (!tripJson.equals("")) {
            trip = Gson().fromJson(tripJson, Trip::class.java)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.trip_map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        tripStartDes.text = trip.startAddress
        tripEndDes.text = trip.endAddress

        navController = Navigation.findNavController(view)

        //installation of bottom sheet
        tripDescBehavior = BottomSheetBehavior.from(tripBottomSheet)
        tripDescBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bg.visibility = View.VISIBLE
                bg.alpha = slideOffset
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    bg.visibility = View.GONE
            }

        })

        btnBack.setOnClickListener {
            navController.popBackStack()
        }

        btnCall.setOnClickListener {
            dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + trip.phoneNum))
            startActivity(dialIntent)
        }

    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {

            googleMap = p0
            startPos = LatLng(trip.latA, trip.lngA)
            endPos = LatLng(trip.latB, trip.lngB)

            googleMap.addMarker(
                MarkerOptions().position(startPos).title("Start Point")
                    .icon(BitmapDescriptorFactory.fromBitmap(drawBitmap("startIcon")))
            )


            googleMap.addMarker(
                MarkerOptions().position(endPos).title("End Point")
                    .icon(BitmapDescriptorFactory.fromBitmap(drawBitmap("endPoint")))
                    .anchor(0.5f, 0.3f)
            )

            getRoute(startPos, endPos)

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 13f))
        }
    }

    private fun drawBitmap(typeOfBitmap: String): Bitmap? {
        if (typeOfBitmap.equals("startIcon"))
            return BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_pin_start
            )
        else if (typeOfBitmap.equals("endPoint"))
            return BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_pin_end
            )
        return null
    }

    // this function draws route of 2 location

    private fun getRoute(startPos: LatLng, endPos: LatLng) {
        // preparing parameters for routes api
        val origin = "${startPos.latitude}, ${startPos.longitude}"
        val destination = "${endPos.latitude}, ${endPos.longitude}"
        val mode = "driving"
        // getting json from api and parsing it to list of positions
        api.getDirection(origin, destination, mode, getString(R.string.google_maps_api_key))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DirectionResults> {
                override fun onComplete() {
                    cD.clear()
                }

                override fun onSubscribe(d: Disposable) {
                    cD.addAll(d)
                }

                override fun onNext(t: DirectionResults) {
                    if (t == null)
                        return
                    else {
                        val routelist = ArrayList<LatLng>()
                        routelist.add(startPos)
                        // getting routes from directionResults then legs from routes then steps from legs and finally points from steps
                        // and decoding this points in order to get positions list
                        if (t.routes.isNotEmpty()) run {
                            var decodeList: List<LatLng>
                            val routeA: Route = t.routes[0]
                            if (routeA.legs.isNotEmpty()) {
                                for (j in 0 until routeA.legs.size) {
                                    val steps = routeA.legs[j].steps
                                    var step: Steps
                                    var polyline: String
                                    for (k in 0 until steps.size) {
                                        step = steps[k]
                                        polyline = step.polyline.points
                                        decodeList = RouteDecode.decodePoly(polyline)
                                        routelist.addAll(decodeList)
                                    }
                                }
                            }
                        }

                        if (routelist.isNotEmpty()) {
                            // after getting all positions we can draw proper polyline between 2 locations
                            routelist.add(endPos)
                            val rectLine = PolylineOptions().width(POLYLINE_STROKE_WIDTH_PX)
                                .color(activity!!.resources.getColor(R.color.polyLineColor))
                            for (i in 0 until routelist.size) {
                                rectLine.add(routelist[i])
                            }
                            googleMap.addPolyline(rectLine)

                        }

                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

            })
    }
}
