package com.example.daza.soundmap.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import android.util.Log
import com.example.daza.soundmap.data.livedata.FirebaseQueryLiveData
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by daza on 08.04.18.
 */
class FirebaseQueryViewModel : ViewModel() {
    val TAG = FirebaseQueryViewModel::class.java.simpleName
    val DB_REFERENCE: GeoFire = GeoFire(FirebaseDatabase.getInstance().getReference("measurementLocation"))
    var fireRadius = 7.72
    var fireCentrum = GeoLocation(52.1518944, 21.0288875)
    val geoQuery = DB_REFERENCE.queryAtLocation(fireCentrum, fireRadius)

    val liveData: FirebaseQueryLiveData = FirebaseQueryLiveData(geoQuery)
    val firebaseAuthInstance by lazy { FirebaseAuth.getInstance() }

    val listOfKeys by lazy { arrayListOf<String>() }

    fun getDataSnapshotLiveData(): LiveData<Pair<DataSnapshot, GeoLocation>> {
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

    //   upload to db
    fun pushData(location: Location, noise: Int) {
        val itemId = PushDataHelper.DB_REFERENCE.child("measurements").push().key
        listOfKeys.add(itemId)
        PushDataHelper.DB_REFERENCE.child("measurements").child(itemId).child("noise").setValue(noise)
        PushDataHelper.DB_REFERENCE.child("measurements").child(itemId).child("userID").setValue(firebaseAuthInstance.currentUser?.uid)
        PushDataHelper.LOCATION_REF.setLocation(itemId, GeoLocation(location.latitude, location.longitude), PushDataHelper.firelistener)

    }

    fun clearData() {
        val keys = listOfKeys
        if (keys.isNotEmpty()) {
            for (itemId in keys) {
                PushDataHelper.DB_REFERENCE.child("measurements").child(itemId).removeValue()
                PushDataHelper.LOCATION_REF.databaseReference.child("measurementLocation").child(itemId).removeValue()
            }
            listOfKeys.clear()
        }
    }
}


