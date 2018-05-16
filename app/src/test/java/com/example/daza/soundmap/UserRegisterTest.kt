package com.example.daza.soundmap

import android.content.Intent
import android.widget.Button
import android.widget.EditText
import com.example.daza.soundmap.ui.activities.MainActivity
import com.example.daza.soundmap.ui.activities.RegistrationActivity
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
class UserRegisterTest {
    @Test
    fun shouldRegisterUser() {
        val activity = Robolectric.setupActivity(RegistrationActivity::class.java)

        val emailText = activity.findViewById<EditText>(R.id.text_email)
                .setText("example@example.com")

        val passwordText = activity.findViewById<EditText>(R.id.text_password)
                .setText("password123")
        val confirmPasswordText = activity.findViewById<EditText>(R.id.text_confirm_password)
                .setText("password123")


        val signupButton = activity.findViewById<Button>(R.id.button_register)

        Assert.assertNotNull(signupButton)
        assert(signupButton.isEnabled)


        signupButton.performClick()


        Assert.assertNotNull(activity.firebaseAuth.currentUser)

        val actualIntent = Shadows.shadowOf(RuntimeEnvironment.application).nextStartedActivity
        val expectedIntent = Intent(activity, MainActivity::class.java)
        Assert.assertEquals(expectedIntent.component, actualIntent.component)

    }
}