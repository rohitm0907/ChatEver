package com.rohit.chitForChat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.rohit.chitForChat.ChatLiveActivity
import com.rohit.chitForChat.Models.ChatFriendsModel
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.R
import de.hdodenhof.circleimageview.CircleImageView

class ChatListAdapter(var context: Context, var chatFriendList: ArrayList<ChatFriendsModel>) :
    RecyclerView.Adapter<ChatListAdapter.viewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.viewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.list_chat, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatListAdapter.viewHolder, position: Int) {
        holder.txtName.setText(chatFriendList.get(position).name)
        holder.txtLastMessage.setText(chatFriendList.get(position).lastMessage)
        holder.txtTime.setText("2:00 pm")
        if(!chatFriendList.get(position).image.equals("")) {
            Glide.with(context).load(chatFriendList.get(position).image).into(holder.imgUser)
        }
        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(
                    context,
                    ChatLiveActivity::class.java
                ).putExtra(MyConstants.OTHER_USER_NAME, chatFriendList.get(position).name)
                    .putExtra(MyConstants.OTHER_USER_PHONE, chatFriendList.get(position).userId)
                    .putExtra(MyConstants.OTHER_USER_IMAGE, chatFriendList.get(position).image)
            )
        }
    }

    override fun getItemCount(): Int {
        return chatFriendList.size;
    }

    class viewHolder(itemView: View) : ViewHolder(itemView) {
        var txtName = itemView.findViewById<TextView>(R.id.txtName)
        var txtLastMessage = itemView.findViewById<TextView>(R.id.txtLastMessage)
        var txtTime = itemView.findViewById<TextView>(R.id.txtTime)
        var imgUser = itemView.findViewById<CircleImageView>(R.id.imgUser)


    }
}