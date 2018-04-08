package com.example.daza.soundmap

import android.arch.lifecycle.ViewModelProviders
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import android.arch.lifecycle.Observer


class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    val TAG = LocationActivity::class.java.simpleName
    val DB_REFERENCE = FirebaseDatabase.getInstance().reference
    val GEOFIRE_REF = GeoFire(DB_REFERENCE)

    lateinit var elobutton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        elobutton = findViewById(R.id.elo_button)
        elobutton.setOnClickListener {

            val itemId = DB_REFERENCE.child("measurements").push().key
            DB_REFERENCE.child("measurements").child(itemId).child("noise").setValue(50)
            DB_REFERENCE.child("measurements").child(itemId).child("userID").setValue(50)

            val geofire = GeoFire(DB_REFERENCE.child("measurementLocation"))
            val firelistener = object : GeoFire.CompletionListener{
                override fun onComplete(key: String?, error: DatabaseError?) {
                    Log.d("ELO", "ELO 420 wysylamy geo FIRE")
                }
            }
            geofire.setLocation(itemId, GeoLocation(1.0, 2.0), firelistener)
        }


        ViewModelProviders.of(this)
                .get(LocationViewModel::class.java)
                .getLocation(this)
                .observe(this, Observer<Location> { location ->
                    Log.d(TAG, "Latitude: ${location?.latitude} Longitude: ${location?.longitude}")
                })

        ViewModelProviders.of(this)
                .get(FirebaseQueryViewModel::class.java)
                .getDataSnapshotLiveData()
                .observe(this, Observer<DataSnapshot> {
                    dataSnapshot ->  Log.d(TAG, "Data from firebase $dataSnapshot")
                })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
