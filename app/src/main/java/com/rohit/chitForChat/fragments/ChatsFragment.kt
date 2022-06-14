package com.rohit.chitForChat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rohit.chitForChat.Models.ChatFriendsModel
import com.rohit.chitForChat.Models.LiveChatModel
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.MyUtils
import com.rohit.chitForChat.adapters.ChatListAdapter
import com.rohit.chitForChat.adapters.ChatLiveAdapter
import com.rohit.chitForChat.databinding.FragmentChatsBinding
import android.app.Activity
import com.google.android.material.tabs.TabLayout
import com.rohit.chitForChat.MyUtils.listFriends
import com.rohit.chitForChat.R
import java.util.*
import kotlin.collections.ArrayList

class ChatsFragment : Fragment() {
    var firebaseChatFriends =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHAT_FIRENDS)
var countUnreadMessages=0
    lateinit var binding: FragmentChatsBinding;
    var chatFriendList: ArrayList<ChatFriendsModel> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getChatsFromFirebase()
    }

    private fun getChatsFromFirebase() {
        firebaseChatFriends.child(MyUtils.getStringValue(requireActivity(), MyConstants.USER_PHONE))
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        chatFriendList.clear()

                        countUnreadMessages=0
                        for (postSnapshot in snapshot.children) {
                            val user: ChatFriendsModel? =
                                postSnapshot.getValue(ChatFriendsModel::class.java)
                            if(user!!.seenStatus.equals("0")){
                                countUnreadMessages++
                            }
                            chatFriendList.add(user!!)

                            // for check in nearby list, user is already friend or not
                            listFriends.add(user.userId!!)
                            // here you can access to name property like university.name
                        }

                        chatFriendList.sortByDescending {chatFriendsModel ->
                            chatFriendsModel.time!!.toDouble()
                        }


                        if (activity != null) {
                            binding!!.recyclerChatList.adapter =
                                ChatListAdapter(requireActivity(), chatFriendList!!)
                            setUnreadMessages()
                        }
                    }else{
                        chatFriendList.clear()
                        val activity: Activity? = activity
                        if (activity != null) {
                            binding!!.recyclerChatList.adapter = ChatListAdapter(requireActivity(), chatFriendList!!)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun setUnreadMessages() {
        if(countUnreadMessages!=0) {
            requireActivity().findViewById<TabLayout>(R.id.myTablayout).getTabAt(1)!!
                .getOrCreateBadge()!!.isVisible=true
            requireActivity().findViewById<TabLayout>(R.id.myTablayout).getTabAt(1)!!
                .getOrCreateBadge()!!.setNumber(countUnreadMessages);
        }else{
            requireActivity().findViewById<TabLayout>(R.id.myTablayout).getTabAt(1)!!
                .getOrCreateBadge()!!.isVisible=false
        }
    }

}