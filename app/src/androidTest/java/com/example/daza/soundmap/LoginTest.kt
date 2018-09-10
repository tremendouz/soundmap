package com.example.daza.soundmap

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.example.daza.soundmap.ui.activities.AuthActivity
import com.example.daza.soundmap.ui.activities.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest {

    val VALID_EMAIL = "test@test.com"
    val VALID_PASSWORD = "testtest"
    val idlingRegistry = IdlingRegistry.getInstance()


    @Rule
    @JvmField
    val activityRule = IntentsTestRule<AuthActivity>(AuthActivity::class.java)

    @Test
    fun shouldLogin_withValidCredentials(){
        val activityResource = RegistrationTest.WaitForActivityIdlingResource(MainActivity::class.java.name)

        onView(withId(R.id.text_email))
                .perform(typeText(VALID_EMAIL))
                .perform(closeSoftKeyboard())

        onView(withId(R.id.text_password))
                .perform(typeText(VALID_PASSWORD))
                .perform(closeSoftKeyboard())

        onView((withId(R.id.button_login)))
                .perform(click())

        idlingRegistry.register(activityResource)
        intended(hasComponent(MainActivity::class.java.name))
        idlingRegistry.unregister(activityResource)
    }


}