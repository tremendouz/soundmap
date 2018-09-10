package com.example.daza.soundmap

import com.example.daza.soundmap.utils.CustomFIFO
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun originalAverage_isCorrect() {
        val fifo = CustomFIFO(10)
        for (i in 0 until 20){
            fifo.add(i.toDouble())
        }
        assertEquals(fifo.mAvarage(), 14)
    }

    @Test
    fun avarage_isCorrect() {
        val fifo = CustomFIFO.fifoAvarage(10)
        for (i in 0 until 20){
            fifo.add(i.toDouble())
        }
        assertEquals(fifo.value.toInt(), 14)
    }
}
