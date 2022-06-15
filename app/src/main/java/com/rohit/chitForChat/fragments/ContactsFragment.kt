package com.rohit.chitForChat.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rohit.chitForChat.Models.ContactModel
import com.rohit.chitForChat.Models.Users
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.MyUtils
import com.rohit.chitForChat.MyUtils.listAllUsers
import com.rohit.chitForChat.R
import com.rohit.chitForChat.adapters.ContactsAdapter
import com.rohit.chitForChat.adapters.NearbyChatAdapter
import com.rohit.chitForChat.databinding.FragmentChatsBinding
import com.rohit.chitForChat.databinding.FragmentContactsBinding
import kotlinx.android.synthetic.main.activity_login.*


class ContactsFragment : Fragment() {
    var firebaseUsers = FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
        .getReference(MyConstants.NODE_USERS)
var listContacts:ArrayList<ContactModel> = ArrayList()
var binding:FragmentContactsBinding?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
      binding= FragmentContactsBinding.inflate(inflater, container, false)
    return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getContacts()
        if(MyUtils.listAllUsersNumbers.size>0){
            getContacts()
        }else{
            fetchAllUsers()
        }
    }
    fun fetchAllUsers(){
        firebaseUsers.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
//                MyUtils.stopProgress(requireActivity())
            if (snapshot.exists()) {
                MyUtils.listAllUsersNumbers.clear()
                for (postSnapshot in snapshot.children) {
                    val user: Users? =
                        postSnapshot.getValue(Users::class.java)
                    listAllUsers.add(user!!)
                    MyUtils.listAllUsersNumbers.add(user!!.phone.toString())
                }

                getContacts()
            } else {
              getContacts()
            }
        }

        override fun onCancelled(error: DatabaseError) {
         Toast.makeText(requireContext(),"Something went wrong, Please try again later",Toast.LENGTH_SHORT).show()
        }

    })
}

private fun setAdapter() {
        binding!!.recyclerChatList.adapter =
            ContactsAdapter(requireActivity(), listContacts)
    binding!!.recyclerChatList.setItemViewCacheSize(listContacts.size                                                                       )
    }

    @SuppressLint("Range")
    fun getContacts() {
        listContacts.clear()
        val phones: Cursor? = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (phones!!.moveToNext()) {
            val name: String =
                phones!!.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber: String =
                phones!!.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            var contact=ContactModel()
            contact.name=name
            contact.mobileNumber=phoneNumber.replace(" ","")
            if(!listContacts.contains(contact)){
                listContacts.add(contact)
            }

        }
        listContacts.sortBy { contactModel ->
            contactModel.name
        }
        setAdapter()
        phones.close()
    }
}