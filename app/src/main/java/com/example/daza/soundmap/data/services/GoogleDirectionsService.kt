package com.example.daza.soundmap.data.services

import com.example.daza.soundmap.data.models.MapDirectionsModel
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by daza on 19.05.18.
 */
interface GoogleDirectionsService {
    @GET("maps/api/directions/json")
    fun getEncodedPolyline(@Query("origin") origin: String,
            @Query("destination") destination: String,
            @Query("waypoints") waypoints: String,
            @Query("mode") mode: String,
            @Query("key") key: String): Observable<MapDirectionsModel>
    companion object {
        fun create(): GoogleDirectionsService {
            val interceptor  = HttpLoggingInterceptor().apply {
                this.level = HttpLoggingInterceptor.Level.BASIC

            }

            val client : OkHttpClient = OkHttpClient.Builder().apply {
                this.addInterceptor(interceptor)
            }.build()

            val retrofit: Retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://maps.googleapis.com")
                    .client(client)
                    .build()
            return retrofit.create(GoogleDirectionsService::class.java)
        }
    }
}