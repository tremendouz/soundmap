package com.example.daza.soundmap.data.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.daza.soundmap.R
import com.example.daza.soundmap.data.models.Data
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by daza on 12.05.18.
 */
class DayForecastAdapter : RecyclerView.Adapter<DayForecastAdapter.DayForecastViewHolder>() {
    var forecastList: List<Data> = emptyList<Data>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DayForecastViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.row_day_forecast, parent, false)
        return DayForecastViewHolder(view)
    }

    override fun getItemCount(): Int {
        return forecastList.size
    }

    override fun onBindViewHolder(holder: DayForecastViewHolder?, position: Int) {
        holder?.day?.text = convertTimestampToDate(forecastList[position].time.toLong())
        holder?.windSpeed?.text = forecastList[position].windSpeed.toString() + " m/s"
        holder?.windBurst?.text = forecastList[position].windGust.toString() + " m/s"
    }

    fun convertTimestampToDate(timestamp: Long): String{
        val date = Date(TimeUnit.MILLISECONDS.convert(timestamp.toLong(), TimeUnit.SECONDS))
        val formatter = SimpleDateFormat("EEE, dd-MM-yyyy")
        return formatter.format(date)
    }

    inner class DayForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val day = itemView.findViewById<TextView>(R.id.txt_forecast_for_day)
        val windSpeed = itemView.findViewById<TextView>(R.id.txt_day_wind_speed)
        val windBurst = itemView.findViewById<TextView>(R.id.txt_day_wind_gust)
    }
}