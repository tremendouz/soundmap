package com.example.daza.soundmap.data.livedata

import android.arch.lifecycle.LiveData
import android.util.Log
import com.example.daza.soundmap.data.models.RideModel
import com.google.firebase.database.*

/**
 * Created by daza on 31.05.18.
 */
class RidesLiveData(query: Query): LiveData<Pair<String, RideModel>>() {
    val TAG = RidesLiveData::class.java.simpleName
    val DB_REFERENCE = FirebaseDatabase.getInstance().reference
    val myQuery = query
    val listener = object : ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError?) {
            Log.e(TAG, "Can't listen to query: $myQuery. Exception: ${databaseError?.toException()}")

        }
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            for(data in dataSnapshot!!.children){
                val rideData = data.getValue(RideModel::class.java)
                val id = data.key
                value = Pair(id, rideData!!)
            }
            Log.i(TAG, "Got data: $dataSnapshot")
        }

    }
    override fun onActive() {
        myQuery.addValueEventListener(listener)
    }

    override fun onInactive() {
        myQuery.removeEventListener(listener)
    }
}