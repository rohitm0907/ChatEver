package com.rohit.chitForChat.adapters

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.rohit.chitForChat.Models.LiveChatModel
import com.rohit.chitForChat.MyConstants
import com.rohit.chitForChat.MyUtils
import com.rohit.chitForChat.R

class ChatLiveAdapter(
    var context: Activity,
    var chatsList: ArrayList<LiveChatModel>,
    var roomId: String
) :
    RecyclerView.Adapter<ChatLiveAdapter.viewHolder>() {
    var firebaseChats =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHATS)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        if (viewType == 1) {
            return viewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_chat_right, parent, false)!!
            )
        } else {
            return viewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_chat_left, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: ChatLiveAdapter.viewHolder, position: Int) {
        if (chatsList.get(position).sender.equals(
                MyUtils.getStringValue(
                    context,
                    MyConstants.USER_PHONE
                )
            )
        ) {
            holder.viewSeen.visibility = View.VISIBLE
            if (chatsList.get(position).seenStatus.toString().equals("1")) {
                holder.viewSeen.setBackgroundResource(R.drawable.bg_message_seen)
            } else {
                holder.viewSeen.setBackgroundResource(R.drawable.bg_message_unseen)
            }
        } else {
            holder.viewSeen.visibility = View.GONE
            if (chatsList.get(position).seenStatus.equals("0") && chatsList.get(position).receiver.equals(
                    MyUtils.getStringValue(context, MyConstants.USER_PHONE)
                )
            ) {
                firebaseChats.child(roomId.toString()).child(chatsList.get(position).key.toString())
                    .child("seenStatus").setValue("1")
            }

        }

        if (chatsList.get(position).messageType.equals("text")) {
            holder.imgPlay.visibility=View.GONE
            holder.imgMessage.visibility = View.GONE
            holder.txtMessage.visibility = View.VISIBLE
            holder.audioPause.visibility=View.GONE
            holder.audioPlay.visibility=View.GONE
            holder.audioSeekbar.visibility=View.GONE
            holder.txtMessage.text = chatsList[position].message

        } else if (chatsList.get(position).messageType.equals("image")) {
            holder.audioPause.visibility=View.GONE
            holder.audioPlay.visibility=View.GONE
            holder.audioSeekbar.visibility=View.GONE
            holder.txtMessage.visibility = View.GONE
            holder.imgMessage.visibility = View.VISIBLE
            Glide.with(context)
                .load(chatsList.get(position).message)
                .into(holder.imgMessage)
            holder.imgPlay.visibility=View.GONE
        } else if (chatsList.get(position).messageType.equals("video")) {
            holder.audioPause.visibility=View.GONE
            holder.audioPlay.visibility=View.GONE
            holder.audioSeekbar.visibility=View.GONE
            holder.imgPlay.visibility=View.VISIBLE
            holder.txtMessage.visibility = View.GONE
            holder.imgMessage.visibility = View.VISIBLE
            Glide.with(context)
                .load(chatsList.get(position).message)
                .into(holder.imgMessage)
        }else if(chatsList.get(position).messageType.equals("audio")){
            holder.audioPause.visibility=View.INVISIBLE
            holder.audioPlay.visibility=View.VISIBLE
            holder.audioSeekbar.visibility=View.VISIBLE
            holder.imgPlay.visibility=View.GONE
            holder.imgMessage.visibility = View.GONE
            holder.txtMessage.visibility = View.GONE
        }


        if (!MyConstants.DATE.equals(MyUtils.convertIntoDate(chatsList.get(position).time.toString()))) {
            holder.txtDate.visibility = View.VISIBLE
            holder.txtDate.setText(MyUtils.convertIntoDate(chatsList.get(position).time.toString()))
            MyConstants.DATE = MyUtils.convertIntoDate(chatsList.get(position).time.toString())
        } else {
            holder.txtDate.visibility = View.GONE
        }
        holder.txtTime.setText(MyUtils.convertIntoTime((chatsList.get(position).time).toString()))

        holder.imgPlay.setOnClickListener {
            showDialog(chatsList.get(position).message, chatsList.get(position).messageType.toString())
        }
        holder.imgMessage.setOnClickListener {
            showDialog(chatsList.get(position).message, chatsList.get(position).messageType.toString())
        }

    }

    private fun showDialog(url: String?,type:String) {
        var dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_image)

        var imgUser = dialog.findViewById<ImageView>(R.id.imgUser)
        var videoUser=dialog.findViewById<VideoView>(R.id.videoUser)

        dialog.getWindow()!!.setBackgroundDrawableResource(android.R.color.black);
        dialog.window!!.setLayout(
            GridLayoutManager.LayoutParams.MATCH_PARENT,
            GridLayoutManager.LayoutParams.WRAP_CONTENT
        )

        if(type.equals("video")){
            imgUser.visibility=View.GONE
            videoUser.visibility=View.VISIBLE
            var mediaController = MediaController(context)
//            mediaController.setAnchorView(videoUser)
            videoUser.setMediaController(mediaController)
            videoUser.setVideoURI(Uri.parse(url))
            videoUser.requestFocus()
            videoUser.setOnPreparedListener {
                videoUser.start()
                mediaController.show(3000)
            }
        }else {
            imgUser.visibility=View.VISIBLE
            videoUser.visibility=View.GONE
            if (!url.equals("")) {
                Glide.with(context).load(url).into(imgUser)
            }
        }

        dialog.show()

    }

    override fun getItemCount(): Int {
        return chatsList.size;
    }

    override fun getItemViewType(position: Int): Int {

        if (chatsList.get(position).sender.equals(
                MyUtils.getStringValue(
                    context,
                    MyConstants.USER_PHONE
                )
            )
        ) {
            return 1
        } else {
            return 2
        }
    }

    class viewHolder(itemView: View) : ViewHolder(itemView) {
        var txtMessage = itemView.findViewById<TextView>(R.id.txtMessage)
        var imgMessage = itemView.findViewById<ImageView>(R.id.imgMessage)
        var txtTime = itemView.findViewById<TextView>(R.id.txtTime)
        var txtDate = itemView.findViewById<TextView>(R.id.txtdate)
        var viewSeen = itemView.findViewById<View>(R.id.viewSeen)
        var imgPlay = itemView.findViewById<ImageView>(R.id.imgPlay)
        var audioPlay = itemView.findViewById<ImageView>(R.id.audioPlay)
        var audioPause = itemView.findViewById<ImageView>(R.id.audioPause)
        var audioSeekbar=itemView.findViewById<SeekBar>(R.id.audioSeekbar)
    }
}