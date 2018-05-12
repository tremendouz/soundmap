package com.example.daza.soundmap

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import android.util.Log
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by daza on 08.04.18.
 */
class FirebaseQueryViewModel : ViewModel() {
    val TAG = FirebaseQueryViewModel::class.java.simpleName
    val DB_REFERENCE: GeoFire = GeoFire(FirebaseDatabase.getInstance().getReference("measurementLocation"))
    val geoQuery = DB_REFERENCE.queryAtLocation(GeoLocation(-34.0, 151.0), 0.1)
    val liveData: FirebaseQueryLiveData = FirebaseQueryLiveData(geoQuery)

    fun getDataSnapshotLiveData(): LiveData<DataSnapshot> {
        return liveData
    }

    object PushDataHelper {
        val TAG = FirebaseQueryViewModel::class.java.simpleName
        val DB_REFERENCE = FirebaseDatabase.getInstance().reference
        val MEASUREMENT_REF = GeoFire(DB_REFERENCE.child("measurements"))
        val LOCATION_REF = GeoFire(DB_REFERENCE.child("measurementLocation"))

        val firelistener = GeoFire.CompletionListener { key, error ->
            if (error != null) {
                Log.e(TAG, "Error: $error. Location was NOT SAVED on the server")
            } else {
                Log.d(TAG, "Location was successfully SAVED on the server. Used KEY: $key")
            }
        }
    }

    fun pushData(location: Location, noise: Int) {
        val itemId = PushDataHelper.DB_REFERENCE.child("measurements").push().key
        PushDataHelper.DB_REFERENCE.child("measurements").child(itemId).child("noise").setValue(noise)
        PushDataHelper.DB_REFERENCE.child("measurements").child(itemId).child("userID").setValue(50)
        PushDataHelper.LOCATION_REF.setLocation(itemId, GeoLocation(location.latitude, location.longitude), PushDataHelper.firelistener)

    }
}


