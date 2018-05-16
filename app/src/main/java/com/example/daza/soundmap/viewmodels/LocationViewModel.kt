package com.example.daza.soundmap.viewmodels

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.example.daza.soundmap.data.livedata.LocationLiveData

/**
 * Created by daza on 08.04.18.
 */
class LocationViewModel : ViewModel() {
    var locationData: LocationLiveData? = null

    fun getLocation(context: Context): LocationLiveData =
            locationData ?: synchronized(this) {
                locationData ?: LocationLiveData(context).also {
                    locationData = it }
            }

}