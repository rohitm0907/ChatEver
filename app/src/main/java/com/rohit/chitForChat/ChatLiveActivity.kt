package com.rohit.chitForChat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rohit.chitForChat.Models.ChatFriendsModel
import com.rohit.chitForChat.Models.LiveChatModel
import com.rohit.chitForChat.MyConstants.FIREBASE_BASE_URL
import com.rohit.chitForChat.adapters.ChatLiveAdapter
import com.rohit.chitForChat.databinding.ActivityChatLiveBinding

class ChatLiveActivity : AppCompatActivity() {
    var binding: ActivityChatLiveBinding? = null
    var roomId: String? = null
    var firebaseChats =
        FirebaseDatabase.getInstance(FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHATS)

    var firebaseChatFriends =
        FirebaseDatabase.getInstance(FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHAT_FIRENDS)
    var chatsList: ArrayList<LiveChatModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_live)
        binding = ActivityChatLiveBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.txtName.setText(intent.getStringExtra(MyConstants.OTHER_USER_NAME))
        Glide.with(this@ChatLiveActivity).load(intent.getStringExtra(MyConstants.OTHER_USER_IMAGE))
            .into(binding!!.imgUser)
        var senderId = MyUtils.getStringValue(this@ChatLiveActivity, MyConstants.USER_PHONE)
        var receiverId = intent.getStringExtra(MyConstants.OTHER_USER_PHONE).toString()
        if (senderId < receiverId) {
            roomId = senderId + receiverId
        } else {
            roomId = receiverId + senderId
        }
        binding!!.imgSend.setOnClickListener {
            if (binding!!.edtMessage.text.equals("")) {
                MyUtils.showToast(this@ChatLiveActivity, "Please Enter Message")
            } else {
                var key = firebaseChats.push().key
                var data: LiveChatModel =
                    LiveChatModel(
                        senderId,
                        receiverId,
                        binding!!.edtMessage.text.toString(),
                        key.toString(),
                        "text"
                    );
                firebaseChats.child(roomId!!).child(key.toString()).setValue(data)
                    .addOnCompleteListener {
                        firebaseChatFriends.child(senderId).child(receiverId).setValue(
                            ChatFriendsModel(
                                receiverId,
                                intent.getStringExtra(MyConstants.OTHER_USER_NAME).toString(),
                                binding!!.edtMessage.text.toString(),
                                intent.getStringExtra(MyConstants.OTHER_USER_IMAGE).toString()
                            )
                        )
                        firebaseChatFriends.child(receiverId).child(senderId).setValue(
                            ChatFriendsModel(
                                senderId,
                                MyUtils.getStringValue(
                                    this@ChatLiveActivity,
                                    MyConstants.USER_NAME
                                ),
                                binding!!.edtMessage.text.toString(),
                                intent.getStringExtra(MyConstants.OTHER_USER_IMAGE).toString()
                            )
                        )
                        binding!!.edtMessage.setText("")
                    }


            }
        }

        getChatsFromFirebase();
    }

    private fun getChatsFromFirebase() {
        firebaseChats.child(roomId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    chatsList.clear()
                    for (postSnapshot in snapshot.children) {
                        val chat: LiveChatModel? = postSnapshot.getValue(LiveChatModel::class.java)
                        chatsList.add(chat!!)
                        // here you can access to name property like university.name
                        binding!!.rcChat.adapter = ChatLiveAdapter(this@ChatLiveActivity, chatsList)
                    }

                    binding!!.rcChat.scrollToPosition(chatsList.size - 1)

                }
            }


            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}