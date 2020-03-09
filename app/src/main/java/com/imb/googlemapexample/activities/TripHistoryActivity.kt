package com.imb.googlemapexample.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.imb.googlemapexample.R

class TripHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_history)

    }

    fun setToolbar(toolbar:androidx.appcompat.widget.Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
