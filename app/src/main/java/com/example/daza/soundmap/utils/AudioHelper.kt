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
import kotlin.concurrent.thread
import kotlin.math.abs

/**
 * Created by daza on 08.04.18.
 */
class AudioHelper(val context: Context) {
    val TAG = "AudioHelper"

    @Volatile
    var tmpEndedAt: Int = 0

    @Volatile
    lateinit var fifo: CustomFIFO.fifoAvarage

    val RECORD_TIME = 5000L
    val SAMPLE_RATE = 44100
    // TODO this value gives better results in dB
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
    // TODO this min_rms gives wrong db Values
    //val MIN_RMS = sharedPreferences.getFloat(MIN_RMS_VALUE, 0.0F).toDouble()


    //TODO Investigate dB values
    fun recordAudio(isCalibrationNeeded: Boolean = false){
        var dbInit = 0
        if (isBufferSizeCorrect) {
            thread {
                val amplitudeFromSharedPreferences = sharedPreferences.getFloat(MIN_RMS_VALUE, 0.0F).toDouble()
                Log.i(TAG, "Amplitude saved in shared preferences: $amplitudeFromSharedPreferences")
                val audioBuffer = ByteArray(MIN_BUFFER_SIZE)
                val audioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, MIN_BUFFER_SIZE)
                if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord.startRecording()
                    isRecording = true
//                    launch {
//                        delay(RECORD_TIME, TimeUnit.MILLISECONDS)
//                        isRecording = false
//                    }
                    Log.i(TAG, "Recording started")
                    while (isRecording) {
                        if (isCalibrationNeeded) {
                            var sum: Double = 0.0
                            val readLength = audioRecord.read(audioBuffer, 0, MIN_BUFFER_SIZE)
                            Log.i(TAG, "READ LEN $readLength")
                            for (i in 0 until readLength) {
                                sum += audioBuffer[i] * audioBuffer[i]
                            }
                            if (readLength > 0) {
                                val minAmplitude: Double = (Math.sqrt(sum / readLength))
                                val dBcurrent = 20* Math.log10(minAmplitude / MIN_RMS)
                                Log.i(TAG, "Current Amplitude $minAmplitude || in dB $dBcurrent")

                                val savedAmplitude = sharedPreferences.getFloat(MIN_RMS_VALUE, 0.0F).toDouble()
                                if (savedAmplitude > minAmplitude) {
                                    sharedPreferences.edit().putFloat(MIN_RMS_VALUE, minAmplitude.toFloat()).apply()
                                    val newSavedAmplitude = sharedPreferences.getFloat(MIN_RMS_VALUE, 0.0F).toDouble()
                                    Log.i(TAG, "New saved amplitude $newSavedAmplitude")
                                }

                            }
                        } else {
                            audioRecord.read(audioBuffer, 0, MIN_BUFFER_SIZE)
                            val dBMaxValue = filterAudioBuffer(audioBuffer)
                            tmpEndedAt = dBMaxValue
//                            if (dBMaxValue > dbInit ){
//                                dbInit = dBMaxValue
//                                Log.d(TAG, "Last calculated value ${dbInit}")
//                            }


                        }
                    }
                    audioRecord.stop()
                    audioRecord.release()
                    Log.d(TAG, "Recording stopped")

                } else {
                    Log.e(TAG, "Audio record state is not initialized")
                }

            }
        } else {
            Log.d(TAG, "Buffer size is not correct")
        }

    }

    fun stopRecording(){
        isRecording = false
    }

    fun filterAudioBuffer(input: ByteArray): Int {

        // input ByteArray to ShortArray (16 bit)
        val shortBuffer = ByteBuffer.wrap(input).asShortBuffer()
        val shortArray = ShortArray(shortBuffer.capacity())
        shortBuffer.get(shortArray)

        // ShortArray to DoubleArray
        var doubleArray = DoubleArray(shortArray.size)
        for (i in shortArray.indices) {
            doubleArray[i] = java.lang.Short.reverseBytes(shortArray[i]).toDouble() / 0x8000
        }
        //Log.d("TAG", "MIN DUBLE ${doubleArray.min()} }}")

        // Apply A-weighting
        doubleArray = applyWeightingFilter(A_COEFFICIENT, B_COEFFICIENT, doubleArray)

        //Get RMS in dB
        val batchArray = doubleArray.asSequence().chunked(MIN_BUFFER_SIZE / 2)
        val rmsList = arrayListOf<Double>()
        for (item in batchArray) {
            val sum: Double = item.sumByDouble { it * it }
            val rms = 20 * Math.log10(Math.sqrt(sum / item.size) / MIN_RMS)
            rmsList.add(rms)
        }
        return rmsList.max()!!.toInt()
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


    /////////////////////////////////////////////////
    // TMP MEASURE

    fun testDb() {
        fifo = CustomFIFO.fifoAvarage(10)

        val SAMPLE_RATE = 44100
        val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        val SOURCE = MediaRecorder.AudioSource.DEFAULT

        val dB: Double
        val average = 0.0
        val dbSumTotal = 0.0
        val instant = 0.0
        //val bufferSize = 8192
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)

        //val recorder = AudioRecord(SOURCE, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize)
        val recorder = AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        val buffer = ShortArray(bufferSize)

        isRecording = true
        var current = 0
        recorder.startRecording()
//        launch {
//            delay(1000L)
//            isRecording = false
//        }
        thread {
            Log.d(TAG, "Recording STARTED")
            var counter = 0
            while (isRecording) {
                val xlen = recorder.read(buffer, 0, bufferSize)
                // Log.d("Elo", "Max buffer ${buffer.max()}")
                //val new_buffer = buffer
//                for (i in 0 until xlen) {
//                    if (buffer[i] < 0) {
//                        //Log.d(TAG, "WTF probka mniejsza od 0")
//                    } else {
//                        //Log.d(TAG, "${buffer[i]}")
//                        if (buffer[i] > current) {
//                            current = buffer[i].toInt()
//                            //Log.d(TAG, "MAX $current")
//
//                        }
//                    }
//                }
                //Log.d(TAG, "${Arrays.toString(buffer)}")
                val result = filterShortArray(buffer)
                fifo.add(result)
                //counter += 1
                //Log.d(TAG, "Max RMS: $result")
            }

            recorder.stop()
            recorder.release()
//            val testar = shortArrayOf(1, 2, 5, 1107, 6526, 9759, 13711, 16295, 20985, 27061, 32767).toDouble()
//            val testarfail = shortArrayOf(-1, -2, -5, -1107, -6526, -9759, -13711, -16295, -20985, -27061, -32767).toDouble()
//            Log.d(TAG, "${Arrays.toString(testar)}")
//            Log.d(TAG, "${Arrays.toString(testarfail)}")
            Log.d(TAG, "Stopped recording")
            //Log.d(TAG, " counter $counter")

        }


    }


    fun bufferResults(){

    }

    fun DoubleArray.absmin(): Double{
        var min :Double = abs(this[0])
        for (i in 0 until this.size) {
            if (min > abs(this[i]) && abs(this[i]) != 0.0) {
                min = abs(this[i])
            }
        }
        return min
    }



    fun filterShortArray(input: ShortArray): Double {
//        val B_COEFFICIENT = doubleArrayOf(0.169994948147430,
//                0.280415310498794,
//                -1.120574766348363,
//                0.131562559965936,
//                0.974153561246036,
//                -0.282740857326553,
//                -0.152810756202003)
        val B_COEFFICIENT = doubleArrayOf(0.25574113, -0.51148225, -0.25574113,  1.0229645 , -0.25574113,
                -0.51148225,  0.25574113)
        val A_COEFFICIENT = doubleArrayOf(1.00000000e+00, -4.01957618e+00,  6.18940644e+00, -4.45319890e+00,
                1.42084295e+00, -1.41825474e-01,  4.35117723e-03)
//        val A_COEFFICIENT = doubleArrayOf(1.00000000000000000,
//                -2.12979364760736134,
//                0.42996125885751674,
//                1.62132698199721426,
//                -0.96669962900852902,
//                0.00121015844426781,
//                0.04400300696788968)
        //Log.d(TAG, "MIN ${input.toDouble().absmin()}")
        //Log.d(TAG, "MAX ${input.toDouble().max()}")
        val doubleArray = applyWeightingFilter(A_COEFFICIENT, B_COEFFICIENT, input.toDouble())

        //Log.d(TAG, "ABSMIN ${doubleArray.absmin()}")
        //Log.d(TAG, "MIN ${doubleArray.min()}")
        //Log.d(TAG, "MAX ${doubleArray.max()}")
        //Log.d(TAG, "test test ${Arrays.toString(doubleArray)}")
        // Log.d(TAG, "MAX data ${input.max() }}")
//        for(i in 0 until doubleArray.size){
//            Log.d(TAG, "${doubleArray[i]}")
//        }
        var sum = 0.0
        for (item in doubleArray) {
            sum += item * item
        }

//
//        --- TU JUZ WYNIK -----------
        val rms = 20 * Math.log10(Math.sqrt(sum / doubleArray.size))
        //Log.d(TAG, "RMS: $rms")
        return rms
    }

    fun ShortArray.toDouble(): DoubleArray {
        val doubleArray = DoubleArray(this.size)
        for (i in 0 until this.size) {
            //doubleArray[i] = this[i].toUInt().toDouble()
            doubleArray[i] = this[i].toDouble()
        }
        return doubleArray
    }

    fun ShortArray.toUInt(): IntArray {
        val uintArray = IntArray(this.size)
        for (i in 0 until this.size) {
            uintArray[i] = this[i].toUInt()
        }
        return uintArray
    }

    fun Short.toUInt() = toInt() and 0xffff


//    fun Short.toUInt(): Int {
//        return this.toInt() and 0xffff
//    }

    // END TMP
}