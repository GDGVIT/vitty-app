package com.dscvit.vitty.ui.instructions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.FragmentInstructionsBinding
import com.dscvit.vitty.model.UserDetails
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class InstructionsFragment : Fragment() {
    private lateinit var binding: FragmentInstructionsBinding
    private lateinit var sharedPref: SharedPreferences
    private val firebaseUser: FirebaseUser = Firebase.auth.currentUser!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_instructions,
            container,
            false
        )
        pageSetup()
        return binding.root
    }

    private fun pageSetup() {
        sharedPref = activity?.getSharedPreferences("login_info", Context.MODE_PRIVATE)!!
        val name = firebaseUser.displayName
        val email = firebaseUser.email
        val photoUrl = firebaseUser.photoUrl
        val token = sharedPref.getString("token", "")

        binding.apply {
            userDetails = UserDetails(name, email, photoUrl, token, "Google")
            profilePic.load(photoUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
            instructions1Link.apply {
                setOnClickListener {
                    openLink(context.getString(R.string.instructions_1_link))
                }
                setOnLongClickListener {
                    copyLink(
                        "GDSC_WEBSITE_LINK",
                        context.getString(R.string.instructions_1_link)
                    )
                    true
                }
            }
            telegramIssueLink.apply {
                setOnClickListener {
                    openLink(context.getString(R.string.telegram_link))
                }
                setOnLongClickListener {
                    copyLink(
                        "GDSC_TELEGRAM_LINK",
                        context.getString(R.string.telegram_link)
                    )
                    true
                }
            }
        }
    }

    private fun openLink(url: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Browser not found!", Toast.LENGTH_LONG).show()
        }
    }

    private fun copyLink(label: String, url: String) {
        Toast.makeText(
            context,
            "Link Copied",
            Toast.LENGTH_LONG
        ).show()
        val clipboard: ClipboardManager? =
            context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(label, url)
        clipboard?.setPrimaryClip(clip)
    }
}
