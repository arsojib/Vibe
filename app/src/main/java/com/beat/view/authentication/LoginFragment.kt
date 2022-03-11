package com.beat.view.authentication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.beat.R
import com.beat.base.BaseDaggerFragment
import com.beat.core.data.model.LoginTokenResponse
import com.beat.core.data.model.Resource
import com.beat.databinding.LoginFragmentLayoutBinding
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.main.MainActivity
import com.beat.view.dialog.ResetPasswordDialog
import com.beat.view.dialog.WarningDialog
import javax.inject.Inject

class LoginFragment : BaseDaggerFragment() {

    private lateinit var binding: LoginFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.login_fragment_layout, container, false)
        binding.authenticationViewModel =
            ViewModelProvider(this, providerFactory).get(AuthenticationViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.authenticationViewModel!!.getLoginResponse()
            .observe(viewLifecycleOwner, Observer<Resource<LoginTokenResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.authenticationViewModel!!.userOauthSuccess(resource.data!!)
                            startActivity(Intent(mContext, MainActivity::class.java))
                            (context as AppCompatActivity).finish()
                        }
                        Resource.Status.ERROR -> {
                            binding.signIn.isEnabled = true
                            binding.uiFreezer.visibility = View.INVISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                            WarningDialog(
                                mContext,
                                getString(R.string.warning),
                                resource.message!!,
                                getString(android.R.string.ok)
                            )
                        }
                        Resource.Status.LOADING -> {
                            binding.signIn.isEnabled = false
                            binding.uiFreezer.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            })

        binding.signIn.setOnClickListener {
            val isValidMobileNumber =
                binding.authenticationViewModel!!.checkSignInMobileNumberValidation()
            val isValidPassword = binding.authenticationViewModel!!.checkPasswordValidation()
            if (isValidMobileNumber && isValidPassword) {
                hideKeyboard()
                binding.authenticationViewModel!!.loginRequest()
            } else {
                if (!isValidMobileNumber) binding.mobileNumber.error =
                    getString(R.string.please_enter_a_valid_mobile_number)
                if (!isValidPassword) binding.password.error =
                    getString(R.string.password_must_be_at_least_8_characters)
            }
        }

        binding.signUp.setOnClickListener {
            moveToFragmentWithOutBackStack(SignUpFragment.newInstance(), SignUpFragment.toString())
        }

        binding.recoverPassword.setOnClickListener {
            ResetPasswordDialog(this, binding.authenticationViewModel!!)
        }

        binding.uiFreezer.setOnClickListener { }

    }

    private fun hideKeyboard() {
        binding.mobileNumber.clearFocus()
        binding.password.clearFocus()
//        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    private fun clearViewModel() {
        binding.authenticationViewModel!!.signInMobileNumber.value = null
        binding.authenticationViewModel!!.password.value = null
        binding.authenticationViewModel!!.getLoginResponse()
            .removeObservers(viewLifecycleOwner)
    }

    override fun onStop() {
        super.onStop()
//        clearViewModel()
    }

}