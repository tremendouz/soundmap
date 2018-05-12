package com.example.daza.soundmap

import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.observable.ObservableFromArray
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber

class WeatherForecastActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    val TAG = WeatherForecastService::class.java.simpleName
    val API_KEY = "6a8ff9e6413d444dfcf3ce2ac051e014"
    //TODO PAMIETAC O ZMIANIE
    val exclude = "hourly,minutely,daily"
    val units = "si"
    //temp
    val temp_latng = "52.2207651, 21.0096579"

//    lateinit var webutton: Button
//    lateinit var image: ImageView
    val geocoder by lazy { Geocoder(this) }


    val weatherForecastService by lazy { WeatherForecastService.create() }


    lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_forecast)
        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            performQuery()
            // close drawer when item is tapped
            drawerLayout.closeDrawers()

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            true
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.apply {
            this.setTitleTextColor(resources.getColor(android.R.color.white))
        }
        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.menu)
        }

        val pagerAdapter = ForecastPagerAdapter(supportFragmentManager)
        viewPager = findViewById(R.id.view_pager)
        viewPager.adapter = pagerAdapter
        tabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getAddressFromGeo(latitude: Double, longitude: Double): String{
        return geocoder.getFromLocation(latitude, longitude, 1)[0].getAddressLine(0)
    }

    fun performQuery() {
        disposable = weatherForecastService.checkCurrentForecast(API_KEY, temp_latng, exclude, units)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Log.d(TAG, "OnNext: ${result.currently} ${getAddressFromGeo(result.latitude,result.longitude)}")
                },
                        { error -> Log.e(TAG, "OnError: {${error.message}}") },
                        { Log.d(TAG, "OnComplete: API call completed") })
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
        Log.d(TAG, "Disposing observable ...")
    }
}
