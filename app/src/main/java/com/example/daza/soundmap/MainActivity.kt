package com.example.daza.soundmap

import android.Manifest
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var simpleText: TextView
    lateinit var logoutButton: Button
    lateinit var firebaseAuth: FirebaseAuth

    val REQUEST_FINE_LOCATION_CODE = 0
    val REQUEST_AUDIO_RECORD_CODE = 1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO Fix checking permissions
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        REQUEST_FINE_LOCATION_CODE)
        //requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),
        //        REQUEST_AUDIO_RECORD_CODE)


        firebaseAuth = FirebaseAuth.getInstance()


        simpleText = findViewById(R.id.simple_text)
        simpleText.text = firebaseAuth.currentUser?.uid

        logoutButton = findViewById(R.id.button_logout)
        logoutButton.setOnClickListener {
            //firebaseAuth.signOut()
            val intent = Intent(this@MainActivity, LocationActivity::class.java)
            startActivity(intent)
        }

    }
}

