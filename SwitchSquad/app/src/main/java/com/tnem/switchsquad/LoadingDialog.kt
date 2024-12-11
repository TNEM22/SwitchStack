package com.tnem.switchsquad

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.widget.TextView


class LoadingDialog(var activity: Activity) {
    var dialog: AlertDialog? = null

    fun startLoadingDialog(x: Int) {
        val builder = AlertDialog.Builder(activity)

        val inflater = activity.layoutInflater
        val view: View = inflater.inflate(R.layout.custom_dialog, null)
        if (x == 0)
            view.findViewById<TextView>(R.id.loadingTextView).setText("Connecting...")
        else if (x == 1)
            view.findViewById<TextView>(R.id.loadingTextView).setText("Getting Switches...")
        builder.setView(view)
        builder.setCancelable(false)

        dialog = builder.create()
        dialog!!.show()
    }

    fun dismissDialog() {
        dialog!!.dismiss()
    }
}