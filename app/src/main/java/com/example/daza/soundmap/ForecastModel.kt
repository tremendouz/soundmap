package com.example.daza.soundmap

/**
 * Created by daza on 08.05.18.
 */


data class CurrentForecastModel(
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


data class HourByHourForecastModel(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val hourly: Hourly,
    val flags: Flags,
    val offset: Int
)

data class Hourly(
    val data: List<Data>
)

data class Data(
    val time: Int,
    val windSpeed: Double,
    val windGust: Double,
    val windBearing: Int

)


data class DailyForecastModel(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val daily: Daily,
    val flags: Flags,
    val offset: Int
)

data class Daily(
    val data: List<Data>
)

