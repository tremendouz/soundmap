package com.example.daza.soundmap.utils

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import java.util.*

/**
 * Created by daza on 18.05.18.
 */
fun <T> LiveData<T>.reObserve(owner: LifecycleOwner, observer: Observer<T>) {
    removeObserver(observer)
    observe(owner, observer)
}


open class CustomFIFO(size: Int): Queue<Double>{

    val maxsize = size
    val ring = DoubleArray(maxsize)
    var index = 0

    override fun contains(element: Double?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addAll(elements: Collection<Double>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun element(): Double {
        return ring[getCurrentIndex()]
    }

    override fun isEmpty(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val size: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun containsAll(elements: Collection<Double>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun iterator(): MutableIterator<Double> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(element: Double?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAll(elements: Collection<Double>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun add(element: Double): Boolean {
        return offer(element)
    }

    override fun offer(newElement: Double): Boolean {
        ring[index]= newElement
        incrIndex()
        return true
    }

    override fun retainAll(elements: Collection<Double>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun peek(): Double {
        return ring[getCurrentIndex()]
    }

    override fun poll(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    protected fun incrIndex() {
        index = nextIndex(index)
    }

    fun nextIndex(current: Int): Int {
        return if (current + 1 >= ring.size) {
            0
        } else
            current + 1
    }

    fun previousIndex(current: Int): Int {
        return if (current - 1 < 0) {
            ring.size - 1
        } else
            current - 1
    }

    fun getCurrentIndex(): Int{
        if (index == 0) { return ring.size -1; }
        else return index-1;
    }

    fun mAvarage(): Int {
        var sum = 0.0;
        for(i in 0 until maxsize){
            sum += ring[i]
        }
        return (sum/maxsize).toInt()
    }


    class fifoAvarage(size: Int): CustomFIFO(size){
        var numerator = 0.0
        var value = 0.0

        override fun add(element: Double): Boolean {
            return offer(element)
        }

        override fun offer(newElement: Double): Boolean {
            numerator -= ring[index]

            val res = super.offer(newElement)

            numerator += ring[getCurrentIndex()]
            value = numerator / ring.size.toFloat()

            return res
        }
    }

}
