package com.example.daza.soundmap

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import org.jetbrains.anko.toast

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
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                true
            }
            else {
                var fragment = when (it.itemId) {
                    R.id.nav_sound_map -> SoundMapFragment()
                    R.id.nav_my_trips -> SoundMapFragment()
                    R.id.nav_saved_trips -> SoundMapFragment()
                    R.id.nav_forecast -> ForecastFragment()
                    R.id.nav_acc_info -> ForecastFragment()
                    R.id.nav_settings -> ForecastFragment()
                    R.id.nav_app_info -> AboutFragment()
                    else -> SoundMapFragment()
                }

                fragmentManager
                        .beginTransaction()
                        .replace(R.id.main_activity_frame, fragment)
                        .commit()
                it.isChecked = true
                drawerLayout.closeDrawers()
                true
            }
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
