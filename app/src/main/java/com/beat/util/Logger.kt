package com.beat.util

import android.util.Log
import com.beat.BuildConfig

object Logger {
    /**
     * A wrapper for the Logger.d function
     *
     *
     * e.g. `
     * Logger.d(TAG, "Batman goes like %d, %d, %d, \"%s\"", 1, 2, 3, "Pow!");
     * //prints: Batman goes like 1, 2, 3, "Pow!"
     * Logger.d(TAG, "Holy Cows Batman!");
     * //prints: Holy Cows Batman!
    ` *
     *
     * @param tag     The tag used in the logcat
     * @param message The message, which can be formatted according to String.format
     * @param args    The arguments, optional
     */
    var logEnable: Boolean = BuildConfig.DEBUG
    fun d(tag: String?, message: String?, vararg args: Any?) {
        if (logEnable) Log.d(tag, message)
    }

    /**
     * A wrapper for the Logger.i function
     *
     *
     * e.g. `
     * Logger.i(TAG, "Batman goes like %d, %d, %d, \"%s\"", 1, 2, 3, "Pow!");
     * //prints: Batman goes like 1, 2, 3, "Pow!"
     * Logger.i(TAG, "Holy Cows Batman!");
     * //prints: Holy Cows Batman!
    ` *
     *
     * @param tag     The tag used in the logcat
     * @param message The message, which can be formatted according to String.format
     * @param args    The arguments, optional
     */
    fun i(tag: String?, message: String?, vararg args: Any?) {
        if (logEnable) Log.i(tag, String.format(message!!, *args))
    }

    /**
     * A wrapper for the Logger.e function
     *
     *
     * e.g.
     * `
     * try {
     * ...
     * throw new Exception("Holy Cow Batman!");
     * } catch(Exception e) {
     * Logger.e("MyTag", e);
     * //prints:
     * // ...  E/MyTag﹕ Holy Cows Batman!
     * //           java.lang.Exception: Holy Cows Batman!
     * //           at ...
     * //           at ...
     * }
    ` *
     *
     * @param tag The tag used in the logcat
     * @param e   The exception
     */
    fun e(tag: String?, e: Exception) {
        if (logEnable) Log.e(tag, e.message, e)
    }

    fun e(tag: String?, message: String?) {
        if (logEnable) Log.e(tag, message!!)
    }

    fun e(tag: String?, message: String?, e: Exception?) {
        if (logEnable) Log.e(tag, message, e)
    }
}
