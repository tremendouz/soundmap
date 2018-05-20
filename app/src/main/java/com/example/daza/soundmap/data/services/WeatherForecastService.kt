package com.example.daza.soundmap.data.services

import com.example.daza.soundmap.data.models.CurrentForecastModel
import com.example.daza.soundmap.data.models.DailyForecastModel
import com.example.daza.soundmap.data.models.HourByHourForecastModel
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by daza on 07.05.18.
 */


interface WeatherForecastService {
    @GET("forecast/{API_KEY}/{latLng}")
    fun checkCurrentForecast(
            @Path("API_KEY") API_KEY: String,
            @Path("latLng") latLng: String,
            @Query("exclude") exclude: String,
            @Query("units") units: String
    ): Observable<CurrentForecastModel>
    @GET("forecast/{API_KEY}/{latLng}")
    fun checkHourForecast(
            @Path("API_KEY") API_KEY: String,
            @Path("latLng") latLng: String,
            @Query("exclude") exclude: String,
            @Query("units") units: String
    ): Observable<HourByHourForecastModel>
    @GET("forecast/{API_KEY}/{latLng}")
    fun checkDayForecast(
            @Path("API_KEY") API_KEY: String,
            @Path("latLng") latLng: String,
            @Query("exclude") exclude: String,
            @Query("units") units: String
    ): Observable<DailyForecastModel>


    companion object {
        fun create(): WeatherForecastService {
            val interceptor  = HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BASIC
            }

            val client : OkHttpClient = OkHttpClient.Builder().apply {
                this.addInterceptor(interceptor)
            }.build()

            val retrofit: Retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.darksky.net/")
                    .client(client)
                    .build()
            return retrofit.create(WeatherForecastService::class.java)
        }
    }
}