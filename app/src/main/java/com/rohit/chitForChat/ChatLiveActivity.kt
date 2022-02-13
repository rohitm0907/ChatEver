package com.rohit.chitForChat

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.devlomi.record_view.OnRecordListener
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.permissionx.guolindev.PermissionX
import com.rohit.chitForChat.Models.ChatFriendsModel
import com.rohit.chitForChat.Models.LiveChatModel
import com.rohit.chitForChat.MyConstants.FIREBASE_BASE_URL
import com.rohit.chitForChat.adapters.ChatLiveAdapter
import com.rohit.chitForChat.databinding.ActivityChatLiveBinding
import net.alhazmy13.mediapicker.Video.VideoPicker
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class ChatLiveActivity : AppCompatActivity() {
    private var audioRecorder: AudioRecorder? = null
    private var recordFile: File? = null
    private var senderId: String = ""
    private var receiverId: String = ""
    var sentImage: Bitmap? = null
    var binding: ActivityChatLiveBinding? = null
    var roomId: String? = null
    var firebaseChats =
        FirebaseDatabase.getInstance(FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHATS)

    var firebaseUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)
    var firebaseChatFriends =
        FirebaseDatabase.getInstance(FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHAT_FIRENDS)
    var chatsList: ArrayList<LiveChatModel> = ArrayList()
    var firebaseOnlineStatus =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_ONLINE_STATUS)

    var likeStatus = "0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_live)

        binding = ActivityChatLiveBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        getSupportActionBar()!!.hide();

        binding!!.txtName.setText(intent.getStringExtra(MyConstants.OTHER_USER_NAME))

        if (intent.getStringExtra(MyConstants.LIKE_STATUS) != null) {
            binding!!.imgLike.visibility = View.VISIBLE
            likeStatus = intent.getStringExtra(MyConstants.LIKE_STATUS).toString()
            if (likeStatus.equals("0")) {
                binding!!.imgLike.setImageResource(R.drawable.ic__dislike)
            } else {
                binding!!.imgLike.setImageResource(R.drawable.ic__liked)
            }
        } else {
            binding!!.imgLike.visibility = View.GONE
        }

        audioRecording()
        if (!intent.getStringExtra(MyConstants.OTHER_USER_IMAGE).equals("")) {
            Glide.with(this@ChatLiveActivity)
                .load(intent.getStringExtra(MyConstants.OTHER_USER_IMAGE))
                .into(binding!!.imgUser)
        }



        clicks()
        senderId = MyUtils.getStringValue(this@ChatLiveActivity, MyConstants.USER_PHONE)
        receiverId = intent.getStringExtra(MyConstants.OTHER_USER_PHONE).toString()


        getOnlineStatus(receiverId)
        if (senderId < receiverId) {
            roomId = senderId + receiverId
        } else {
            roomId = receiverId + senderId
        }
        binding!!.imgSend.setOnClickListener {
            if (binding!!.edtMessage.text.toString().equals("")) {
                MyUtils.showToast(this@ChatLiveActivity, "Please Enter Message")
            } else {
                sendMessageOnFirebase(binding!!.edtMessage.text.toString(), "text")
            }
        }


        binding!!.edtMessage.addTextChangedListener(object : TextWatcher {
            var isTyping = false
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding!!.rcChat.scrollToPosition(chatsList.size - 1)

            }

            var timer = Timer();
            var DELAY: Int = 2000;

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {


            }

            override fun afterTextChanged(s: Editable) {

                if (!isTyping) {
                    if (!MyUtils.getStringValue(this@ChatLiveActivity, MyConstants.USER_PHONE)
                            .equals("")
                    )
                        firebaseOnlineStatus.child(
                            MyUtils.getStringValue(
                                this@ChatLiveActivity,
                                MyConstants.USER_PHONE
                            )
                        ).child(MyConstants.NODE_ONLINE_STATUS).setValue("Typing...")

                    // Send notification for start typing event
                    isTyping = true
                }
                timer.cancel()
                timer = Timer()
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            isTyping = false
                            if (!MyUtils.getStringValue(
                                    this@ChatLiveActivity,
                                    MyConstants.USER_PHONE
                                ).equals("")
                            )
                                firebaseOnlineStatus.child(
                                    MyUtils.getStringValue(
                                        this@ChatLiveActivity,
                                        MyConstants.USER_PHONE
                                    )
                                ).child(MyConstants.NODE_ONLINE_STATUS).setValue("Online")
                            //send notification for stopped typing event
                        }
                    },
                    3000
                )
            }
        })


        binding!!.imgLike.setOnClickListener {
            if (likeStatus.equals("0")) {
                firebaseUsers.child(receiverId).child("totalLikes")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var likes = snapshot.getValue(String::class.java)!!.toInt();
                                likes++;
                                firebaseUsers.child(receiverId).child("totalLikes")
                                    .setValue(likes.toString()).addOnCompleteListener {
                                        MyUtils.showToast(
                                            this@ChatLiveActivity,
                                            "likes successfully"
                                        )
                                        likeStatus = "1"
                                        //set like of another user in our profile
                                        firebaseChatFriends.child(senderId).child(receiverId)
                                            .child("likedStatus").setValue("1")
                                        binding!!.imgLike.setBackgroundResource(R.drawable.ic__liked)

                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

            } else {
                MyUtils.showToast(this, "Already liked")
            }
        }


        binding!!.imgCamera.setOnClickListener {


            showOptionsDialog()


        }

        getChatsFromFirebase();

        binding!!.rcChat.addScrollListener { position: Int ->

            if (chatsList.get(position).seenStatus.equals("0") && chatsList.get(position).receiver.equals(
                    senderId
                )
            ) {
                firebaseChats.child(roomId.toString()).child(chatsList.get(position).key.toString())
                    .child(chatsList.get(position).seenStatus.toString()).setValue("1")
            }

        }

    }

    private fun showOptionsDialog() {
        // setup the alert builder
        // setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@ChatLiveActivity)
        builder.setTitle("Select to Share")
// add a list
// add a list
        val animals = arrayOf("Image", "Video", "Location", "Cancel")
        builder.setItems(animals,
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> {
                        ImagePicker.with(this)
                            .crop()                    //Crop image(Optional), Check Customization for more option
                            .compress(1024)            //Final image size will be less than 1 MB(Optional)
                            .maxResultSize(
                                1080,
                                1080
                            )    //Final image resolution will be less than 1080 x 1080(Optional)
                            .start()
                    }
                    1 -> {
//                        VideoPicker.Builder(this@ChatLiveActivity)
//                            .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
//                            .directory(VideoPicker.Directory.DEFAULT)
//                            .extension(VideoPicker.Extension.MP4)
//                            .enableDebuggingMode(true)
//                            .build()
                        val intent = Intent()
                        intent.type = "video/*"
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(
                            Intent.createChooser(intent, "Select Video"),
                            1
                        )


                    }
                    2 -> {

                        var location = MyUtils.getStringValue(
                            this@ChatLiveActivity,
                            MyConstants.USER_LATITUDE,
                        ) + "," + "" + MyUtils.getStringValue(
                            this@ChatLiveActivity,
                            MyConstants.USER_LONGITUDE,
                        )
                        sendMessageOnFirebase(location, "location")
                    }
                    3 -> {
                        builder.setCancelable(true)
                    }
                }
            })

        val dialog: AlertDialog = builder.create()
        dialog.show()
