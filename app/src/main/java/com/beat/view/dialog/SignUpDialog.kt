package com.beat.view.dialog

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.beat.R
import com.beat.core.data.model.LoginTokenResponse
import com.beat.core.data.model.Resource
import com.beat.databinding.SignUpLayoutBinding
import com.beat.util.listener.ClickListener
import com.beat.view.authentication.AuthenticationViewModel

class SignUpDialog constructor(
    private val fragment: Fragment,
    private val authenticationViewModel: AuthenticationViewModel,
    private val clickListener: ClickListener
) {

    private lateinit var dialog: AlertDialog

    private lateinit var binding: SignUpLayoutBinding

    init {
        initialDialog()
    }

    private fun initialDialog() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(fragment.context),
            R.layout.sign_up_layout,
            null,
            false
        )
        binding.authenticationViewModel = authenticationViewModel
        val builder: AlertDialog.Builder = AlertDialog.Builder(fragment.context)
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.setCancelable(false)

        binding.authenticationViewModel!!.getSignUpResponse()
            .observe(fragment.viewLifecycleOwner, Observer<Resource<LoginTokenResponse>> {
                it?.let { resource ->
                    when (resource.status) {
                        Resource.Status.SUCCESS -> {
                            binding.codeLayout.visibility = View.GONE
                            binding.thankLayout.visibility = View.VISIBLE
                            binding.authenticationViewModel!!.userOauthSuccess(resource.data!!)
                        }
                        Resource.Status.ERROR -> {
                            binding.uiFreezer.isEnabled = true
                            binding.progressBar.visibility = View.INVISIBLE
                            Toast.makeText(fragment.context, resource.message, Toast.LENGTH_LONG)
                                .show()
                        }
                        Resource.Status.LOADING -> {
                            binding.uiFreezer.isEnabled = false
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }
                }
            })

        binding.confirm.setOnClickListener {
            val isValidCode = binding.authenticationViewModel!!.checkSignUpCodeValidation()
            if (isValidCode) {
                hideKeyboard(binding.code)
                binding.authenticationViewModel!!.signUpRequest()
            } else {
                if (!isValidCode) binding.code.error =
                    fragment.getString(R.string.code_must_be_at_least_4_characters)
            }
        }

        binding.continueTo.setOnClickListener {
            dialog.dismiss()
            clickListener.onClick(-1)
        }

        dialog.show()
    }

    private fun hideKeyboard(editText: EditText) {
        editText.clearFocus()
    }

}