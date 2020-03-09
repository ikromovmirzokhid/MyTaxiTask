package com.imb.googlemapexample.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.imb.googlemapexample.R
import com.imb.googlemapexample.adapters.TripListAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        nView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.trips) {
                navController.navigate(R.id.action_mainMapFragment_to_tripHistoryActivity)
                dLayout.closeDrawers()
            }
            return@setNavigationItemSelectedListener true
        }

    }
}


