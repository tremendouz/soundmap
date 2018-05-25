package com.example.daza.soundmap.ui.fragments

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_signup.view.*
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.alert_dialog_password_restore.view.*
import org.jetbrains.anko.design.textInputEditText
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SignupFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SignupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class SignupFragment : Fragment() {
    val TAG = SignupFragment::class.java.simpleName
    val DEBOUNCE_TIME = 0L



    lateinit var btnRegister: Button
    lateinit var btnBackToLogin: Button
    lateinit var textEmail: EditText
    lateinit var textPassword: EditText
    lateinit var textConfirmPassword: EditText
    lateinit var inputEmail: TextInputLayout
    lateinit var inputPassword: TextInputLayout
    lateinit var inputConfirmPassword: TextInputLayout


    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnChangeFragmentListener? = null

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
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        btnRegister = view.findViewById(R.id.button_signup)
        btnBackToLogin = view.findViewById(R.id.button_back_to_login)
        textEmail = view.findViewById(R.id.text_email)
        textPassword = view.findViewById(R.id.text_password)
        textConfirmPassword = view.findViewById(R.id.text_confirm_password)
        inputEmail = view.findViewById(R.id.input_layout_reg_email)
        inputPassword = view.findViewById(R.id.input_layout_reg_password)
        inputConfirmPassword = view.findViewById(R.id.input_layout_reg_confirm_password)

        btnBackToLogin.setOnClickListener {
            mListener?.changeFragment(this)
        }

        btnRegister.isEnabled = false

        btnRegister.setOnClickListener {
            mListener?.registerUser(textEmail.text.toString(), textPassword.text.toString())
        }

        setUpFormValidation()


        return view
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

    fun isValidEmail(email: String): Boolean = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPassword(password: String): Boolean = password.isNotEmpty() && password.length > 6

    fun isValidConfirmPassword(password: String): Boolean = password == textPassword.text.toString()

    fun isValidForm(boolean: Boolean) {
        btnRegister.isEnabled = boolean
    }

    fun validateInput(valid: Boolean, textInputLayout: TextInputLayout){
        if(textInputLayout.editText!!.text.isEmpty() && !valid){
            textInputLayout.isErrorEnabled = false
        }
        else{
            val errorMsg = when(textInputLayout){
                inputEmail -> resources.getString(R.string.error_input_email)
                inputPassword -> resources.getString(R.string.error_input_password)
                inputConfirmPassword -> resources.getString(R.string.error_input_confirm_password)
                else -> "Invalid"
            }
            textInputLayout.error = errorMsg
            textInputLayout.isErrorEnabled = !valid
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

        val confirmPasswordObservable = RxTextView.textChanges(textConfirmPassword)
        confirmPasswordObservable.map { text -> isValidConfirmPassword(text.toString()) }
                .skip(1)
                .debounce(DEBOUNCE_TIME, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { isValid -> validateInput(isValid, inputConfirmPassword) }


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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnChangeFragmentListener {
        fun changeFragment(fragment: Fragment) {
        }
        fun registerUser(email: String, password: String){
        }
        fun loginUser(email: String, password: String)
        fun restorePasword(email: String)
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
         * @return A new instance of fragment SignupFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): SignupFragment {
            val fragment = SignupFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
