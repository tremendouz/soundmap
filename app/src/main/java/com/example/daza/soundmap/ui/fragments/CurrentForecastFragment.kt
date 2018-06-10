package com.example.daza.soundmap.ui.fragments

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.daza.soundmap.R
import com.example.daza.soundmap.data.models.CurrentForecastModel
import com.example.daza.soundmap.data.services.WeatherForecastService
import com.example.daza.soundmap.viewmodels.LocationViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CurrentForecastFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CurrentForecastFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CurrentForecastFragment : Fragment() {
    val TAG = CurrentForecastFragment::class.java.simpleName
    var isCreated = false
    val API_KEY = "6a8ff9e6413d444dfcf3ce2ac051e014"
    //TODO PAMIETAC O ZMIANIE
    val exclude = "hourly,minutely,daily"
    val units = "si"
    //temp
    //val currentLocation = "52.2207651, 21.0096579"
    val geocoder by lazy { Geocoder(this.context) }
    lateinit var disposable: Disposable
    var savedList: CurrentForecastModel? = null
    lateinit var currentLocation: String
    lateinit var shimmerFrameLayout: ShimmerFrameLayout
    val weatherForecastService by lazy { WeatherForecastService.create() }

    private lateinit var windSpeed: TextView
    private lateinit var windBurst: TextView
    private lateinit var windDirection: TextView
    private lateinit var address: TextView
    private lateinit var lastCall: TextView


    ///////////////////////////
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
        val vm = ViewModelProviders.of(parentFragment).get(LocationViewModel::class.java)
                .getLocation(activity)
                .observe(this, android.arch.lifecycle.Observer { location ->
                    currentLocation = "${location!!.latitude}, ${location.longitude}"
                    if (!isCreated) {
                        performQuery(currentLocation)
                        isCreated = true
                    }
                })


    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_current_forecast, container, false)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.apply {
            this.setOnRefreshListener {
                performQuery(currentLocation)

            }
        }

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.duration = 1000


        windSpeed = view.findViewById(R.id.text_wind_speed)
        windBurst = view.findViewById(R.id.text_wind_burst)
        windDirection = view.findViewById(R.id.text_wind_direction)
        address = view.findViewById(R.id.text_geo_address)
        lastCall = view.findViewById(R.id.text_last_time_call)

        if (savedList != null) {
            fillTextViews(savedList!!)
        }
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }

    fun getAddressFromGeo(latitude: Double, longitude: Double): String {
        return geocoder.getFromLocation(latitude, longitude, 1)[0]
                .getAddressLine(0)
    }

    fun fillTextViews(data: CurrentForecastModel) {
        val currently = data.currently

        val windSpeedString = currently.windSpeed.toString() + " m/s"
        val windBurstString = currently.windGust.toString() + " m/s"
        val windDirString = currently.windBearing.toCompass()
        val addressString = getAddressFromGeo(data.latitude, data.longitude)
        val lastCallString = convertTimestampToDate(currently.time.toLong())

        windSpeed.text = windSpeedString
        if (currently.windSpeed > 5.28) {
            windSpeed.setTextColor(Color.RED)
        }
        windBurst.text = windBurstString
        windDirection.text = windDirString
        address.text = addressString
        lastCall.text = lastCallString

    }

    fun Int.toCompass(): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        return directions[((this + 11.25) / 22.5).toInt()]
    }

    fun convertTimestampToDate(timestamp: Long): String {
        val date = Date(TimeUnit.MILLISECONDS.convert(timestamp.toLong(), TimeUnit.SECONDS))
        val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
        return formatter.format(date)
    }


    fun performQuery(currentLocation: String) {
        shimmerFrameLayout.startShimmerAnimation()
        disposable = weatherForecastService.checkCurrentForecast(API_KEY, currentLocation, exclude, units)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    swipeRefreshLayout.isRefreshing = false
                    Log.d(TAG, "OnNext: ${result.currently} ${getAddressFromGeo(result.latitude, result.longitude)}")
                    fillTextViews(result)
                    savedList = result
                    shimmerFrameLayout.stopShimmerAnimation()

                },
                        { error -> Log.e(TAG, "OnError: {${error.message}}") },
                        { Log.d(TAG, "OnComplete: API call completed") })
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
         * @return A new instance of fragment CurrentForecastFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): CurrentForecastFragment {
            val fragment = CurrentForecastFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
