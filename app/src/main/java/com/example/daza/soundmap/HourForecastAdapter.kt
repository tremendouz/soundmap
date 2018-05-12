package com.example.daza.soundmap

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by daza on 11.05.18.
 */
class HourForecastAdapter : RecyclerView.Adapter<HourForecastAdapter.HourForecastViewHolder>() {
    var forecastList: List<Data> =  emptyList<Data>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): HourForecastViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.row_hour_forecast, parent, false)
        return HourForecastViewHolder(view)
    }

    override fun getItemCount(): Int {
        return forecastList.size
    }

    override fun onBindViewHolder(holder: HourForecastViewHolder?, position: Int) {
        val formattedDate = convertTimestampToDate(forecastList[position].time.toLong())
        val time = formattedDate.split(" ")[1]+"\n"
        val date = formattedDate.split(" ")[0]
        val spannableString = SpannableString(time + date)
        spannableString.setSpan(RelativeSizeSpan(2f), 0, 5, 0)
        holder?.hour?.text = spannableString
        holder?.windSpeed?.text = forecastList[position].windSpeed.toString() + " m/s"
        holder?.windBurst?.text = forecastList[position].windGust.toString() + " m/s"

    }

    fun convertTimestampToDate(timestamp: Long): String{
        val date = Date(TimeUnit.MILLISECONDS.convert(timestamp.toLong(), TimeUnit.SECONDS))
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
        return formatter.format(date)
    }

    inner class HourForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hour = itemView.findViewById<TextView>(R.id.txt_forecast_for_hour)
        val windSpeed = itemView.findViewById<TextView>(R.id.txt_hour_wind_speed)
        val windBurst = itemView.findViewById<TextView>(R.id.txt_hour_wind_gust)
    }
}