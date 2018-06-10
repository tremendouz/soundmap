package com.example.daza.soundmap.data.models

/**
 * Created by daza on 06.06.18.
 */
data class RideModel(
        val datetime: Int = 0,
        val name: String = "name",
        val typeOfActivity:String = "typeOfActivity",
        val distance: Int = 0,
        val duration: Int = 0,
        val numberOfMeasurements: Int = 0
)
