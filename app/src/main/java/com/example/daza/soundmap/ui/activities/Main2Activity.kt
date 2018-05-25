package com.example.daza.soundmap.ui.activities

import android.app.Fragment
import android.app.FragmentManager
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import com.example.daza.soundmap.R
import com.example.daza.soundmap.ui.fragments.*
import com.google.android.gms.maps.MapFragment
import com.google.firebase.auth.FirebaseAuth

class Main2Activity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private val TAG = Main2Activity::class.java.simpleName
    private val firebaseAuthInstance by lazy { FirebaseAuth.getInstance() }

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
            if (it.itemId == R.id.nav_logout) {
                clearBackStack()
                firebaseAuthInstance.signOut()
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                true
            } else {
                var fragment = when (it.itemId) {
                    R.id.nav_sound_map -> NoiseMapFragment()
                    R.id.nav_my_trips -> NoiseMapFragment()
                    R.id.nav_saved_trips -> NoiseMapFragment()
                    R.id.nav_forecast -> ForecastFragment()
                    R.id.nav_acc_info -> ForecastFragment()
                    R.id.nav_settings -> SettingsFragment()
                    R.id.nav_app_info -> AboutFragment()
                    else -> NoiseMapFragment()
                }

                cacheFragment(fragment)
                it.isChecked = true
                drawerLayout.closeDrawers()
                true
            }
        }

        cacheFragment(NoiseMapFragment())
        navigationView.menu.getItem(0).isChecked = true

    }

    fun cacheFragment(fragment: android.support.v4.app.Fragment) {
        Log.d("Caching", "Working with ${fragment.javaClass.simpleName}")
        var findFragment = supportFragmentManager.findFragmentByTag(fragment.javaClass.simpleName)
        if (findFragment == null) {
            Log.d("Caching", "Creating new instance of ${fragment.javaClass.simpleName}")
            findFragment = fragment.javaClass.newInstance()
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity_frame, findFragment, fragment.javaClass.simpleName)
                .addToBackStack(null)
                .commit()
    }


    fun clearBackStack() {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if (backStackEntryCount > 0) {
            //val entry = supportFragmentManager.getBackStackEntryAt(0)
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.executePendingTransactions()
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
