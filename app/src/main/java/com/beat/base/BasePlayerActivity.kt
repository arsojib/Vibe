package com.beat.base

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.beat.R
import com.beat.util.isPlayerInitialed
import com.beat.util.isRadioInitialed
import dagger.android.support.DaggerAppCompatActivity
import org.greenrobot.eventbus.EventBus

abstract class BasePlayerActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    fun moveToFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, tag).commit()
    }

    fun showToast(message: String, duration: Int) {
        Toast.makeText(this, message, duration).show()
    }

    protected fun setPlayerInitialState(initialed: Boolean) {
        isPlayerInitialed = initialed
    }

    protected fun setRadioInitialState(initialed: Boolean) {
        isRadioInitialed = initialed
    }

    protected fun getPlayerInitialState() = isPlayerInitialed

    protected fun getRadioInitialState() = isRadioInitialed

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}