package com.dscvit.vitty.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UtilFunctions {

    fun openLink(context: Context, url: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Browser not found!", Toast.LENGTH_LONG).show()
        }
    }
}