package com.example.daza.soundmap

import android.arch.lifecycle.LiveData
import android.content.Context
import com.example.daza.soundmap.utils.AudioHelper

/**
 * Created by daza on 09.04.18.
 */
class AudioMeasureLiveData(val context:Context): LiveData<Int>() {
    val audioHelper = AudioHelper(context)

    override fun onActive() {
        super.onActive()
        //audioHelper.recordAudio()
    }

    override fun onInactive() {
        audioHelper.stopRecording()
    }

}