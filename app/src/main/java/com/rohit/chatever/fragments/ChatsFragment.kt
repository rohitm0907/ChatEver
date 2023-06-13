package com.rohit.chatever.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rohit.chatever.Models.ChatFriendsModel
import com.rohit.chatever.MyConstants
import com.rohit.chatever.MyUtils
import com.rohit.chatever.adapters.ChatListAdapter
import com.rohit.chatever.databinding.FragmentChatsBinding
import android.app.Activity
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.rohit.chatever.MyUtils.listFriends
import com.rohit.chatever.R
import kotlin.collections.ArrayList

class ChatsFragment : Fragment() {
    var firebaseChatFriends =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHAT_FIRENDS)
    var countUnreadMessages = 0
    lateinit var binding: FragmentChatsBinding
    var chatFriendList: ArrayList<ChatFriendsModel> = ArrayList()
    var adapterFriendList:ChatListAdapter?=null


    var currentPosition = -1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                    Log.d("mylog","vdvdvdvdbdbd")
                    if (snapshot.exists()) {
                        chatFriendList.clear()
                        countUnreadMessages = 0
                        for (postSnapshot in snapshot.children) {
                            val user: ChatFriendsModel? =
                                postSnapshot.getValue(ChatFriendsModel::class.java)
                            if (user!!.seenStatus.equals("0")) {
                                countUnreadMessages++
                            }
                            if (user.deleteTime != null) {
                                if (user.deleteTime!!.toLong() < user.time!!.toLong()) {
                                    chatFriendList.add(user!!)
                                    listFriends.add(user.userId!!)
                                }
                            } else {
                                chatFriendList.add(user!!)
                                listFriends.add(user.userId!!)
                            }
                            // for check in nearby list, user is already friend or not
                            // here you can access to name property like university.name
                        }
                        chatFriendList.sortByDescending { chatFriendsModel ->
                            chatFriendsModel.time!!.toDouble()
                        }

Log.d("mylog",chatFriendList.toString())
                        if (activity != null) {
                            if(adapterFriendList==null || binding!!.recyclerChatList.adapter==null){
                                    adapterFriendList=
                                    ChatListAdapter(requireActivity(), chatFriendList!!)
                                binding!!.recyclerChatList.adapter =adapterFriendList
                                binding!!.recyclerChatList.setItemViewCacheSize(chatFriendList.size)
                                setUnreadMessages()

                            }else{

                                binding!!.recyclerChatList.adapter!!.notifyDataSetChanged()
                                setUnreadMessages()

                            }
                                                    }
//                        try {
//                            if (currentPosition != -1) {
//                                binding!!.recyclerChatList.scrollToPosition(currentPosition)
//                            }
//
//                        } catch (e: Exception) {
//
//                        }
                    } else {
                            chatFriendList.clear()
                            val activity: Activity? = activity
                            if (activity != null) {
                                binding!!.recyclerChatList.adapter =
                                    ChatListAdapter(requireActivity(), chatFriendList!!)

                        }
                    }


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


        binding!!.recyclerChatList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentPosition = getCurrentItem()
                }
            }
        })
    }

    private fun getCurrentItem(): Int {
        return (binding!!.recyclerChatList.getLayoutManager() as LinearLayoutManager)
            .findFirstCompletelyVisibleItemPosition()
    }

    private fun setUnreadMessages() {
        if (countUnreadMessages != 0) {
            requireActivity().findViewById<TabLayout>(R.id.myTablayout).getTabAt(0)!!
                .getOrCreateBadge()!!.isVisible = true
            requireActivity().findViewById<TabLayout>(R.id.myTablayout).getTabAt(0)!!
                .getOrCreateBadge()!!.setNumber(countUnreadMessages);
        } else {
            requireActivity().findViewById<TabLayout>(R.id.myTablayout).getTabAt(0)!!
                .getOrCreateBadge()!!.isVisible = false
        }
    }

}