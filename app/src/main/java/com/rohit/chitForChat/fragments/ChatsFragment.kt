package com.rohit.chitForChat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rohit.chitForChat.adapters.ChatListAdapter
import com.rohit.chitForChat.databinding.FragmentChatsBinding


class ChatsFragment : Fragment() {
    lateinit var binding:FragmentChatsBinding;
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

        binding.recyclerChatList.adapter=ChatListAdapter(requireActivity())

    }


}