package com.example.daza.soundmap.data.livedata

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.example.daza.soundmap.utils.AudioHelper
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

/**
 * Created by daza on 09.04.18.
 */
class AudioMeasureLiveData(val context: Context) : MutableLiveData<Int>() {
    val audioHelper = AudioHelper(context)
    lateinit var timer: Timer

    override fun onActive() {
        super.onActive()
        audioHelper.testDb()
        setUpTimer()

    }

    override fun onInactive() {
        audioHelper.stopRecording()
        timer.cancel()
        timer.purge()
    }

    fun setUpTimer(){
        timer = Timer("schedule", true)
        timer.scheduleAtFixedRate(1000, 1000) {
            postValue(audioHelper.fifo.value.toInt())
            //Log.d("AUDIO LIVE DATA", "${audioHelper.fifo.value.toInt()}")

        }
    }

}