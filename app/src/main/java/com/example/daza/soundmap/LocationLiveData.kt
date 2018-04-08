package com.example.daza.soundmap

import android.arch.lifecycle.LiveData
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*

/**
 * Created by daza on 08.04.18.
 */
class LocationLiveData(val context: Context) : LiveData<Location>() {
    val LOCATION_REQUEST_INTERVAL: Long = 5000
    val LOCATION_REQUEST_FASTEST_INTERVAL: Long = 2000

    lateinit var fusedLocationClient: FusedLocationProviderClient
    val locationRequest by lazy { LocationRequest.create() }


    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val newLocation = locationResult.lastLocation
            value = newLocation
        }
    }


    @SuppressWarnings("MissingPermission")
    override fun onActive() {
        super.onActive()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        setupLocationRequest()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }


    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    fun setupLocationRequest() {
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.fastestInterval = LOCATION_REQUEST_FASTEST_INTERVAL
        locationRequest.interval = LOCATION_REQUEST_INTERVAL
    }
}