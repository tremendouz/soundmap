package com.example.daza.soundmap.viewmodels

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.example.daza.soundmap.data.livedata.AudioMeasureLiveData

/**
 * Created by daza on 11.04.18.
 */
class MeasurementViewModel: ViewModel() {
    var audioData: AudioMeasureLiveData? = null

    fun getAudioLevel(context: Context): AudioMeasureLiveData =
            audioData ?: synchronized(this) {
                audioData ?: AudioMeasureLiveData(context).also {
                    audioData = it }
            }
}