package com.imb.googlemapexample.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.imb.googlemapexample.R
import com.imb.googlemapexample.pojos.Trip

class TripListAdapter(private var trips: List<Trip>) :
    RecyclerView.Adapter<TripListAdapter.ViewHolder>() {

    private lateinit var listener: OnTripClickListener

    fun setOnTripClickListener(listener: OnTripClickListener) {
        this.listener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.trips_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = trips.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = trips[position]
        holder.startAddress.text = trip.startAddress
        holder.endAddress.text = trip.endAddress

        holder.itemView.setOnClickListener {
            val tripJson = Gson().toJson(trip)
            val bundle = Bundle()
            bundle.putString("tripInfo", tripJson)
            listener.notifyClick(bundle)
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var startAddress: TextView = itemView.findViewById(R.id.start_address)
        var endAddress: TextView = itemView.findViewById(R.id.end_address)

    }

    interface OnTripClickListener {
        fun notifyClick(bundle: Bundle)
    }
}