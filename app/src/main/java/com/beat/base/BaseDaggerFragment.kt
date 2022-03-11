package com.beat.base

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.androidadvance.topsnackbar.TSnackbar
import com.beat.R
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment

abstract class BaseDaggerFragment : DaggerFragment() {

    lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    protected open fun moveToFragmentWithBackStack(
        fragment: Fragment?,
        tag: String?,
        backStackTag: String?
    ) {
        (context as AppCompatActivity).supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.container, fragment!!, tag)
            .addToBackStack(backStackTag)
            .commit()
    }

    protected open fun moveToFragmentWithOutBackStack(
        fragment: Fragment?,
        tag: String?
    ) {
        val fm = activity!!.supportFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
        (context as AppCompatActivity).supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.container, fragment!!, tag)
            .commit()
    }

    open fun onNavBackClick() {
        (context as AppCompatActivity).onBackPressed()
    }

    open fun showTSnackBar(parentLayout: View?, message: String?) {
        if (parentLayout != null) {
            val snackBar = TSnackbar.make(parentLayout, message!!, TSnackbar.LENGTH_LONG)
            snackBar.setActionTextColor(Color.WHITE)
            snackBar.view.setBackgroundColor(ContextCompat.getColor(parentLayout.context, R.color.colorPrimary))
            val textView =
                snackBar.view.findViewById<TextView>(com.androidadvance.topsnackbar.R.id.snackbar_text)
            textView.setTextColor(Color.WHITE)
            snackBar.show()
        }
    }

    open fun showSnackBar(parentLayout: View?, message: String) {
        if (parentLayout != null) {
            val snackBar = Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG)
            snackBar.view.setBackgroundColor(ContextCompat.getColor(parentLayout.context, R.color.colorRed))
            snackBar.setActionTextColor(ContextCompat.getColor(parentLayout.context, R.color.colorAccent))
                .show()
        }
    }

    fun showToast(message: String, duration: Int) {
        Toast.makeText(mContext, message, duration).show()
    }

}