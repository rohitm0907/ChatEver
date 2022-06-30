package com.rohit.chitchat.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.rohit.chitchat.*
import com.rohit.chitchat.databinding.FragmentSettingsBinding
import com.suke.widget.SwitchButton
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.android.synthetic.main.dialog_yes_no.*
import java.util.*


class Settings : Fragment() {
    var firebaseOnlineStatus =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_ONLINE_STATUS)

    var firebaseUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)
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

        if (MyUtils.getStringValue(
                requireContext(),
                MyConstants.GHOST_MODE
            ).equals(MyConstants.ON)
        ) {
            binding!!.sbGhost.isChecked = true
        }

        binding!!.btnLogout.setOnClickListener {
            showDialog(resources.getString(R.string.logout));
        }

        binding!!.sbGhost.setOnCheckedChangeListener(SwitchButton.OnCheckedChangeListener { view, isChecked ->

            if (isChecked) {
                firebaseUsers.child(
                    MyUtils.getStringValue(
                        requireContext(),
                        MyConstants.USER_PHONE
                    )
                ).child(MyConstants.GHOST_MODE).setValue(MyConstants.ON).addOnCompleteListener {
                    MyUtils.saveStringValue(
                        requireContext(),
                        MyConstants.GHOST_MODE,
                        MyConstants.ON.toString()
                    )

                }
            } else {
                firebaseUsers.child(
                    MyUtils.getStringValue(
                        requireContext(),
                        MyConstants.USER_PHONE
                    )
                ).child(MyConstants.GHOST_MODE).setValue(MyConstants.OFF).addOnCompleteListener {

                    MyUtils.saveStringValue(
                        requireContext(),
                        MyConstants.GHOST_MODE,
                        MyConstants.OFF.toString()
                    )

                }
            }
        })


        binding!!.imgInfo.setOnClickListener {
            SimpleTooltip.Builder(requireContext())
                .anchorView(binding!!.imgInfo)
                .textColor(resources.getColor(R.color.white))
                .arrowColor(resources.getColor(R.color.black))
                .backgroundColor(resources.getColor(R.color.black))
                .text("By ON Ghost mode, You will not appeared in nearby list when Other users searching")
                .gravity(Gravity.BOTTOM)
                .animated(true)
                .transparentOverlay(false)
                .build()
                .show()

        }


    }


    fun showDialog(message: String) {
        var dialog: Dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_yes_no)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        var txtTitle = dialog.findViewById<TextView>(R.id.txtTitle)
        var btnYes = dialog.findViewById<AppCompatButton>(R.id.btnYes)
        var btnNo = dialog.findViewById<AppCompatButton>(R.id.btnNo)

        txtTitle.text = message

        btnYes.setOnClickListener {
            dialog.dismiss()
            if (!MyUtils.getStringValue(requireActivity(), MyConstants.USER_PHONE).equals("")) {
                firebaseOnlineStatus.child(
                    MyUtils.getStringValue(
                        requireContext(),
                        MyConstants.USER_PHONE
                    )
                ).child(MyConstants.NODE_ONLINE_STATUS)
                    .setValue(Calendar.getInstance().timeInMillis.toString())
            }

            firebaseUsers.child(
                MyUtils.getStringValue(
                    requireContext(),
                    MyConstants.USER_PHONE
                )
            ).child("token").setValue("")

            MyUtils.clearAllData(requireActivity())
            MyUtils.applyFilterType = "No Filter"
            MyUtils.chatNearbyList.clear()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity()!!.finishAffinity()
        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }


    }

}

