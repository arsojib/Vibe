package com.beat.base

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.beat.R

abstract class BaseActivity : AppCompatActivity() {

    fun moveToFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, tag).commit()
    }

    fun showToast(message: String, duration: Int) {
        Toast.makeText(this, message, duration).show()
    }

}