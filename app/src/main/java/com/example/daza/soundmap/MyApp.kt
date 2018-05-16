package com.example.daza.soundmap

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.FirebaseApp

/**
 * Created by daza on 08.04.18.
 */
class MyApp: Application() {
    override fun onCreate(){
        super.onCreate()
        FirebaseApp.initializeApp(this)
        MapsInitializer.initialize(this)
    }
}