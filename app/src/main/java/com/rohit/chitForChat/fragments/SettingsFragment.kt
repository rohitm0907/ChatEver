package com.rohit.chitForChat.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.MyUtils
import com.rohit.chitForChat.ProfileActivity
import com.rohit.chitForChat.R
import com.rohit.chitForChat.databinding.FragmentSettingsBinding


class Settings : Fragment() {
    var binding: FragmentSettingsBinding? = null;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(requireActivity().layoutInflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.btnProfile.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    ProfileActivity::class.java
                ).putExtra(
                    MyConstants.PHONE_NUMBER,
                    MyUtils.getStringValue(requireContext(), MyConstants.USER_PHONE)
                )
            )
        }

        binding!!.btnLogout.setOnClickListener {
            MyUtils.clearAllData(requireActivity())
            requireActivity()!!.finish()
        }

    }


}