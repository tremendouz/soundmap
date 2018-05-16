package com.example.daza.soundmap.ui.activities

import android.app.Fragment
import android.app.FragmentManager
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import com.example.daza.soundmap.ui.fragments.ForecastFragment
import com.example.daza.soundmap.R
import com.example.daza.soundmap.ui.fragments.AboutFragment
import com.example.daza.soundmap.ui.fragments.CurrentForecastFragment
import com.example.daza.soundmap.ui.fragments.NoiseMapFragment

class Main2Activity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private val fragmentManager by lazy { supportFragmentManager }
    private val TAG = Main2Activity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.apply {
            this.setTitleTextColor(resources.getColor(android.R.color.white))
        }

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.menu)
        }

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener {
            if(it.itemId == R.id.nav_logout){
                clearBackStack()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                true
            }
            else {
                var fragment = when (it.itemId) {
                    R.id.nav_sound_map -> NoiseMapFragment()
                    R.id.nav_my_trips -> NoiseMapFragment()
                    R.id.nav_saved_trips -> NoiseMapFragment()
                    R.id.nav_forecast -> ForecastFragment()
                    R.id.nav_acc_info -> ForecastFragment()
                    R.id.nav_settings -> ForecastFragment()
                    R.id.nav_app_info -> AboutFragment()
                    else -> NoiseMapFragment()
                }

                if (fragment is NoiseMapFragment){
                    replaceMapFragment(fragment)
                }
                else{
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.main_activity_frame, fragment)
                        .commit()}
                it.isChecked = true
                drawerLayout.closeDrawers()
                true
            }
        }

    }

    fun clearBackStack(){
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if (backStackEntryCount > 0) {
            val entry = fragmentManager.getBackStackEntryAt(0)
            fragmentManager.popBackStack(entry.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            //fragmentManager.executePendingTransactions()
        }
    }

    fun replaceMapFragment(fragment: NoiseMapFragment){
        val fragmentName = fragment::class.java.simpleName
        val fragmentInBackStack = fragmentManager.popBackStackImmediate(fragmentName, 0)
        if (!fragmentInBackStack){
            fragmentManager.beginTransaction()
                    .replace(R.id.main_activity_frame, NoiseMapFragment.getInstance())
                    .addToBackStack(fragmentName)
                    .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
