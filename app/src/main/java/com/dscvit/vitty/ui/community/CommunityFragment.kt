package com.dscvit.vitty.ui.community

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.FragmentCommunityBinding
import com.dscvit.vitty.util.Constants

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private lateinit var prefs: SharedPreferences


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val communityViewModel =
            ViewModelProvider(this)[CommunityViewModel::class.java]

        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        prefs = requireContext().getSharedPreferences(Constants.USER_INFO, 0)

        binding.communityToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.requests -> {
                    requireView().findNavController()
                        .navigate(R.id.action_navigation_community_to_navigation_requests)
                    true
                }
                else -> {
                    false
                }
            }
        }

        if (!prefs.getBoolean("KRISH", false)) {
            binding.krish.visibility = View.GONE
        } else {
            binding.krish.visibility = View.VISIBLE
        }

        binding.krish.setOnLongClickListener {
            prefs.edit().putBoolean("KRISH", false).apply()
            binding.krish.visibility = View.GONE
            true
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}