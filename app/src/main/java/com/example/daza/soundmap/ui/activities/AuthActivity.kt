package com.example.daza.soundmap.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import android.util.Log
import android.widget.Toast
import com.example.daza.soundmap.R
import com.example.daza.soundmap.ui.fragments.LoginFragment
import com.example.daza.soundmap.ui.fragments.SignupFragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton

class AuthActivity : AppCompatActivity(), SignupFragment.OnChangeFragmentListener {
    val TAG = AuthActivity::class.java.simpleName
    val REQUEST_PERMISSIONS_CODE = 0
    val USER_TABLE = "user"
    val firebaseAuthInstance by lazy { FirebaseAuth.getInstance() }
    val fireDB by lazy { FirebaseDatabase.getInstance() }
    val fireRef by lazy { fireDB.getReference(USER_TABLE) }

    // TODO - tests without this var
    var isTested: Boolean = true

    var isFirstFragment = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        checkPermissions()
        setupContent()

    }

    fun setupContent() {
        if (firebaseAuthInstance.currentUser != null && !isTested) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            supportFragmentManager.beginTransaction()
                    .setTransition(TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.auth_activity_frame, LoginFragment())
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStackImmediate("login", POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.executePendingTransactions()
        } else {
            finish()
        }
    }

    override fun changeFragment(fragment: Fragment) {
        if (fragment is SignupFragment) {
            supportFragmentManager.beginTransaction()
                    .setTransition(TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.auth_activity_frame, LoginFragment())
                    .addToBackStack(null)
                    .commit()
        } else if (fragment is LoginFragment) {
            if (isFirstFragment) {
                supportFragmentManager.beginTransaction()
                        .setTransition(TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.auth_activity_frame, SignupFragment())
                        .addToBackStack("login")
                        .commit()
                isFirstFragment = false
            } else {
                supportFragmentManager.beginTransaction()
                        .setTransition(TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.auth_activity_frame, SignupFragment())
                        .addToBackStack(null)
                        .commit()
            }
        }

    }

    override fun registerUser(email: String, password: String) {
        firebaseAuthInstance.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = firebaseAuthInstance.currentUser
                        fireRef.child(user?.uid).setValue("Dawid")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "createUserWithEmail:failure ${task.exception}")
                        Toast.makeText(this,
                                "Error. ${task.exception.toString().split(": ")[1]} Try again.", Toast.LENGTH_LONG).show()
                    }
                }
    }

    override fun loginUser(email: String, password: String) {
        firebaseAuthInstance.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "LoginUserWithEmail:success")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.e(TAG, "LoginUserWithEmail:failure")
                        Toast.makeText(this,
                                "Error. ${task.exception.toString().split(": ")[1]} Try again.", Toast.LENGTH_LONG).show()
                    }
                }
    }

    override fun restorePasword(email: String) {
        firebaseAuthInstance.sendPasswordResetEmail(email)
                .addOnCompleteListener { task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "SendRestorePasswordEmail:success")

                    } else {
                        Log.w(TAG, "SendRestorePasswordEmail:failure")
                        Toast.makeText(this,
                                R.string.password_restore_failed, Toast.LENGTH_LONG).show()
                    }
                }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        } else if (requestCode == REQUEST_PERMISSIONS_CODE
                && grantResults[0] == PackageManager.PERMISSION_DENIED
                || grantResults[1] == PackageManager.PERMISSION_DENIED) {
            alert("In order to use this app you have to grant both ACCESS FINE LOCATION and RECORD AUDIO permissions") {
                yesButton { checkPermissions() }
                noButton { finish() }
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
