package com.imb.googlemapexample.api

import com.imb.googlemapexample.pojos.DirectionResults
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface RouteApi {

    @GET("maps/api/directions/json")
    fun getDirection(
        @Query("origin") origin: String, @Query("destination") destination: String,
        @Query("mode") mode: String, @Query("key") apiKey: String
    ): Observable<DirectionResults>

}