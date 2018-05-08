package com.example.daza.soundmap

import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber

class WeatherForecastActivity : AppCompatActivity() {
    val TAG = WeatherForecastService::class.java.simpleName
    val API_KEY = "6a8ff9e6413d444dfcf3ce2ac051e014"
    val exclude = "minutely,hourly,daily"
    val units = "si"
    //temp
    val temp_latng = "52.2207651, 21.0096579"

    lateinit var webutton: Button
    val geocoder by lazy { Geocoder(this) }


    val weatherForecastService by lazy { WeatherForecastService.create() }
    lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_forecast)
        webutton = findViewById(R.id.webutton)
        webutton.setOnClickListener {
            performQuery()
        }
    }

    fun performQuery() {
        disposable = weatherForecastService.checkForecast(API_KEY, temp_latng, exclude, units)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Log.d(TAG, "OnNext: $result ${geocoder.getFromLocation(result.latitude, result.longitude, 1)[0].getAddressLine(0)}")
                },
                        { error -> Log.e(TAG, "OnError: {${error.message}}") },
                        { Log.d(TAG, "OnComplete: API call completed") })
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
        Log.d(TAG, "Dispose observable")
    }
}
