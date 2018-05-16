package com.example.daza.soundmap.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.daza.soundmap.R
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class MainActivity : AppCompatActivity() {

    val REQUEST_PERMISSIONS_CODE = 0


    lateinit var simpleText: TextView
    lateinit var logoutButton: Button
    lateinit var firebaseAuth: FirebaseAuth

    val REQUEST_FINE_LOCATION_CODE = 0
    val REQUEST_AUDIO_RECORD_CODE = 1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()

        firebaseAuth = FirebaseAuth.getInstance()


        simpleText = findViewById(R.id.simple_text)
        simpleText.text = firebaseAuth.currentUser?.uid

        logoutButton = findViewById(R.id.button_logout)
        logoutButton.setOnClickListener {
            //firebaseAuth.signOut()
            val intent = Intent(this@MainActivity, Main2Activity::class.java)
            startActivity(intent)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        } else if (requestCode == REQUEST_PERMISSIONS_CODE
                && grantResults[0] == PackageManager.PERMISSION_DENIED
                || grantResults[1] == PackageManager.PERMISSION_DENIED) {
            alert("In order to use this app you have to grant both ACCESS FINE LOCATION and RECORD AUDIO permissions"){
                yesButton {checkPermissions()}
                noButton {finish()}
                title = "Permission denied"
                isCancelable = false
            }.show()
        }
    }


    fun checkPermissions() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO),
                REQUEST_PERMISSIONS_CODE)

    }
}

