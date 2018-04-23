package com.example.daza.soundmap

import android.arch.lifecycle.ViewModel
import android.content.Context

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