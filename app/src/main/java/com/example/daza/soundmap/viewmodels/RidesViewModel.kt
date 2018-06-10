package com.example.daza.soundmap.viewmodels

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.example.daza.soundmap.data.livedata.RidesLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by daza on 06.06.18.
 */
class RidesViewModel: ViewModel() {
    var ridesLiveData: RidesLiveData? = null
    val ID by lazy { FirebaseAuth.getInstance().currentUser?.uid }
    val REF = "/rides/$ID"
    val QUERY = FirebaseDatabase.getInstance().getReference(REF)

    fun getRidesData(): RidesLiveData =
            ridesLiveData ?: synchronized(this) {
                ridesLiveData ?: RidesLiveData(QUERY).also {
                    ridesLiveData = it }
            }
}