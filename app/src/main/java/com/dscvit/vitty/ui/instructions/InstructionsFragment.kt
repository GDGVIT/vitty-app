package com.dscvit.vitty.ui.instructions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.FragmentInstructionsBinding
import com.dscvit.vitty.model.UserDetails

class InstructionsFragment : Fragment() {
    private lateinit var binding: FragmentInstructionsBinding
    private lateinit var sharedPref: SharedPreferences

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
        val name = sharedPref.getString("name", "")
        val email = sharedPref.getString("email", "")
        val token = sharedPref.getString("token", "")
        binding.userDetails = UserDetails(name, email, token)
        binding.instructions1Link.setOnClickListener {
            Toast.makeText(
                context,
                "Link Copied!\nTip: Long press to share the link.",
                Toast.LENGTH_LONG
            ).show()
            val clipboard: ClipboardManager? =
                context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText(
                "EXTENSION-LINK",
                context?.getString(R.string.instructions_1_link)
            )
            clipboard?.setPrimaryClip(clip)
        }
        binding.instructions1Link.setOnLongClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
            }
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                context?.getString(R.string.share_text)
            )
            startActivity(Intent.createChooser(shareIntent, "send to"))
            true
        }
    }
}
