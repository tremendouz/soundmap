package com.example.daza.soundmap

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by daza on 08.04.18.
 */
class FirebaseQueryViewModel: ViewModel() {
    val DB_REFERENCE : GeoFire = GeoFire(FirebaseDatabase.getInstance().getReference("measurementLocation"))
    val geoQuery = DB_REFERENCE.queryAtLocation(GeoLocation(-34.0, 151.0), 200.0)
    val liveData: FirebaseQueryLiveData = FirebaseQueryLiveData(geoQuery)

    fun getDataSnapshotLiveData(): LiveData<DataSnapshot> {
        return liveData
    }
}