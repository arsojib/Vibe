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
import com.beat.core.data.model.Resource
import com.beat.databinding.SignUpFragmentLayoutBinding
import com.beat.util.listener.ClickListener
import com.beat.util.viewModelFactory.ViewModelProviderFactory
import com.beat.view.main.MainActivity
import com.beat.view.dialog.SignUpDialog
import com.beat.view.dialog.WarningDialog
import javax.inject.Inject

class SignUpFragment : BaseDaggerFragment() {

    private lateinit var binding: SignUpFragmentLayoutBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    companion object {
        fun newInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.sign_up_fragment_layout, container, false)
        binding.authenticationViewModel =
            ViewModelProvider(this, providerFactory).get(AuthenticationViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialComponent()
    }

    private fun initialComponent() {
        binding.authenticationViewModel!!.getAddNewUserResponse()
            .observe(viewLifecycleOwner, Observer<Resource<String>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.signUp.isEnabled = true
                            binding.uiFreezer.visibility = View.INVISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                            SignUpDialog(
                                this,
                                binding.authenticationViewModel!!,
                                object : ClickListener {
                                    override fun onClick(position: Int) {
                                        startActivity(Intent(mContext, MainActivity::class.java))
                                        (context as AppCompatActivity).finish()
                                    }
                                })
                        }
                        Resource.Status.ERROR -> {
                            binding.signUp.isEnabled = true
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
                            binding.signUp.isEnabled = false
                            binding.uiFreezer.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            })

        binding.signUp.setOnClickListener {
            val isValidMobileNumber =
                binding.authenticationViewModel!!.checkSignUpMobileNumberValidation()
            if (isValidMobileNumber) {
                hideKeyboard()
                //Temporary holding sign up functionality
                binding.authenticationViewModel!!.addNewUserRequest()
            } else {
                if (!isValidMobileNumber) binding.mobileNumber.error =
                    getString(R.string.please_enter_a_valid_mobile_number)
            }
        }

        binding.signIn.setOnClickListener {
            moveToFragmentWithOutBackStack(LoginFragment.newInstance(), LoginFragment.toString())
        }

    }

    private fun hideKeyboard() {
        binding.mobileNumber.clearFocus()
//        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    private fun clearViewModel() {
        binding.authenticationViewModel!!.signUpMobileNumber.value = null
        binding.authenticationViewModel!!.getAddNewUserResponse()
            .removeObservers(viewLifecycleOwner)
    }

    override fun onStop() {
        super.onStop()
//        clearViewModel()
    }
}