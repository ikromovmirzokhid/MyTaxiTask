package com.imb.googlemapexample.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.imb.googlemapexample.R
import com.imb.googlemapexample.activities.TripHistoryActivity
import com.imb.googlemapexample.adapters.TripListAdapter
import com.imb.googlemapexample.pojos.Trip
import com.imb.googlemapexample.storage.TripsHistory
import kotlinx.android.synthetic.main.fragment_trip_history.*


class TripHistoryFragment : Fragment(), TripListAdapter.OnTripClickListener {
    private lateinit var adapter: TripListAdapter
    private var allTrips = ArrayList<Trip>()
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trip_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as TripHistoryActivity).setToolbar(toolbar)

        navController = Navigation.findNavController(view)

        for (i in 0..2)
            allTrips.addAll(
                TripsHistory.getAllTrips()
            )
        adapter = TripListAdapter(allTrips)
        adapter.setOnTripClickListener(this)

        rView.layoutManager = LinearLayoutManager(activity)
        rView.adapter = adapter
    }

    override fun notifyClick(bundle: Bundle) {
        navController.navigate(R.id.action_tripHistoryFragment_to_tripMapFragment, bundle)
    }
}
