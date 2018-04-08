package com.example.daza.soundmap

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by daza on 08.04.18.
 */
class FirebaseQueryViewModel: ViewModel() {
    val DB_REFERENCE : DatabaseReference = FirebaseDatabase.getInstance().getReference("user")
    val liveData: FirebaseQueryLiveData = FirebaseQueryLiveData(DB_REFERENCE)

    fun getDataSnapshotLiveData(): LiveData<DataSnapshot> {
        return liveData
    }
}