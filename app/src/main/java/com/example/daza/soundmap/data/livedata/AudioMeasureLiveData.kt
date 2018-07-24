package com.example.daza.soundmap.data.livedata

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.example.daza.soundmap.utils.AudioHelper
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

/**
 * Created by daza on 09.04.18.
 */
class AudioMeasureLiveData(val context: Context) : MutableLiveData<Int>() {
    //val audioHelper = AudioHelper(context)
    //var isAudioRecording = false
    val timer = Timer("schedule", true)


    override fun onActive() {
        super.onActive()
        //audioHelper.recordAudio()
//        timer.scheduleAtFixedRate(1000, 1000) {
//            //postValue(audioHelper.tmpEndedAt)
//            postValue(1)
//        }

    }

    override fun onInactive() {
        //audioHelper.stopRecording()
    }

}