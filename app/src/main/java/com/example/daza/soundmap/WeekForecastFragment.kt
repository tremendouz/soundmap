package com.example.daza.soundmap

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [WeekForecastFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [WeekForecastFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeekForecastFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: DayForecastAdapter
    private lateinit var button: Button
    val TAG = DayForecastFragment::class.java.simpleName
    val API_KEY = "6a8ff9e6413d444dfcf3ce2ac051e014"
    //TODO PAMIETAC O ZMIANIE
    val exclude = "currently,minutely,hourly"
    val units = "si"
    //temp
    val temp_latng = "52.2207651, 21.0096579"
    val geocoder by lazy { Geocoder(this.context) }
    lateinit var disposable: Disposable

    val weatherForecastService by lazy { WeatherForecastService.create() }

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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
        recyclerViewAdapter = DayForecastAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_week_forecast, container, false)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_day)
        swipeRefreshLayout.apply {
            this.setOnRefreshListener {
                performQuery()
            }
        }
        recyclerView = view.findViewById(R.id.recycler_view_week_forecast)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = GridLayoutManager(this.context, 2)

        return view
    }

    fun performQuery() {
        disposable = weatherForecastService.checkDayForecast(API_KEY, temp_latng, exclude, units)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    swipeRefreshLayout.isRefreshing = false
                    Log.d(TAG, "OnNext: ${result.daily.data}")
                    recyclerViewAdapter.forecastList = result.daily.data
                    recyclerViewAdapter.notifyDataSetChanged()
                },
                        { error -> Log.e(TAG, "OnError: {${error.message}}") },
                        { Log.d(TAG, "OnComplete: API call completed") })
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }


    override fun onDetach() {
        super.onDetach()
        mListener = null
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
         * @return A new instance of fragment WeekForecastFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): WeekForecastFragment {
            val fragment = WeekForecastFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
