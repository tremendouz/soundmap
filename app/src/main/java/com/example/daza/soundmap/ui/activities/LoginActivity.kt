package com.example.daza.soundmap.ui.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.daza.soundmap.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import io.reactivex.Observable
import io.reactivex.functions.BiFunction


class LoginActivity : AppCompatActivity() {

    val REQUEST_PERMISSIONS_CODE = 0


    val TAG = LoginActivity::class.java.simpleName
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var goToRegisterButton: Button
    lateinit var textEmail: EditText
    lateinit var textPassword: EditText
    lateinit var inputEmail: TextInputLayout
    lateinit var inputPassword: TextInputLayout
    lateinit var loginButton: Button
    lateinit var restorePasswordButton: Button
    val debounceTime: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        firebaseAuth = FirebaseAuth.getInstance()
        textEmail = findViewById(R.id.text_email)
        inputEmail = findViewById(R.id.input_layout_email)
        textPassword = findViewById(R.id.text_password)
        inputPassword = findViewById(R.id.input_layout_password)
        loginButton = findViewById(R.id.button_login)
        loginButton.setOnClickListener {
            loginUser(textEmail.text.toString(), textPassword.text.toString())
        }

        val emailObservable = RxTextView.textChanges(textEmail)
        emailObservable.map { text -> isValidEmail(text.toString()) }
                .skip(1)
                .debounce(debounceTime, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { isValid -> validateEmail(isValid) }

        val passwordObservable = RxTextView.textChanges(textPassword)
        passwordObservable.map { text -> isValidPassword(text.toString()) }
                .skip(1)
                .debounce(debounceTime, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { isValid -> validatePassword(isValid) }

        val combinedObservable: Observable<Boolean> = Observable.combineLatest(
                emailObservable,
                passwordObservable,
                BiFunction { email, pass ->
                    isValidEmail(email.toString()) &&
                            isValidPassword(pass.toString())
                })
        combinedObservable.subscribe { isValid -> isValidForm(isValid) }

        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        }

        goToRegisterButton = findViewById(R.id.button_registration)
        goToRegisterButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }

        restorePasswordButton = findViewById(R.id.button_forgot_password)
        restorePasswordButton.setOnClickListener {
            restorePassword()
        }
    }


    fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "LoginUserWithEmail:success")
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.w(TAG, "LoginUserWithEmail:failure")
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        inputEmail.isErrorEnabled = false
        inputPassword.isErrorEnabled = false
    }

    override fun onStop() {
        super.onStop()
        textEmail.text.clear()
        textPassword.text.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateEmail(valid: Boolean) {
        if (!valid)
            inputEmail.error = resources.getString(R.string.error_input_email)
        inputEmail.isErrorEnabled = !valid

    }

    fun isValidPassword(password: String): Boolean {
        return password.isNotEmpty() && password.length > 6
    }


    fun validatePassword(valid: Boolean) {
        if (!valid)
            inputPassword.error = "Password is to weak"
        inputPassword.isErrorEnabled = !valid
    }

    fun isValidForm(boolean: Boolean) {
        loginButton.isEnabled = boolean
    }

    fun restorePassword(email: String = "dawidos00952@gmail.com") {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "SendRestorePasswordEmail:success")
                        toast("Email has been sent.")

                    } else {
                        Log.w(TAG, "SendRestorePasswordEmail:failure")
                    }
                }
    }


    fun Context.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}