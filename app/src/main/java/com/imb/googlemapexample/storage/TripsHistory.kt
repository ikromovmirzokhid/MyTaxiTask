package com.imb.googlemapexample.storage

import com.imb.googlemapexample.pojos.Trip

class TripsHistory {

    companion object {
        private val trip1 = Trip(
            "Sagbon 156 a block, Olmazor distri…", "Amir Temur Avenue 2a block 23 A", 41.3418855,
            69.2285395, 41.3259085, 69.2799308, "901234567"
        )
        private val trip2 = Trip(
            "Istiqbol 20 h, Mirabad dis.", "Sharaf Rashidov, prospect", 41.306748, 69.2820088,
            41.3194424, 69.2624884, "977654321"
        )
        private val trip3 = Trip(
            "Muqimiy 2a h, Chilanzar dis.", "Qumbuloq 13 h, Uchtepa dis.", 41.2907169, 69.2225652,
            41.3005974, 69.1755678, "941237654"
        )
        private val trip4 = Trip(
            "Shodlik 60 h, Uchtepa dis.", "Amir Temur Avenue 2a block 23 A", 41.3816188, 69.2645091,
            41.3259085, 69.2799308, "999876543"
        )
        private var allTrips = arrayListOf(trip1, trip2, trip3, trip4)
        fun getAllTrips(): List<Trip> {
            return allTrips
        }
    }
}