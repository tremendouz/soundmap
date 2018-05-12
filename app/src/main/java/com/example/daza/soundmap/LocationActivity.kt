package com.example.daza.soundmap

import android.arch.lifecycle.LiveDataReactiveStreams
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
import android.arch.lifecycle.ViewModel
import android.graphics.Color
import android.location.Geocoder
import android.provider.ContactsContract
import android.widget.Switch
import android.widget.TextView
import com.example.daza.soundmap.utils.AudioHelper
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.PolylineOptions
import io.reactivex.Flowable
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.internal.operators.flowable.FlowableWithLatestFrom
import io.reactivex.internal.operators.observable.ObservableWithLatestFrom

import org.reactivestreams.Publisher


class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    val TAG = LocationActivity::class.java.simpleName


    lateinit var elobutton: Button
    lateinit var narabutton: Switch
    lateinit var helper: AudioHelper
    lateinit var circleTextView: TextView

    val geocoder by lazy { Geocoder(this) }

    val polylineOptions by lazy {
        PolylineOptions()
                .width(3.0F)
                .color(Color.RED)
    }

    var isMapReady: Boolean = false

    lateinit var mapFragment: SupportMapFragment

    val mapPixelWidth by lazy { mapFragment.view!!.measuredWidth.toDouble() }
    val mapPixelHeight by lazy { mapFragment.view!!.measuredHeight.toDouble() }


    val firebaseViewModel by lazy {
        ViewModelProviders.of(this)
                .get(FirebaseQueryViewModel::class.java)
    }
    val locationViewModel by lazy {
        ViewModelProviders.of(this)
                .get(LocationViewModel::class.java)
    }

    @Override


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //helper = AudioHelper(this)
        //helper.recordAudio()

        elobutton = findViewById(R.id.elo_button)
        narabutton = findViewById(R.id.switch1)
        circleTextView = findViewById(R.id.circle_text)
        circleTextView.text = "Licznik"

        /*************************************************************************/
        /*GEOFIRE*/

        elobutton.setOnClickListener {
            //firebaseViewModel.pushData(GeoLocation(52.2207651, 21.0096579), 50)
        }


        val audioMeasureLiveData = ViewModelProviders.of(this).get(MeasurementViewModel::class.java).getAudioLevel(this)


        val fireBaseLiveData = firebaseViewModel.getDataSnapshotLiveData()
        fireBaseLiveData.observe(this, Observer<DataSnapshot> { dataSnapshot ->
            Log.d(TAG, "QUERY RADIUS: ${firebaseViewModel.geoQuery.radius}")
            Log.d(TAG, "QUERY CENTER: ${firebaseViewModel.geoQuery.center} ")
            Log.d(TAG, "LIVE DATA FROM WEB: Data from firebase ${dataSnapshot?.child("noise")}")
        })


        val locationLiveData = locationViewModel.getLocation(this)
//        locationLiveData.observe(this, Observer<Location> { location ->
//            //Log.i(TAG, "MAP READY: $isMapReady")
//            firebaseViewModel.geoQuery.center = GeoLocation(location!!.latitude, location.longitude)
//            Log.d(TAG, "Latitude: ${location.latitude} Longitude: ${location.longitude}"
//            )
//        })


        narabutton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                firebaseViewModel.geoQuery.radius = 0.0
            } else {
                firebaseViewModel.geoQuery.radius = 0.1
            }
        }


        val rxDataPublisher: Publisher<Int> = LiveDataReactiveStreams.toPublisher(
                this, audioMeasureLiveData)

        val rxLocationPublisher: Publisher<Location> = LiveDataReactiveStreams.toPublisher(
                this, locationLiveData)

        val rxFlowable: Flowable<Pair<Location, Int>> = Flowable.fromPublisher(rxLocationPublisher)
                .withLatestFrom(
                        rxDataPublisher, BiFunction { location, integer -> Pair(location, integer) })

        val flowableToLiveData = LiveDataReactiveStreams.fromPublisher(rxFlowable)

        flowableToLiveData.observe(this, Observer { pair ->
            val location = pair?.first

            if (location != null) {
                runOnUiThread {
                    val locationName = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val addressLine = locationName[0].getAddressLine(0)
                   circleTextView.text = addressLine
                }
            }

            firebaseViewModel.geoQuery.radius = 0.0
            if (pair != null) {
                firebaseViewModel.geoQuery.center = GeoLocation(pair.first.latitude, pair.first.longitude)
            }
            firebaseViewModel.pushData(pair!!.first, pair.second)
            Log.i("RXJAVA <-> LIVEDATA",
                    " Location: ${pair?.first.latitude} {${pair.first.longitude}} Noise: ${pair?.second} dB")

            //polylineOptions.add(LatLng(pair.first.latitude, pair.first.longitude))
            //mMap.addPolyline(polylineOptions)
            // change query radius to 50 m
            //Log.i("INFO", "ZMNIEJSZAM RADIUS")
            //firebaseViewModel.geoQuery.radius = 0.05
            mMap.addMarker(MarkerOptions().position(LatLng(pair.first.latitude, pair.first.longitude)))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(pair.first.latitude, pair.first.longitude)))

            firebaseViewModel.geoQuery.radius = 0.1
            //Log.i("INFO", "ZWiekszam RADIUS")
            //firebaseViewModel.geoQuery.radius = 1.0
        })

    }


    fun changeGeoQueryRadius(zoomLevel: Float) {
        // TODO set man max zoom level ?
        // its unnecessary to show the whole world
        if (zoomLevel > 18) {
            firebaseViewModel.geoQuery.radius = 0.0
        } else {
            firebaseViewModel.geoQuery.radius = 200.0
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        // TODO kinda dont care about map loading coz we can add button to start measurements
        // move all live data code to this method ???
        isMapReady = true


        mMap.setOnCameraIdleListener {
            val zoomLevel = mMap.cameraPosition.zoom
            //Log.i(TAG, "Map ZOOM: $zoomLevel")
            val metersPerPx = 156543.03392 * Math.cos(mMap.cameraPosition.target.latitude * Math.PI / 180) / Math.pow(2.0, zoomLevel.toDouble())

            // TODO find out why we need to divide by 3 to get correct result
            val kilometersPerPixel = 0.33 * metersPerPx / 1000
            val queryRadius = 0.5 * Math.sqrt(mapPixelHeight * mapPixelHeight + mapPixelWidth * mapPixelWidth) * kilometersPerPixel
            //Log.i(TAG, "Query radius $queryRadius KM BUT REAL ${firebaseViewModel.geoQuery.radius}")
            //changeGeoQueryRadius(zoomLevel)
        }
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}
