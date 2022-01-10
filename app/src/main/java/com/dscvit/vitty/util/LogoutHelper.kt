package com.dscvit.vitty.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import com.dscvit.vitty.R
import com.dscvit.vitty.activity.AuthActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

object LogoutHelper {

    fun logout(context: Context, activity: Activity, prefs: SharedPreferences) {
        val v: View = LayoutInflater
            .from(context)
            .inflate(R.layout.dialog_logout, null)

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(v)
            .setBackground(
                AppCompatResources.getDrawable(
                    context,
                    R.color.transparent
                )
            )
            .create()

        dialog.show()

        val cancel = v.findViewById<Button>(R.id.cancel)
        val logout = v.findViewById<Button>(R.id.logout)

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        logout.setOnClickListener {
            prefs.edit().apply {
                putInt(Constants.TIMETABLE_AVAILABLE, 0)
                putInt(Constants.UPDATE, 0)
                putString(Constants.UID, "")
                putBoolean(Constants.FIRST_TIME_SETUP, false)
                apply()
            }
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            activity.finish()
        }
    }
}