// create and show the alert dialog

// create and show the alert dialog

    }

    fun RecyclerView.addScrollListener(onScroll: (position: Int) -> Unit) {
        var lastPosition = 0
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (layoutManager is LinearLayoutManager) {
                    val currentVisibleItemPosition =
                        (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                    if (lastPosition != currentVisibleItemPosition && currentVisibleItemPosition != RecyclerView.NO_POSITION) {
                        onScroll.invoke(currentVisibleItemPosition)
                        lastPosition = currentVisibleItemPosition
                    }
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK

            if (requestCode == ImagePicker.REQUEST_CODE) {
                val uri: Uri = data?.data!!
                // Use Uri object instead of File to avoid storage permissions

                MyUtils.showProgress(this@ChatLiveActivity)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    sentImage = ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            this@ChatLiveActivity.contentResolver,
                            uri
                        )
                    )
                } else {
                    MediaStore.Images.Media.getBitmap(this@ChatLiveActivity.contentResolver, uri)
                }

                uploadImageOnFirebase(sentImage!!)
            } else if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE) {
                var mPaths: List<String> =
                    data!!.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH)!!;
//
//                mPaths.forEachIndexed { index, s ->
//
//                    uploadVideoOnFirebase(mPaths.get(index))
//                    //Your Code
//
//
//                }
            } else if (requestCode == 1) {
                var uri = data!!.data

                uploadVideoOnFirebase(uri!!)
                //Your Code


            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()

        }
    }

    private fun uploadVideoOnFirebase(videoUrl: Uri) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.getReference()
