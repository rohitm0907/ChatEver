package com.rohit.chitForChat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rohit.chitForChat.R

class ChatListAdapter(var context:Context): RecyclerView.Adapter<ChatListAdapter.viewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.viewHolder {
        var view=LayoutInflater.from(context).inflate(R.layout.list_chat,parent,false)
    return viewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatListAdapter.viewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
      return 10;
    }
    class viewHolder(itemView: View) : ViewHolder(itemView) {

    }
}