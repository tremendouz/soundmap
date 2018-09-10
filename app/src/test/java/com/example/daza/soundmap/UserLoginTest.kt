package com.example.daza.soundmap

import android.content.Intent
import android.widget.Button
import android.widget.EditText
import com.example.daza.soundmap.ui.activities.AuthActivity
import com.example.daza.soundmap.ui.activities.MainActivity
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * Created by daza on 08.04.18.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class UserLoginTest {
    val VALID_EMAIL = "test@test.com"
    val VALID_PASSWORD = "testtest"
    val INVALID_PASSWORD = "123"
    val INVALID_EMAIL = "test@test..com"

    val activity = Robolectric.setupActivity(AuthActivity::class.java)


    @Test
    fun shouldChangeActivity_afterSuccessfulLogin(){
        fillForm(VALID_EMAIL, VALID_PASSWORD)

        //val loginButton = activity.findViewById<Button>(R.id.button_login).performClick()

        val actualIntent = Shadows.shadowOf(RuntimeEnvironment.application).nextStartedActivity
        val expectedIntent = Intent(activity, MainActivity::class.java)
        Assert.assertEquals(expectedIntent.component, actualIntent.component)

    }

//    @Test
//    fun shouldChangeActivity_ifUser_hasNoAccount(){
//        val goToRegisterButton = activity.findViewById<Button>(R.id.button_registration)
//                .performClick()
//
//        val actualIntent = Shadows.shadowOf(RuntimeEnvironment.application).nextStartedActivity
//        val expectedIntent = Intent(activity, RegistrationActivity::class.java)
//        Assert.assertEquals(expectedIntent.component, actualIntent.component)
//
//    }
//
//
//    @Test
//    fun shouldNotChangeActivity_afterFailedLogin(){
//        fillForm(INVALID_EMAIL, INVALID_PASSWORD)
//        val loginButton = activity.findViewById<Button>(R.id.button_login).performClick()
//        val actualIntent = Shadows.shadowOf(RuntimeEnvironment.application).nextStartedActivity
//        val expectedIntent = Intent(activity, AuthActivity::class.java)
//        Assert.assertEquals(expectedIntent.component, actualIntent.component)
//    }


    fun fillForm(email: String, password: String){
        val emailText = activity.findViewById<EditText>(R.id.text_email).setText(email)
        val passwordText = activity.findViewById<EditText>(R.id.text_password).setText(password)
        val loginButton = activity.findViewById<Button>(R.id.button_login).performClick()
    }

}