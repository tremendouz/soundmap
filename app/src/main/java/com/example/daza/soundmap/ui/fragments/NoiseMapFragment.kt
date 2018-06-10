package com.example.daza.soundmap.ui.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.daza.soundmap.R
import com.example.daza.soundmap.data.models.RideModel
import com.example.daza.soundmap.data.services.GoogleDirectionsService
import com.example.daza.soundmap.viewmodels.FirebaseQueryViewModel
import com.example.daza.soundmap.viewmodels.LocationViewModel
import com.example.daza.soundmap.viewmodels.MeasurementViewModel
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.PolyUtil
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Publisher


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [NoiseMapFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [NoiseMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NoiseMapFragment : Fragment(), OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    val TAG = NoiseMapFragment::class.java.simpleName
    val geocoder by lazy { Geocoder(activity) }
    val MAP_PIXEL_WIDTH by lazy { mapFragment.view!!.measuredWidth.toDouble() }
    val MAP_PIXEL_HEIGHT by lazy { mapFragment.view!!.measuredHeight.toDouble() }
    val MAPS_API_KEY by lazy { resources.getString(R.string.google_maps_key) }
    val DIRECTION_MODE = "bicycling"
    //val DIRECTION_MODE = "walking"
    val ORIGIN = "52.150001400986284,21.03444099676267"
    val DESTINATION = "52.14923774618937,21.042594912167942"
    val WAYPOINTS = "52.151923646458364,21.038818361874974|52.151791988457184,21.037530901547825"
    val googleDirectionsService by lazy { GoogleDirectionsService.create() }

    val MAPS_THEME_KEY = "map_theme"
    var isFirstOpened = true
    var isRideActive = false


    lateinit var listOfGeoPoints: ArrayList<Location>
    lateinit var mapFragment: SupportMapFragment
    lateinit var mMap: GoogleMap
    lateinit var disposable: Disposable
    lateinit var addMeasurementButton: FloatingActionButton


    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }

        listOfGeoPoints = arrayListOf()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        setupLiveData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_noise_map, container, false)
        mapFragment = childFragmentManager.findFragmentById((R.id.map_new)) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val zoomLocationButton = view.findViewById<FloatingActionButton>(R.id.fab_loc)
        zoomLocationButton.setOnClickListener {
            val latlng = LatLng(52.1518944, 21.0288875)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12.0f))
        }

        addMeasurementButton = view.findViewById(R.id.fab_add)
        addMeasurementButton.setOnClickListener {
            if (!isRideActive) {
                showAddMeasurementDialog()
            } else {
                showStopMeasurement()
            }
        }

        val textV = view.findViewById<TextView>(R.id.myTextProgress)
        textV.text = getString(R.string.progress_bar_noise, 123)

        return view
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == MAPS_THEME_KEY) {
            changeMapStyle(sharedPreferences, key)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

    }

    fun showAddMeasurementDialog() {
        val view = LayoutInflater.from(activity).inflate(R.layout.alert_add_measurement, null)
        val positiveButton = view.findViewById<Button>(R.id.btn_alert_positive)
        val negativeButton = view.findViewById<Button>(R.id.btn_alert_negative)

        val addMeasurementAlert = AlertDialog.Builder(activity)
                .setTitle(R.string.alert_dialog_ride_tile)
                .setMessage(R.string.alert_dialog_ride_message)
                .setView(view)
                .setCancelable(false)
                .show()

        positiveButton.setOnClickListener {

            addMeasurementButton.setImageDrawable(resources.getDrawable(R.drawable.stop_button))
            isRideActive = true
            addMeasurementAlert.cancel()
        }
        negativeButton.setOnClickListener {
            addMeasurementAlert.cancel()
        }
    }

    fun showStopMeasurement() {
        val builder = AlertDialog.Builder(activity)
                .setTitle(R.string.alert_dialog_stop_ride_tile)
                .setMessage(R.string.alert_dialog_stop_ride_message)
                .setCancelable(false)
                .setPositiveButton("Yes", { dialog, which ->
                    addNewRide()
                    addMeasurementButton.setImageDrawable(resources.getDrawable(android.R.drawable.ic_input_add))
                    isRideActive = false
                    dialog.cancel()
                })
                .setNegativeButton("No", { dialog, which -> dialog.cancel() })
                .show()
    }

    fun changeMapStyle(sharedPreferences: SharedPreferences?, key: String) {
        val themePref = sharedPreferences?.getString(key, "")
        val mapStyle = when (themePref) {
            "Default" -> R.raw.default_map_style
            "Retro" -> R.raw.retro_map_style
            "Night" -> R.raw.night_map_style
            else -> R.raw.default_map_style
        }
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, mapStyle))
        Log.d(TAG, "Maps theme changed to: $themePref")
    }

    @SuppressWarnings("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "MAP READY")
        mMap = googleMap

        if (isFirstOpened) {
            Log.d(TAG, "First map theme load from settings")
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            changeMapStyle(sharedPreferences, MAPS_THEME_KEY)
            isFirstOpened = false
        }
        //mMap.isMyLocationEnabled = true
        //mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        val compassButton = mapFragment.view?.findViewWithTag<View>("GoogleMapCompass")
        val rlp = compassButton!!.layoutParams as RelativeLayout.LayoutParams
        //Log.d(TAG, "${rlp.topMargin} ${rlp.bottomMargin} ${rlp.rightMargin} ${rlp.leftMargin} rlp")
        rlp.addRule(RelativeLayout.ALIGN_PARENT_END)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
        rlp.setMargins(0, 30, 30, 0)

        mMap.setOnCameraIdleListener {
            val zoomLevel = mMap.cameraPosition.zoom
            val metersPerPx = 0.33 * 156543.03392 * Math.cos(mMap.cameraPosition.target.latitude * Math.PI / 180) / Math.pow(2.0, zoomLevel.toDouble())
            val kilometersPerPixel = metersPerPx / 1000
            val queryRadius = 0.5 * Math.sqrt(MAP_PIXEL_HEIGHT * MAP_PIXEL_HEIGHT + MAP_PIXEL_WIDTH * MAP_PIXEL_WIDTH) * kilometersPerPixel

        }
    }


    fun setupLiveData() {
        val firebaseViewModel = ViewModelProviders.of(this)
                .get(FirebaseQueryViewModel::class.java)
        firebaseViewModel.getDataSnapshotLiveData()

        val locationViewModel = ViewModelProviders.of(this)
                .get(LocationViewModel::class.java)
        val locationLiveData = locationViewModel.getLocation(activity)

        val audioViewModel = ViewModelProviders.of(this)
                .get(MeasurementViewModel::class.java)
        val audioLiveData = audioViewModel.getAudioLevel(activity)

        val audioLocationLiveData = transformLiveData(locationLiveData, audioLiveData)
                .observe(this,
                        Observer<Pair<Location, Int>> { pair ->
                            if (!listOfGeoPoints.contains(pair!!.first)) {
                                listOfGeoPoints.add(pair.first)
                            }
                            peformDirectionsApiCall(ORIGIN, DESTINATION, WAYPOINTS, DIRECTION_MODE)
                            Log.d(TAG, "Location list ${listOfGeoPoints.size}")
                            Log.d(TAG, "Location: ${pair?.first?.latitude} ${pair?.first?.longitude}")
                        })
    }

    fun transformLiveData(locationLiveData: LiveData<Location>, audioLiveData: LiveData<Int>): LiveData<Pair<Location, Int>> {
        val rxDataPublisher: Publisher<Int> = LiveDataReactiveStreams.toPublisher(
                this, audioLiveData)

        val rxLocationPublisher: Publisher<Location> = LiveDataReactiveStreams.toPublisher(
                this, locationLiveData)

        val rxFlowable: Flowable<Pair<Location, Int>> = Flowable.fromPublisher(rxLocationPublisher)
                .withLatestFrom(
                        rxDataPublisher, BiFunction { location, integer -> Pair(location, integer) })

        return LiveDataReactiveStreams.fromPublisher(rxFlowable)
    }

    fun peformDirectionsApiCall(origin: String, destination: String, waypoints: String, mode: String) {
        disposable = googleDirectionsService.getEncodedPolyline(origin, destination, waypoints, mode, key = MAPS_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Log.d(TAG, "$result")
                    val toDraw = PolyUtil.decode(result.routes[0].overview_polyline.points)
                    mMap.addPolyline(PolylineOptions().addAll(toDraw))
                },
                        { error -> Log.e(TAG, "OnError: {${error.message}}") },
                        { Log.d(TAG, "OnComplete: API call completed") })
    }

    object PushRideDataHelper {
        val TAG = "PushRideDataHelper"
        val DB_REF = FirebaseDatabase.getInstance().reference
        val ID = FirebaseAuth.getInstance().currentUser?.uid
        val REF = DB_REF.child("/rides/$ID")
    }

    fun addNewRide() {
        val rideId = PushRideDataHelper.REF.push().key
        val ride = RideModel(123456,
        "name",
        "cycling",
        100,
        0,
        0)
        PushRideDataHelper.REF.child(rideId).setValue(ride) { error, key ->
            if (error != null) {
                Log.e(PushRideDataHelper.TAG, "Error: $error.Ride was NOT SAVED on the server")
            } else {
                Log.d(PushRideDataHelper.TAG, "Ride was successfully SAVED on the server. Used key: $key")
            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NoiseMapFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): NoiseMapFragment {
            val fragment = NoiseMapFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}