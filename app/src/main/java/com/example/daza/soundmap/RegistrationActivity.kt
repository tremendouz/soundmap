package com.example.daza.soundmap

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function3
import java.util.concurrent.TimeUnit


class RegistrationActivity : AppCompatActivity() {

    val TAG = RegistrationActivity::class.java.simpleName

    lateinit var btnRegister: Button
    lateinit var textEmail: EditText
    lateinit var textPassword: EditText
    lateinit var textConfirmPassword: EditText
    lateinit var inputEmail: TextInputLayout
    lateinit var inputPassword: TextInputLayout
    lateinit var inputConfirmPassword: TextInputLayout

    var debounceTime = 0L

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var fireDB: FirebaseDatabase
    lateinit var fireRef: DatabaseReference

    lateinit var buttonDisposable: Disposable

    val USER_TABLE = "user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()
        fireDB = FirebaseDatabase.getInstance()
        fireRef = fireDB.getReference(USER_TABLE)


        btnRegister = findViewById(R.id.button_register)
        textEmail = findViewById(R.id.text_email)
        textPassword = findViewById(R.id.text_password)
        textConfirmPassword = findViewById(R.id.text_confirm_password)
        inputEmail = findViewById(R.id.input_layout_reg_email)
        inputPassword = findViewById(R.id.input_layout_reg_password)
        inputConfirmPassword = findViewById(R.id.input_layout_confirm_password)


        btnRegister.setOnClickListener {
            registerUser(textEmail.text.toString(), textPassword.text.toString())
        }
        btnRegister.isEnabled = false


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

        val confirmPasswordObservable = RxTextView.textChanges(textConfirmPassword)
        confirmPasswordObservable.map { text -> isValidConfirmPassword(text.toString()) }
                .subscribe { isValid -> validateConfirmPassword(isValid) }


        val combinedObservable: Observable<Boolean> = Observable.combineLatest(
                emailObservable,
                passwordObservable,
                confirmPasswordObservable,
                Function3 { email, pass, conf ->
                    isValidEmail(email.toString()) &&
                            isValidPassword(pass.toString()) &&
                            isValidConfirmPassword(conf.toString())
                })
        combinedObservable.subscribe { isValid: Boolean -> isValidForm(isValid) }
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


    fun isValidConfirmPassword(password: String): Boolean {
        return password == textPassword.text.toString()
    }


    fun validateConfirmPassword(valid: Boolean) {
        if (!valid)
            inputConfirmPassword.error = "Passwords do not match"
        inputConfirmPassword.isErrorEnabled = !valid
    }


    fun isValidForm(boolean: Boolean) {
        if (!boolean) {
            btnRegister.setBackgroundColor(Color.GRAY)
            btnRegister.isEnabled = false
        } else {
            btnRegister.setBackgroundColor(Color.BLUE)
            btnRegister.isEnabled = true
        }
    }

    fun registerUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = firebaseAuth.currentUser
                        fireRef.child(user?.uid).setValue("Dawid")

                        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure")
                    }
                }
    }

}
