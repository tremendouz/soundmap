package com.example.daza.soundmap.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Created by daza on 08.04.18.
 */
class AudioHelper(val context: Context){
    val TAG = "AudioHelper"

    val RECORD_TIME = 5000L
    val SAMPLE_RATE = 44100
    val MIN_RMS = 4.8253787224504133E-5
    val B_COEFFICIENT = doubleArrayOf(0.169994948147430,
            0.280415310498794,
            -1.120574766348363,
            0.131562559965936,
            0.974153561246036,
            -0.282740857326553,
            -0.152810756202003)
    val A_COEFFICIENT = doubleArrayOf(1.00000000000000000,
            -2.12979364760736134,
            0.42996125885751674,
            1.62132698199721426,
            -0.96669962900852902,
            0.00121015844426781,
            0.04400300696788968)


    val MIN_BUFFER_SIZE by lazy {
        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
    }
    val isBufferSizeCorrect = MIN_BUFFER_SIZE != AudioTrack.ERROR && MIN_BUFFER_SIZE != AudioTrack.ERROR_BAD_VALUE
    var isRecording = false

    val PREFS_FILENAME = "com.example.dawid.soundmeter.prefs"
    val MIN_RMS_VALUE = "com.example.dawid.soundmeter.calibratedvalue"
    val sharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)


    fun recordAudio(isCalibrationNeeded: Boolean = false){
        if (isBufferSizeCorrect){
            thread {
                val audioBuffer = ByteArray(MIN_BUFFER_SIZE)
                val audioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, MIN_BUFFER_SIZE)
                if (audioRecord.state== AudioRecord.STATE_INITIALIZED){
                    audioRecord.startRecording()
                    launch {
                        isRecording = true
                        delay(RECORD_TIME, TimeUnit.MILLISECONDS)
                        isRecording = false
                    }
                    Log.i(TAG, "Recording started")
                    while(isRecording){
                        if(isCalibrationNeeded){
                            Log.i(TAG, "Calibration started")
                            var sum: Double = 0.0
                            val readLength= audioRecord.read(audioBuffer, 0, MIN_BUFFER_SIZE)
                            for (i in 0 until readLength){
                                sum += audioBuffer[i] * audioBuffer[i]
                            }
                            if(readLength >0) {
                                val minAmplitude: Double = (Math.sqrt(sum / readLength))
                                var savedAmplitude = sharedPreferences.getFloat(MIN_RMS_VALUE, 0.0F).toDouble()
                                if (savedAmplitude == 0.0) {
                                    savedAmplitude = minAmplitude
                                } else if (savedAmplitude > minAmplitude) {
                                    sharedPreferences.edit().putFloat(MIN_RMS_VALUE, minAmplitude.toFloat()).apply()
                                    Log.i(TAG, "Current MIN_RMS_VALUE: $minAmplitude")
                                }
                            }
                        }
                        val readLength= audioRecord.read(audioBuffer, 0, MIN_BUFFER_SIZE)
                    }
                    //TODO add A-weight filtering to audioBuffer
                    audioRecord.stop()
                    audioRecord.release()
                }
                else{
                    Log.e(TAG, "Audio record state is not initialized")
                }
            }
        }
        else {
            Log.d(TAG, "Buffer size is not correct")
        }
    }

    fun filterAudioBuffer(input: ByteArray){

        // input ByteArray to ShortArray (16 bit)
        val shortBuffer = ByteBuffer.wrap(input).asShortBuffer()
        val shortArray = ShortArray(shortBuffer.capacity())
        shortBuffer.get(shortArray)

        // ShortArray to DoubleArray
        var doubleArray = DoubleArray(shortArray.size)
        for (i in shortArray.indices){
            doubleArray[i] = java.lang.Short.reverseBytes(shortArray[i]).toDouble() / 0x8000
        }

        // Apply A-weighting
        doubleArray = applyWeightingFilter(A_COEFFICIENT, B_COEFFICIENT, doubleArray)

        // Get RMS in dB
        val batchArray = doubleArray.asSequence().chunked(MIN_BUFFER_SIZE/2)
        val rmsList = arrayListOf<Double>()
        for (item in batchArray){
            val sum: Double =  item.sumByDouble { it * it }
            val rms = 20*Math.log10(Math.sqrt(sum/item.size)/ MIN_RMS)
            rmsList.add(rms)
        }

    }

    fun applyWeightingFilter(a: DoubleArray, b: DoubleArray, signal: DoubleArray): DoubleArray {

        val result = DoubleArray(signal.size)
        for (i in 0 until signal.size) {
            var tmp = 0.0
            for (j in 0 until b.size) {
                if (i - j < 0) continue
                tmp += b[j] * signal[i - j]
            }
            for (j in 1 until a.size) {
                if (i - j < 0) continue
                tmp -= a[j] * result[i - j]
            }
            tmp /= a[0]
            result[i] = tmp
        }
        return result
    }


}