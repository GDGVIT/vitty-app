package com.dscvit.vitty.ui.instructions

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }
}
