package com.dscvit.vitty.ui.community

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.FragmentRequestsBinding
import com.dscvit.vitty.util.Constants

class RequestsFragment : Fragment() {

    private var _binding: FragmentRequestsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var prefs: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val communityViewModel =
            ViewModelProvider(this)[CommunityViewModel::class.java]

        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)

        binding.scheduleToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.close -> {
                    requireActivity().onBackPressed()
                    true
                }
                else -> {
                    false
                }
            }
        }

        binding.logout.setOnClickListener {
            prefs.edit().putBoolean("KRISH", true).apply()
            binding.reqKrish.visibility = View.GONE
        }

        if (prefs.getBoolean("KRISH", false)) {
            binding.reqKrish.visibility = View.GONE
        } else {
            binding.reqKrish.visibility = View.VISIBLE
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}