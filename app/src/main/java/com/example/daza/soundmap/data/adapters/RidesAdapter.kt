package com.example.daza.soundmap.data.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.daza.soundmap.R
import com.example.daza.soundmap.data.models.RideModel

/**
 * Created by daza on 09.06.18.
 */
class RidesAdapter: RecyclerView.Adapter<RidesAdapter.RidesViewHolder>() {
    val ridesList = arrayListOf<Pair<String, RideModel>>()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RidesViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.row_ride, parent, false)
        return RidesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ridesList.size
    }

    override fun onBindViewHolder(holder: RidesViewHolder?, position: Int) {
        holder?.rideDatetime?.text = ridesList[position].second.datetime.toString()
        holder?.rideName?.text = ridesList[position].second.name
        holder?.rideType?.text = ridesList[position].second.typeOfActivity
        holder?.rideDist?.text = ridesList[position].second.distance.toString()
        holder?.rideDuration?.text = ridesList[position].second.duration.toString()
        holder?.rideNrMeasurements?.text = ridesList[position].second.numberOfMeasurements.toString()

    }

    fun addItem(item: Pair<String, RideModel>){
        if(item !in ridesList){
            ridesList.add(item)
            notifyDataSetChanged()
        }
    }

    inner class RidesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val rideDatetime = itemView.findViewById<TextView>(R.id.txt_ride_datetime)
        val rideName = itemView.findViewById<TextView>(R.id.txt_ride_name)
        val rideType = itemView.findViewById<TextView>(R.id.txt_type_of_phy_activity)
        val rideDist = itemView.findViewById<TextView>(R.id.txt_ride_distance)
        val rideDuration = itemView.findViewById<TextView>(R.id.txt_ride_duration)
        val rideNrMeasurements = itemView.findViewById<TextView>(R.id.txt_ride_nr_measurement)
    }
}