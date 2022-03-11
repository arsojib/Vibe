package com.beat.view.dialog

import android.content.Context
import android.text.Html
import androidx.appcompat.app.AlertDialog

class WarningDialog constructor(
    private val context: Context,
    private val title: String,
    private val body: String,
    private val btnText: String
) {

    init {
        initialComponent()
    }

    private fun initialComponent() {
        AlertDialog.Builder(context)
            .setTitle(Html.fromHtml("<font color='#F26522'>$title</font>"))
            .setMessage(body)
            .setPositiveButton(
                btnText
            ) { dialog, which ->
                dialog.dismiss()
            }.show()
    }

}