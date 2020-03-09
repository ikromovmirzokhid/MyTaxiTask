package com.imb.googlemapexample.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaceViewModel : ViewModel() {
    var place: MutableLiveData<String> = MutableLiveData()

    fun setName(placeName: String) {
        place.value = placeName
    }
}