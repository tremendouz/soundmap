package com.example.daza.soundmap.data.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.daza.soundmap.ui.fragments.CurrentForecastFragment
import com.example.daza.soundmap.ui.fragments.DayForecastFragment
import com.example.daza.soundmap.ui.fragments.WeekForecastFragment

/**
 * Created by daza on 10.05.18.
 */
class ForecastPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> CurrentForecastFragment()
            1 -> DayForecastFragment()
            else -> WeekForecastFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Now"
            1 -> "Day"
            else -> {
                return "Week"
            }
        }
    }

}