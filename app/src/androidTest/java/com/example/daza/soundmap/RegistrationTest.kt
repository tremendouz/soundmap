package com.example.daza.soundmap


import android.support.design.widget.TextInputLayout
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.IdlingResource
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage
import android.view.View
import com.example.daza.soundmap.ui.activities.MainActivity
import com.example.daza.soundmap.ui.activities.RegistrationActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*


@RunWith(AndroidJUnit4::class)
@LargeTest
class RegistrationTest {

    val invalidEmailString = "example@example..com"
    val invalidPassword = "123"

    val validEmailString = "example@example.com"
    val validPasswordString = "password123"


    val idlingRegistry = IdlingRegistry.getInstance()


    @Rule
    @JvmField
    val activityRule = IntentsTestRule<RegistrationActivity>(RegistrationActivity::class.java)



    @Before
    fun clearAll() {
        onView(withId(R.id.text_email)).perform(clearText())
        onView(withId(R.id.text_password)).perform(clearText())
        onView(withId(R.id.text_confirm_password)).perform(clearText())
    }


    @Test
    fun shouldShowTextInputError_afterInvalidEmail() {
        val resources = InstrumentationRegistry.getTargetContext().resources
        val emailError = resources.getString(R.string.error_input_email)

        onView(withId(R.id.text_email)).perform(typeText(invalidEmailString))
        onView(withId(R.id.input_layout_reg_email))
                .check(matches(hasTextInputLayoutErrorText(emailError)))
        // alternative way
        //onView(withText(R.string.error_input_email)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldShowNoTextInputError_afterValidEmail() {
        onView(withId(R.id.text_email)).perform(clearText())
        onView(withId(R.id.text_email)).perform(typeText(validEmailString))
        onView(withText(R.string.error_input_email)).check(doesNotExist())
    }

    @Test
    fun shouldEnableButton_afterSuccessfulValidation() {
        onView(withId(R.id.text_email))
                .perform(typeText(validEmailString))
                .perform(closeSoftKeyboard())

        onView(withId(R.id.text_password))
                .perform(typeText(validPasswordString))
                .perform(closeSoftKeyboard())

        onView(withId(R.id.text_confirm_password))
                .perform(typeText(validPasswordString))
                .perform(closeSoftKeyboard())

        onView((withId(R.id.button_register))).check(matches(isEnabled()))
    }

    @Test
    fun shouldDisableButton_afterSuccessfulValidation() {
        onView(withId(R.id.text_email))
                .perform(typeText(invalidEmailString))
                .perform(closeSoftKeyboard())

        onView(withId(R.id.text_password))
                .perform(typeText(validPasswordString))
                .perform(closeSoftKeyboard())

        onView(withId(R.id.text_confirm_password))
                .perform(typeText(validPasswordString))
                .perform(closeSoftKeyboard())

        onView((withId(R.id.button_register))).check(matches(not(isEnabled())))
    }

    @Test
    fun shouldRegisterUser_afterSuccessfulValidation() {
        val (validEmailString, validPasswordString) = generateUniqueCredentials()
        val activityResource = WaitForActivityIdlingResource(MainActivity::class.java.name)

        onView(withId(R.id.text_email))
                .perform(typeText(validEmailString))
                .perform(closeSoftKeyboard())

        onView(withId(R.id.text_password))
                .perform(typeText(validPasswordString))
                .perform(closeSoftKeyboard())

        onView(withId(R.id.text_confirm_password))
                .perform(typeText(validPasswordString))
                .perform(closeSoftKeyboard())


        onView(withId(R.id.button_register))
                .perform(click())

        idlingRegistry.register(activityResource)
        intended(hasComponent(MainActivity::class.java.name))
        idlingRegistry.unregister(activityResource)
    }


    fun generateUniqueCredentials(): Pair<String, String>{
        val currentDateTime = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val uniquePassword = "P$currentDateTime"
        val uniqueEmail = "$currentDateTime@example.com"
        return Pair(uniqueEmail, uniquePassword)
    }

    fun hasTextInputLayoutErrorText(errorText: String): Matcher<View> =
            object : TypeSafeMatcher<View>() {
                override fun matchesSafely(view: View?): Boolean {
                    val error = (view as TextInputLayout).error
                    val errorString = error.toString()
                    return errorText == errorString
                }

                override fun describeTo(description: Description?) {
                }
            }


    class WaitForActivityIdlingResource(classToWaitName: String): IdlingResource {
        val instance by lazy { ActivityLifecycleMonitorRegistry.getInstance() }
        val activityToWaitName = classToWaitName
        var resumed = false
        lateinit var resourceCallback: IdlingResource.ResourceCallback

        override fun getName(): String {
            return this.javaClass.name
        }

        override fun isIdleNow(): Boolean {
            resumed = isActivityLaunched()
            if(resumed){
                resourceCallback.onTransitionToIdle()
            }
            return resumed
        }

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
            resourceCallback = callback
        }

        fun isActivityLaunched(): Boolean{
            val activitiesInStage = instance.getActivitiesInStage(Stage.RESUMED)
            for(activity in activitiesInStage){
                if(activity::class.java.name==activityToWaitName){
                    return true
                }
            }
            return false
        }
    }
}