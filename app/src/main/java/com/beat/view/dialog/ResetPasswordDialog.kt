package com.beat.view.dialog

import android.app.AlertDialog
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.beat.R
import com.beat.core.data.model.Event
import com.beat.core.data.model.Resource
import com.beat.databinding.ResetPasswordLayoutBinding
import com.beat.view.authentication.AuthenticationViewModel

class ResetPasswordDialog constructor(
    private val fragment: Fragment,
    private val authenticationViewModel: AuthenticationViewModel
) {

    private lateinit var dialog: AlertDialog
    private lateinit var binding: ResetPasswordLayoutBinding
    private var countDownTimer: CountDownTimer? = null

    init {
        initialDialog()
    }

    private fun initialDialog() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(fragment.context),
            R.layout.reset_password_layout,
            null,
            false
        )
        binding.authenticationViewModel = authenticationViewModel
        val builder: AlertDialog.Builder = AlertDialog.Builder(fragment.context)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.setCancelable(false)

        binding.authenticationViewModel!!.getInitiateResetPasswordResponse()
            .observe(fragment.viewLifecycleOwner, Observer<Event<Resource<String>>> {
                it?.let { resource ->
                    when (resource.getContentIfNotHandled()?.status) {
                        Resource.Status.SUCCESS -> {
                            binding.uiFreezer.visibility = View.INVISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.confirmMobileNumberLayout.visibility = View.GONE
                            binding.setPasswordLayout.visibility = View.VISIBLE
                            enableTimer()
                        }
                        Resource.Status.ERROR -> {
                            binding.uiFreezer.visibility = View.INVISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                            Toast.makeText(
                                fragment.context,
                                fragment.getText(R.string.something_went_wrong_please_try_again),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Resource.Status.LOADING -> {
                            binding.uiFreezer.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            })

        binding.authenticationViewModel!!.getResetPasswordResponse()
            .observe(fragment.viewLifecycleOwner, Observer<Event<Resource<String>>> {
                it?.let { resource ->
                    when (resource.getContentIfNotHandled()?.status) {
                        Resource.Status.SUCCESS -> {
                            binding.uiFreezer.visibility = View.INVISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                            Toast.makeText(
                                fragment.context,
                                fragment.getString(R.string.your_password_has_been_reset_successfully_please_login_to_continue),
                                Toast.LENGTH_LONG
                            ).show()
                            dismissDialog()
                        }
                        Resource.Status.ERROR -> {
                            binding.uiFreezer.visibility = View.INVISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                            Toast.makeText(
                                fragment.context,
                                resource.getContentIfNotHandled()?.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Resource.Status.LOADING -> {
                            binding.uiFreezer.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            })

        binding.confirmMobileNumber.setOnClickListener {
            val isValidMobileNumber =
                binding.authenticationViewModel!!.checkResetMobileNumberValidation()
            if (isValidMobileNumber) {
                hideKeyboard(binding.mobileNumber)
                binding.authenticationViewModel!!.initiateResetPasswordRequest()
            } else {
                binding.mobileNumber.error =
                    fragment.getString(R.string.please_enter_a_valid_mobile_number)
            }
        }

        binding.savePassword.setOnClickListener {
            val isValidCode = binding.authenticationViewModel!!.checkCodeValidation()
            val isValidNewPassword = binding.authenticationViewModel!!.checkNewPasswordValidation()
            if (isValidCode && isValidNewPassword) {
                hideKeyboard(binding.code)
                hideKeyboard(binding.newPassword)
                binding.authenticationViewModel!!.resetPasswordRequest()
            } else {
                if (!isValidCode) binding.code.error =
                    fragment.getString(R.string.code_must_be_at_least_4_characters)
                if (!isValidNewPassword) binding.newPassword.error =
                    fragment.getString(R.string.password_must_be_at_least_8_characters)
            }
        }

        binding.resend.setOnClickListener {
            binding.authenticationViewModel!!.initiateResetPasswordRequest()
        }

        binding.cancelMobileNumber.setOnClickListener {
            dismissDialog()
        }

        binding.cancelPassword.setOnClickListener {
            dismissDialog()
        }

        binding.uiFreezer.setOnClickListener { }

        dialog.show()
    }

    private fun enableTimer() {
        countDownTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.waitText.text = fragment.getString(
                    R.string.please_wait_format,
                    (millisUntilFinished / 1000).toString() + "s"
                )
            }

            override fun onFinish() {
                binding.waitText.text =
                    fragment.getString(R.string.please_wait_format, 0.toString() + "s")
                binding.resend.visibility = View.VISIBLE
                binding.waitText.visibility = View.GONE
            }
        }.start()
        binding.dontReceiveCodeLayout.visibility = View.VISIBLE
        binding.resend.visibility = View.INVISIBLE
        binding.waitText.visibility = View.VISIBLE
    }

    private fun hideKeyboard(editText: EditText) {
        editText.clearFocus()
    }

    private fun clearViewModel() {
        binding.authenticationViewModel!!.resetMobileNumber.value = null
        binding.authenticationViewModel!!.code.value = null
        binding.authenticationViewModel!!.newPassword.value = null
        binding.authenticationViewModel!!.getInitiateResetPasswordResponse()
            .removeObservers(fragment.viewLifecycleOwner)
        binding.authenticationViewModel!!.getResetPasswordResponse()
            .removeObservers(fragment.viewLifecycleOwner)
    }

    private fun dismissDialog() {
        clearViewModel()
        countDownTimer?.cancel()
        dialog.dismiss()
    }

}