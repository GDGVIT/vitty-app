package com.dscvit.vitty.ui.instructions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
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
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(context?.getString(R.string.instructions_1_link))
                            )
                        )
                    } catch (e: Exception) {
                        Toast.makeText(context, "Browser not found!", Toast.LENGTH_LONG).show()
                    }
                }
                setOnLongClickListener {
                    Toast.makeText(
                        context,
                        "Link Copied",
                        Toast.LENGTH_LONG
                    ).show()
                    val clipboard: ClipboardManager? =
                        context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText(
                        "EXTENSION-LINK",
                        context?.getString(R.string.instructions_1_link)
                    )
                    clipboard?.setPrimaryClip(clip)
                    true
                }
            }
        }
    }
}
