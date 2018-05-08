package com.example.daza.soundmap

/**
 * Created by daza on 08.05.18.
 */


data class ForecastModel(
        val latitude: Double,
        val longitude: Double,
        val currently: Currently,
        val flags: Flags

)

data class Currently(
        val time: Int,
        val windSpeed: Double,
        val windGust: Double,
        val windBearing: Int

)

data class Flags(
        val units: String
)