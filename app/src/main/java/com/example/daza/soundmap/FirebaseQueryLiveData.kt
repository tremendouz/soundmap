package com.example.daza.soundmap

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.database.*

/**
 * Created by daza on 08.04.18.
 */
class FirebaseQueryLiveData(query: Query? = null, ref: DatabaseReference? = null) : LiveData<DataSnapshot>() {
    val TAG = FirebaseQueryLiveData::class.java.simpleName
    val myQuery = query ?: ref

    val valueListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            value = dataSnapshot
        }

        override fun onCancelled(dataSnapshot: DatabaseError?) {
            Log.e(TAG, "Can't listen to query: $myQuery. Exception: ${dataSnapshot?.toException()}")
        }
    }

    override fun onActive() {
        myQuery?.addValueEventListener(valueListener)
    }

    override fun onInactive() {
        myQuery?.removeEventListener(valueListener)
    }
}