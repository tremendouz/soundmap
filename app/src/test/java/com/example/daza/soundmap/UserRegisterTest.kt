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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by daza on 08.04.18.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class UserRegisterTest {
    @Test
    fun shouldRegisterUser() {
        val activity = Robolectric.setupActivity(AuthActivity::class.java)

        val goToRegistrationButton = activity.findViewById<Button>(R.id.button_registration)
        goToRegistrationButton.performClick()


        val (validEmailString, validPasswordString) = generateUniqueCredentials()

        val emailText = activity.findViewById<EditText>(R.id.text_email)
                .setText(validEmailString)

        val passwordText = activity.findViewById<EditText>(R.id.text_password)
                .setText(validPasswordString)
        val confirmPasswordText = activity.findViewById<EditText>(R.id.text_confirm_password)
                .setText(validPasswordString)


        val signupButton = activity.findViewById<Button>(R.id.button_signup)

        Assert.assertNotNull(signupButton)
        assert(signupButton.isEnabled)


        signupButton.performClick()


        Assert.assertNotNull(activity.firebaseAuthInstance.currentUser)

        val actualIntent = Shadows.shadowOf(RuntimeEnvironment.application).nextStartedActivity
        val expectedIntent = Intent(activity, MainActivity::class.java)
        Assert.assertEquals(expectedIntent.component, actualIntent.component)

    }

    fun generateUniqueCredentials(): Pair<String, String>{
        val currentDateTime = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val uniquePassword = "P$currentDateTime"
        val uniqueEmail = "$currentDateTime@example.com"
        return Pair(uniqueEmail, uniquePassword)
    }
}