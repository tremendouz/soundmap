package com.example.daza.soundmap.data.livedata

import android.arch.lifecycle.LiveData
import android.location.Location
import android.util.Log
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by daza on 08.04.18.
 */
class FirebaseQueryLiveData(query: GeoQuery?) : LiveData<Pair<DataSnapshot, GeoLocation>>() {
    val TAG = FirebaseQueryLiveData::class.java.simpleName
    val DB_REFERENCE = FirebaseDatabase.getInstance().reference
    val myQuery = query


//    val valueListener = object : ValueEventListener {
//        override fun onDataChange(dataSnapshot: DataSnapshot?) {
//            value = dataSnapshot
//            Log.i(TAG, "Got data from user: $dataSnapshot")
//        }
//
//        override fun onCancelled(dataSnapshot: DatabaseError?) {
//            Log.e(TAG, "Can't listen to query: $myQuery. Exception: ${dataSnapshot?.toException()}")
//        }
//    }

    val geoListener: GeoQueryEventListener = object : GeoQueryEventListener{
        override fun onGeoQueryReady() {
            Log.i(TAG, "Geo Query: READY")
        }

        override fun onKeyEntered(key: String?, location: GeoLocation?) {
            Log.i(TAG, "Key: $key, location: (${location?.longitude},${location?.latitude}) is within range")
            val dbReference = DB_REFERENCE.child("measurements").child(key)
            dbReference.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    value = Pair<DataSnapshot, GeoLocation>(first = dataSnapshot!!, second = location!!)
                    Log.i(TAG, "Got data from user: $dataSnapshot")
                }

                override fun onCancelled(dataSnapshot: DatabaseError?) {
                    Log.e(TAG, "Can't listen to query: $myQuery. Exception: ${dataSnapshot?.toException()}")
                }
            })
        }

        override fun onKeyMoved(key: String?, location: GeoLocation?) {
            Log.i(TAG, "Key moved: $key")
        }

        override fun onKeyExited(key: String?) {
            Log.i(TAG, "Key exited: $key")
        }

        override fun onGeoQueryError(error: DatabaseError?) {
            Log.e(TAG, "GeoQueryError: $error")
        }

    }

    override fun onActive() {
        myQuery?.addGeoQueryEventListener(geoListener)
    }

    override fun onInactive() {
        myQuery?.removeAllListeners()
    }
}