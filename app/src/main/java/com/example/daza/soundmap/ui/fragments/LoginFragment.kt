package com.example.daza.soundmap.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.daza.soundmap.R
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [LoginFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    val TAG = SignupFragment::class.java.simpleName
    val DEBOUNCE_TIME = 0L

    lateinit var btnLogin: Button
    lateinit var btnGoToRegister: Button
    lateinit var btnForgotPassword: Button
    lateinit var textEmail: EditText
    lateinit var textPassword: EditText
    lateinit var inputEmail: TextInputLayout
    lateinit var inputPassword: TextInputLayout


    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: SignupFragment.OnChangeFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        btnLogin = view.findViewById(R.id.button_login)
        btnGoToRegister = view.findViewById(R.id.button_registration)
        btnForgotPassword = view.findViewById(R.id.button_forgot_password)
        inputEmail = view.findViewById(R.id.input_layout_email)
        inputPassword = view.findViewById(R.id.input_layout_password)
        textEmail = view.findViewById(R.id.text_email)
        textPassword = view.findViewById(R.id.text_password)

        btnGoToRegister.setOnClickListener { mListener?.changeFragment(this) }
        btnLogin.setOnClickListener { mListener?.loginUser(textEmail.text.toString(), textPassword.text.toString()) }
        btnForgotPassword.setOnClickListener { showForgotPasswordDialog() }

        setUpFormValidation()

        return view
    }

    fun isValidEmail(email: String): Boolean = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPassword(password: String): Boolean = password.isNotEmpty() && password.length > 6

    fun isValidForm(boolean: Boolean) {
        btnLogin.isEnabled = boolean
    }

    fun validateInput(valid: Boolean, textInputLayout: TextInputLayout, button: Button? = null){
        if(textInputLayout.editText!!.text.isEmpty() && !valid){
            textInputLayout.isErrorEnabled = false
            button?.isEnabled = false
        }
        else{
            val errorMsg = when(textInputLayout){
                inputEmail -> resources.getString(R.string.error_input_email)
                inputPassword -> resources.getString(R.string.error_input_password)
                else -> "Invalid"
            }
            textInputLayout.error = errorMsg
            textInputLayout.isErrorEnabled = !valid
            button?.isEnabled = valid
        }
    }


    fun showForgotPasswordDialog() {
        val view = LayoutInflater.from(activity).inflate(R.layout.alert_dialog_password_restore, null)
        val positiveButton = view.findViewById<Button>(R.id.btn_alert_positive)
        val negativeButton = view.findViewById<Button>(R.id.btn_alert_negative)
        val alert_text_input_email = view.findViewById<TextInputLayout>(R.id.alert_input_layout_email)
        val alert_text_email = view.findViewById<TextInputEditText>(R.id.alert_text_email)
        val alertEmailObservable = RxTextView.textChanges(alert_text_email)

        val restorePasswordAlert = AlertDialog.Builder(activity)
                .setTitle(R.string.alert_dialog_rp_tile)
                .setMessage(R.string.alert_dialog_rp_message)
                .setView(view)
                .setCancelable(false)
                .show()

        alertEmailObservable.map { text -> isValidEmail(text.toString()) }
                .debounce(DEBOUNCE_TIME, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { isValid -> validateInput(isValid, alert_text_input_email, positiveButton) }

        positiveButton.setOnClickListener {
            mListener?.restorePasword(alert_text_email.text.toString())
            restorePasswordAlert.cancel()
        }
        negativeButton.setOnClickListener {
            restorePasswordAlert.cancel()
        }
    }

    fun setUpFormValidation() {
        val emailObservable = RxTextView.textChanges(textEmail)
        emailObservable.map { text -> isValidEmail(text.toString()) }
                .skip(1)
                .debounce(DEBOUNCE_TIME, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { isValid -> validateInput(isValid, inputEmail) }

        val passwordObservable = RxTextView.textChanges(textPassword)
        passwordObservable.map { text -> isValidPassword(text.toString()) }
                .skip(1)
                .debounce(DEBOUNCE_TIME, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { isValid -> validateInput(isValid, inputPassword) }


        val combinedObservable: Observable<Boolean> = Observable.combineLatest(
                emailObservable,
                passwordObservable,
                BiFunction { email, pass ->
                    isValidEmail(email.toString()) &&
                            isValidPassword(pass.toString())

                })
        combinedObservable.subscribe { isValid: Boolean -> isValidForm(isValid) }

    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.changeFragment(this)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SignupFragment.OnChangeFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): LoginFragment {
            val fragment = LoginFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