//        var file=File(videoUrl)
        val mountainvideosRef: StorageReference =
            storageRef.child("videos/" + "rohit" + Calendar.getInstance().time)

        val uploadTask: UploadTask = mountainvideosRef.putFile(videoUrl)
        MyUtils.showProgress(this@ChatLiveActivity)
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
            MyUtils.stopProgress(this@ChatLiveActivity)
            MyUtils.showToast(this@ChatLiveActivity, it.message.toString())
        })
            .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { it -> // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                val result: Task<Uri> = it.getStorage().getDownloadUrl()
                result.addOnSuccessListener { uri ->
                    val videoUri: String = uri.toString()
                    sendMessageOnFirebase(videoUri, "video")
                }
            })
    }


    private fun uploadAudioOnFirebase(audioUrl: Uri) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.getReference()
//        var file=File(videoUrl)
        val mountainvideosRef: StorageReference =
            storageRef.child("audios/" + "rohit" + Calendar.getInstance().time + ".3gp")
        var uri: Uri = Uri.fromFile(File(recordFile!!.path))
        val uploadTask: UploadTask = mountainvideosRef.putFile(uri)
        MyUtils.showProgress(this@ChatLiveActivity)
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
            MyUtils.stopProgress(this@ChatLiveActivity)
            MyUtils.showToast(this@ChatLiveActivity, it.message.toString())
        })
            .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { it -> // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                val result: Task<Uri> = it.getStorage().getDownloadUrl()
                result.addOnSuccessListener { uri ->
                    val uri: String = uri.toString()
                    sendMessageOnFirebase(uri, "audio")
                }
            })
    }

    private fun sendMessageOnFirebase(message: String?, messageType: String) {
        var message = message
        var key = firebaseChats.push().key
        var data: LiveChatModel =
            LiveChatModel(
                senderId,
                receiverId,
                message.toString(),
                messageType,
                key.toString(),
                Calendar.getInstance().time.time.toString(),
                "0"

            );
        firebaseChats.child(roomId!!).child(key.toString()).setValue(data)
            .addOnCompleteListener {
                MyUtils.stopProgress(this@ChatLiveActivity)
                if (messageType.equals("image")) {
                    message = "Image"
                }
                if (messageType.equals("audio")) {
                    message = "Audio"
                }
                if (messageType.equals("video")) {
                    message = "Video"
                }

                if (messageType.equals("location")) {
                    message = "Location"
                }

                firebaseChatFriends.child(senderId).child(receiverId).setValue(
                    ChatFriendsModel(
                        receiverId,
                        intent.getStringExtra(MyConstants.OTHER_USER_NAME).toString(),
                        message.toString(),
                        intent.getStringExtra(MyConstants.OTHER_USER_IMAGE).toString(),
                        message.toString(),
                        "1",
                        "0",
                        likeStatus
                    )
                )


                var data: HashMap<String, String> = HashMap<String, String>()
                data.put("userId", senderId)
                data.put(
                    "name", MyUtils.getStringValue(
                        this@ChatLiveActivity,
                        MyConstants.USER_NAME
                    )
                )
                data.put("lastMessage", "New Message")
                data.put("image", intent.getStringExtra(MyConstants.OTHER_USER_IMAGE).toString())
                data.put("origonalMessage", message.toString())
                data.put("seenStatus", "0")
                data.put("blockStatus", "0")

                firebaseChatFriends.child(receiverId).child(senderId).updateChildren(
                    data as Map<String, Any>
                )
                binding!!.edtMessage.setText("")
            }

    }


    private fun uploadImageOnFirebase(bitmap: Bitmap) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.getReference()
        val mountainImagesRef: StorageReference =
            storageRef.child("images/" + "images" + Calendar.getInstance().time + ".jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()
        val uploadTask: UploadTask = mountainImagesRef.putBytes(data)
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
            MyUtils.stopProgress(this@ChatLiveActivity)
        })
            .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { it -> // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                val result: Task<Uri> = it.getStorage().getDownloadUrl()
                result.addOnSuccessListener { uri ->
                    val imageUri: String = uri.toString()
                    sendMessageOnFirebase(imageUri, "image")
                }
            })
    }

    private fun clicks() {
        binding!!.imgBack.setOnClickListener { finish() }
    }

    private fun getOnlineStatus(receiverId: String) {
        firebaseOnlineStatus.child(receiverId).child(MyConstants.NODE_ONLINE_STATUS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var onlineStatus = snapshot.getValue(String::class.java)
                        binding!!.txtOnlineStatus.setText(onlineStatus)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

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

                    }
                    MyConstants.DATE = ""
                    binding!!.rcChat.adapter =
                        ChatLiveAdapter(this@ChatLiveActivity, chatsList, roomId.toString())

                    binding!!.rcChat.setItemViewCacheSize(chatsList.size)

                    binding!!.rcChat.scrollToPosition(chatsList.size - 1)
                    firebaseChatFriends.child(senderId).child(receiverId).child("seenStatus")
                        .setValue("1")
                }
            }


            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun audioRecording() {
        binding!!.recordButton.setRecordView(binding!!.recordView)
        binding!!.recordButton.setListenForRecord(true)
        binding!!.recordButton.isSoundEffectsEnabled = false
        binding!!.recordView.setSoundEnabled(false)
        binding!!.recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                //Start Recording..

                checkMircrophonePermission()

            }

            override fun onCancel() {
                binding!!.imgCamera.visibility = View.VISIBLE
                binding!!.imgSend.visibility = View.VISIBLE
                binding!!.edtMessage.visibility = View.VISIBLE
                stopRecording(true)
            }

            override fun onFinish(recordTime: Long) {

                binding!!.imgCamera.visibility = View.VISIBLE
                binding!!.imgSend.visibility = View.VISIBLE
                binding!!.edtMessage.visibility = View.VISIBLE
                stopRecording(false);
                if (recordFile != null) {
                    var uri = Uri.fromFile(File(recordFile?.path))
                    uploadAudioOnFirebase(uri)
                }
            }


            override fun onLessThanSecond() {
                binding!!.imgCamera.visibility = View.VISIBLE
                binding!!.imgSend.visibility = View.VISIBLE
                binding!!.edtMessage.visibility = View.VISIBLE
                stopRecording(true);
            }
        })

    }


    private fun stopRecording(deleteFile: Boolean) {
        if (audioRecorder != null)
            audioRecorder!!.stop()
        if (recordFile != null && deleteFile) {
            recordFile?.delete()
        }
    }


    private fun checkMircrophonePermission() {
        PermissionX.init(this@ChatLiveActivity)
            .permissions(Manifest.permission.RECORD_AUDIO)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "Please provide Permission, to record the audio.",
                    "OK",
                    "Cancel"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "Now,You need to allow necessary permissions in Settings manually",
                    "OK",
                    "Cancel"
                )
            }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    binding!!.imgCamera.visibility = View.GONE
                    binding!!.imgSend.visibility = View.GONE
                    binding!!.edtMessage.visibility = View.GONE

                    audioRecorder = AudioRecorder()
                    recordFile = File(filesDir, UUID.randomUUID().toString() + ".3gp")

                    if (!recordFile?.exists()!!) {
                        recordFile?.createNewFile();
                    }

                    try {
                        audioRecorder!!.start(recordFile?.path)
                    } catch (e: Exception) {

                    }
                } else {
                    checkMircrophonePermission()
                    Toast.makeText(this, "Microphone permission needed.", Toast.LENGTH_LONG).show()

                }
            }
    }
}
    
