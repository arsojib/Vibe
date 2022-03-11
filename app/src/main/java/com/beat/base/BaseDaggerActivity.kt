package com.beat.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.beat.R
import dagger.android.support.DaggerAppCompatActivity

abstract class BaseDaggerActivity: DaggerAppCompatActivity() {

    fun moveToFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, tag).commit()
    }

    fun showToast(message: String, duration: Int) {
        Toast.makeText(this, message, duration).show()
    }

}