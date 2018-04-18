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
import android.widget.Switch
import com.example.daza.soundmap.utils.AudioHelper
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast


class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    val TAG = LocationActivity::class.java.simpleName
    val DB_REFERENCE = FirebaseDatabase.getInstance().reference
    val GEOFIRE_REF = GeoFire(DB_REFERENCE)

    lateinit var elobutton: Button
    lateinit var narabutton: Switch
    lateinit var helper: AudioHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //helper = AudioHelper(this)
        //helper.recordAudio()

        elobutton = findViewById(R.id.elo_button)

        /*************************************************************************/
        /*GEOFIRE*/

        elobutton.setOnClickListener {

            val itemId = DB_REFERENCE.child("measurements").push().key
            DB_REFERENCE.child("measurements").child(itemId).child("noise").setValue(50)
            DB_REFERENCE.child("measurements").child(itemId).child("userID").setValue(50)

            val geofire = GeoFire(DB_REFERENCE.child("measurementLocation"))

            val firelistener = GeoFire.CompletionListener { key, error ->
                if (error != null) {
                    Log.e(TAG, "Error: $error. Location was not successfully saved on the server")

                } else {
                    Log.d(TAG, "Location was successfully saved on the server. Used key: $key")

                }
            }
            geofire.setLocation(itemId, GeoLocation(52.2207651, 21.0096579), firelistener)
        }

        //GET Z FIREBASE
//        val geoFire = GeoFire(FirebaseDatabase.getInstance().reference.child("measurementLocation"))
//
//        val geoQuery = geoFire.queryAtLocation(GeoLocation(-34.0, 151.0), 200.0)
//        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
//            override fun onGeoQueryReady() {
//                Log.i(TAG, "Geo Query: READY")
//                longToast("Ready")
//            }
//
//            override fun onKeyEntered(key: String?, location: GeoLocation?) {
//                Log.i(TAG, "Key: $key, location: (${location?.longitude},${location?.latitude}) is within range")
//            }
//
//            override fun onKeyMoved(key: String?, location: GeoLocation?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun onKeyExited(key: String?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun onGeoQueryError(error: DatabaseError?) {
//                Log.e(TAG, "Geo Query error: $error")
//            }
//
//        })


        /************************************************************************/
        /*GEOFIRE*/

        narabutton = findViewById(R.id.switch1)


        //val audioMeasureLiveData = ViewModelProviders.of(this).get(MeasurementViewModel::class.java).getAudioLevel(this)
        //audioMeasureLiveData.observe(this, Observer<Int> {  level -> Log.d(TAG, "Audio data: $level dB") })




        val firebaseViewModel = ViewModelProviders.of(this)
                .get(FirebaseQueryViewModel::class.java)

                val x = firebaseViewModel.getDataSnapshotLiveData()
                x.observe(this, Observer<DataSnapshot> { dataSnapshot ->
                    Log.d(TAG, "Data from firebase ${dataSnapshot?.child("noise")}")
                })

        ViewModelProviders.of(this)
                .get(LocationViewModel::class.java)
                .getLocation(this)
                .observe(this, Observer<Location> { location ->
                    firebaseViewModel.geoQuery.center = GeoLocation(location!!.latitude, location.longitude)
                    Log.d(TAG, "Latitude: ${location?.latitude} Longitude: ${location?.longitude}"

                    )
                    //audioMeasureLiveData.audioHelper.recordAudio(false)

                })

        narabutton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                firebaseViewModel.geoQuery.radius = 0.0
            }
            else{
                firebaseViewModel.geoQuery.radius = 200.0
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}
