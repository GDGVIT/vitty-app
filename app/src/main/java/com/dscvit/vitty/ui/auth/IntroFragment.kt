package com.dscvit.vitty.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.dscvit.vitty.R
import com.dscvit.vitty.databinding.FragmentIntroBinding

class IntroFragment : Fragment() {

    private lateinit var binding: FragmentIntroBinding
    private var fragID = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_intro,
            container,
            false
        )
        fragID = requireArguments().getString("frag_id")?.toInt()!!
        setupPager()
        return binding.root
    }

    private fun setupPager() {
        binding.apply {
            when (fragID) {
                0 -> {
                    heading.text = requireContext().getString(R.string.intro1_title)
                    subHeading.text = requireContext().getString(R.string.intro1_subtitle)
                    illustration.setImageResource(R.drawable.get_notified)
                }
                1 -> {
                    heading.text = requireContext().getString(R.string.intro2_title)
                    subHeading.text = requireContext().getString(R.string.intro2_subtitle)
                    illustration.setImageResource(R.drawable.widget)
                }
                2 -> {
                    heading.text = requireContext().getString(R.string.intro3_title)
                    subHeading.text = requireContext().getString(R.string.intro3_subtitle)
                    illustration.setImageResource(R.drawable.sync)
                }
            }
        }
    }
}
