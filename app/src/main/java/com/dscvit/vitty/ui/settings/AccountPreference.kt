package com.dscvit.vitty.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import coil.load
import com.dscvit.vitty.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AccountPreference(context: Context?, attrs: AttributeSet?) : Preference(context, attrs) {
    private val firebaseUser: FirebaseUser = Firebase.auth.currentUser!!

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        holder?.itemView?.findViewById<TextView>(R.id.title)?.text = firebaseUser.displayName
        holder?.itemView?.findViewById<TextView>(R.id.summary)?.text = firebaseUser.email
        holder?.itemView?.findViewById<ImageView>(R.id.icon)?.load(firebaseUser.photoUrl)
//        holder?.itemView?.setOnClickListener {
//            LogoutHelper.logout(
//                context,
//                context as Activity,
//                context.getSharedPreferences(Constants.USER_INFO, 0)
//            )
//        }
    }

    init {
        this.widgetLayoutResource = R.layout.layout_account_details
    }
}
