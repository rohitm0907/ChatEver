package com.rohit.chitchat

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.hmomeni.progresscircula.ProgressCircula
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.permissionx.guolindev.PermissionX
import com.rohit.chitchat.Firebase.FirebaseNotification.MyNotification
import com.rohit.chitchat.Models.LiveChatModel
import com.rohit.chitchat.MyConstants.FIREBASE_BASE_URL
import com.rohit.chitchat.adapters.ChatLiveAdapter
import com.rohit.chitchat.databinding.ActivityChatLiveBinding
import kotlinx.android.synthetic.main.dialog_custom_progress.*
import net.alhazmy13.mediapicker.Video.VideoPicker
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class ChatLiveActivity : AppCompatActivity() {
    private var fileUri: Uri? = null
    private var REQUEST_TAKE_GALLERY_VIDEO: Int = 0
    private var VIDEO_CAPTURE: Int = 0
    private var audioRecorder: AudioRecorder? = null
    private var recordFile: File? = null
    private var senderId: String = ""
    private var receiverId: String = ""
    private var currentPosition = -1
    var sentImage: Bitmap? = null
    var binding: ActivityChatLiveBinding? = null
    var roomId: String? = null
    var onLiveChatScreen = false
    var isSendMessage = false
    var isFirstTimeOnScreen = true
    var firebaseChats = FirebaseDatabase.getInstance(FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHATS)
    var isScrolling = true
    var database: ValueEventListener? = null

    var firebaseUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)
    var firebaseLikedUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_LIKED_USERS)
    var firebaseChatFriends =
        FirebaseDatabase.getInstance(FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHAT_FIRENDS)
    var chatsList: ArrayList<LiveChatModel> = ArrayList()

    var firebaseOnlineStatus =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_ONLINE_STATUS)

    var likeStatus = "0"
    var token = ""


    companion object {
        private var instance: ChatLiveActivity? = null

        fun getInstance(): ChatLiveActivity? {
            return instance
        }

    }

    fun onBlock(name: String) {
        // if blocked by other user
        if (intent.getStringExtra(MyConstants.OTHER_USER_NAME).equals(name)) {
            finish()
        }
    }

    var lastDeleteTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_live)
        onLiveChatScreen=true
        instance = this@ChatLiveActivity;
        binding = ActivityChatLiveBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        getSupportActionBar()!!.hide();
        REQUEST_TAKE_GALLERY_VIDEO = 1256
        VIDEO_CAPTURE = 2345

        binding!!.txtName.setText(intent.getStringExtra(MyConstants.OTHER_USER_NAME))

        if (intent.getStringExtra(MyConstants.DELETE_TIME) != null) {
            lastDeleteTime = intent.getStringExtra(MyConstants.DELETE_TIME)!!.toLong()
        }
        audioRecording()
        if (!intent.getStringExtra(MyConstants.OTHER_USER_IMAGE).equals("")) {
            Glide.with(this@ChatLiveActivity)
                .load(intent.getStringExtra(MyConstants.OTHER_USER_IMAGE))
                .placeholder(R.drawable.user)
                .into(binding!!.imgUser)
        }
        clicks()
        senderId = MyUtils.getStringValue(this@ChatLiveActivity, MyConstants.USER_PHONE)
        receiverId = intent.getStringExtra(MyConstants.OTHER_USER_PHONE).toString()
        getAnotherUserToken()
        if (intent.getStringExtra(MyConstants.FROM) != null) {
            handleLikedStatus()
        } else {
            binding!!.imgLike.visibility = View.GONE
        }
        getOnlineStatus(receiverId)
        if (senderId < receiverId) {
            roomId = senderId + receiverId
        } else {
            roomId = receiverId + senderId
        }
        MyUtils.currentChatId=roomId!!
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
                if (s.length != 0)
                    binding!!.rcChat.scrollToPosition(chatsList.size - 1)

                if (s.length == 0) {
                    binding!!.recordButton.visibility = View.VISIBLE
                    binding!!.imgSend.visibility = View.INVISIBLE

                } else {
                    binding!!.recordButton.visibility = View.INVISIBLE
                    binding!!.imgSend.visibility = View.VISIBLE
                }

            }

            var timer = Timer();
            var DELAY: Int = 2000;

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {


            }

            override fun afterTextChanged(s: Editable) {
                if (!isTyping) {
                    if (!MyUtils.getStringValue(this@ChatLiveActivity, MyConstants.USER_PHONE)
                            .equals("")
                    ) {
                        firebaseOnlineStatus.child(
                            MyUtils.getStringValue(
                                this@ChatLiveActivity,
                                MyConstants.USER_PHONE
                            )
                        ).child(MyConstants.NODE_ONLINE_STATUS).setValue("Typing..."+receiverId)
                    }

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



        binding!!.rcChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentPosition = getCurrentItem()
                }
            }
        })

        binding!!.imgLike.setOnClickListener {
            if (isLikedProfile == false) {
                firebaseLikedUsers.child(receiverId).child(senderId).setValue("true")
                binding!!.imgLike.setImageResource(R.drawable.ic__liked)
                if (!token.equals("")) {
                    MyNotification.sendNotification(
                        MyUtils.getStringValue(this, MyConstants.USER_NAME).toString(),
                        "has liked your profile.",
                        token,
                        MyConstants.NOTI_REQUEST_TYPE,
                        roomId
                    )
                }
                isLikedProfile = true
            } else {
                MyUtils.showToast(this, "Already liked")
            }
        }


        binding!!.imgCamera.setOnClickListener {
            showOptionsDialog()
        }

        getChatsFromFirebase();

        binding!!.rcChat.addScrollListener { position: Int ->
            if (chatsList.size > 0 && chatsList.get(position).seenStatus.equals("0") && chatsList.get(
                    position
                ).receiver.equals(
                    senderId
                )
            ) {
                firebaseChats.child(roomId.toString()).child(chatsList.get(position).key.toString())
                    .child(chatsList.get(position).seenStatus.toString()).setValue("1")
            }
        }

    }


    var isLikedProfile = false
    private fun handleLikedStatus() {
        firebaseLikedUsers.child(receiverId).child(senderId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        isLikedProfile = true
                        binding!!.imgLike.setImageResource(R.drawable.ic__liked)
                    } else {
                        isLikedProfile = false
                        binding!!.imgLike.setImageResource(R.drawable.ic__dislike)
                    }

                }


                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    private fun getCurrentItem(): Int {
        return (binding!!.rcChat.getLayoutManager() as LinearLayoutManager)
            .findFirstCompletelyVisibleItemPosition()
    }

    private fun getAnotherUserToken() {
        firebaseUsers.child(receiverId).child("token")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        token = snapshot.getValue(String::class.java)!!
                    } else {
                        token = ""
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

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
                        checkImagePermissions()
                    }
                    1 -> {
                        checkVideoPermissions()
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

    private fun checkImagePermissions() {


        var alertType = ""
        lateinit var perms: Array<String>

        alertType = "This app needs access to your camera and storage"
        perms = arrayOf<String>(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )


        val rationale = alertType
        val options: Permissions.Options = Permissions.Options()
            .setRationaleDialogTitle("Info")
            .setSettingsDialogTitle("Warning")

        Permissions.check(
            this/*context*/,
            perms,
            rationale,
            options,
            object : PermissionHandler() {
                override fun onGranted() {
                    ImagePicker.with(this@ChatLiveActivity)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(
                            1080,
                            1080
                        )    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start()

                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String?>?
                ) {
                    // permission denied, block the feature.
                }
            })


    }

    private fun checkVideoPermissions() {


        var alertType = ""
        lateinit var perms: Array<String>

        alertType = "This app needs access to your camera and storage"
        perms = arrayOf<String>(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )


        val rationale = alertType
        val options: Permissions.Options = Permissions.Options()
            .setRationaleDialogTitle("Info")
            .setSettingsDialogTitle("Warning")

        Permissions.check(
            this/*context*/,
            perms,
            rationale,
            options,
            object : PermissionHandler() {
                override fun onGranted() {
                    selectVideo()
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String?>?
                ) {
                    // permission denied, block the feature.
                }
            })


    }

    // UPDATED!
    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }
    var lastPosition = 0
    fun RecyclerView.addScrollListener(onScroll: (position: Int) -> Unit) {

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (layoutManager is LinearLayoutManager) {
                     lastPosition =
                        (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
if(lastPosition==chatsList.size-1){
    binding!!.btnDown.visibility=View.GONE
}
                }
            }
        })
    }

    private fun selectVideo() {
        try {
            val pm = packageManager
            val hasPerm = pm.checkPermission(
                Manifest.permission.CAMERA,
                packageName
            )
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                val options = arrayOf<CharSequence>("Take Video", "Choose From Gallery", "Cancel")
                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(this)
                builder.setTitle("Select Option")
                builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
                    if (options[item] == "Take Video") {
                        dialog.dismiss()
                        startVideoRecording()
                    } else if (options[item] == "Choose From Gallery") {
                        dialog.dismiss()
                        fetchvideoFromGallery()
                    } else if (options[item] == "Cancel") {
                        dialog.dismiss()
                    }
                })
                builder.show()
            } else Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun fetchvideoFromGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Video"),
            REQUEST_TAKE_GALLERY_VIDEO
        )
    }

    fun startVideoRecording() {
        val imageFileName = "IMG_" + System.currentTimeMillis().toString() + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60)
        // fileUri = Uri.fromFile(mediaFile)
        fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", image);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        startActivityForResult(intent, VIDEO_CAPTURE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK

            if (requestCode == VIDEO_CAPTURE) {
                val selectedPhotoUrl: Uri = data!!.getData()!!


                var durationTime: Long
                MediaPlayer.create(this, selectedPhotoUrl).also {
                    durationTime = (it.duration / 1000).toLong()
                    it.reset()
                    it.release()
                }

//                Toast.makeText(this,durationTime.toString()+" seconds",Toast.LENGTH_SHORT).show()
                if(durationTime>60){ MyUtils.showToast(this,"You can't be share this video")
                }else{
                    uploadVideoOnFirebase(selectedPhotoUrl)

                }
            } else

                if (requestCode == ImagePicker.REQUEST_CODE) {
                    val uri: Uri = data?.data!!
                    // Use Uri object instead of File to avoid storage permissions

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        sentImage = ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                this@ChatLiveActivity.contentResolver,
                                uri
                            )
                        )
                    } else {
                        MediaStore.Images.Media.getBitmap(
                            this@ChatLiveActivity.contentResolver,
                            uri
                        )
                    }

                    uploadImageOnFirebase(sentImage!!)
                } else if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE) {
                    var mPaths: List<String> =
                        data!!.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH)!!;
//
                    mPaths.forEachIndexed { index, s ->
                        Log.d("video url", mPaths.get(index).toUri().toString())
                        var durationTime: Long
                        MediaPlayer.create(this, Uri.parse(mPaths.get(index))).also {
                            durationTime = (it.duration / 1000).toLong()
                            it.reset()
                            it.release()
                        }

//                        Toast.makeText(this,durationTime.toString(),Toast.LENGTH_SHORT).show()
                        if(durationTime>60){ MyUtils.showToast(this,"You can't be share this video")
                        }else{
                            uploadVideoOnFirebase(Uri.parse(mPaths.get(index)))

                        }
                        //Your Code


                    }
                } else if (requestCode == 1) {
                    var uri = data!!.data!!.path!!.toUri()
                    uploadVideoOnFirebase(uri!!)
                    //Your Code


                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else if (requestCode === REQUEST_TAKE_GALLERY_VIDEO) {
                    val selectedImageUri: Uri = data!!.getData()!!

                    // OI FILE Manager
                    var filemanagerstring = selectedImageUri.path

                    // MEDIA GALLERY
                    var selectedImagePath = getPath(selectedImageUri)

                        var durationTime: Long
                        MediaPlayer.create(this, selectedImageUri).also {
                            durationTime = (it.duration / 1000).toLong()
                            it.reset()
                            it.release()
                        }

//                        Toast.makeText(this,durationTime.toString(),Toast.LENGTH_SHORT).show()
                        if(durationTime>60){ MyUtils.showToast(this,"You can't be share     `this video")
                        }else{
                            uploadVideoOnFirebase(selectedImageUri)

                        }
                }
        } else {
            Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()

        }
    }

    private fun uploadVideoOnFirebase(videoUrl: Uri) {
        Log.d("mylog", videoUrl.toString())
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.getReference()
//        var file=File(videoUrl)
        val mountainvideosRef: StorageReference =
            storageRef.child("videos/" + "rohit" + Calendar.getInstance().time)

        val uploadTask: UploadTask = mountainvideosRef.putFile(videoUrl)
       showPercentageDialog()
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
        percentageDialog!!.cancel()
            MyUtils.showToast(this@ChatLiveActivity, it.message.toString())
        })
            .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { it -> // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                val result: Task<Uri> = it.getStorage().getDownloadUrl()
                result.addOnSuccessListener { uri ->
                    val videoUri: String = uri.toString()
                    percentageProgress!!.progress=100;
                    Handler().postDelayed({
                        percentageDialog!!.cancel()
                        sendMessageOnFirebase(videoUri, "video")
                    },1000)
                                    }
            })
            ?.addOnProgressListener(object: OnProgressListener<UploadTask.TaskSnapshot> {
                override fun onProgress(snapshot: UploadTask.TaskSnapshot) {
                    val progress=(100.0*snapshot.bytesTransferred / snapshot.totalByteCount)
                    percentageProgress!!.progress=progress.toInt()
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
        showPercentageDialog()
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
            percentageDialog!!.cancel()

        })
            .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { it -> // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                val result: Task<Uri> = it.getStorage().getDownloadUrl()
                result.addOnSuccessListener { uri ->
                    percentageProgress!!.progress=100;
                    Handler().postDelayed({
                        percentageDialog!!.cancel()
                        val imageUri: String = uri.toString()
                        sendMessageOnFirebase(imageUri, "audio")
                    },1000)
                }
            })

            ?.addOnProgressListener(object: OnProgressListener<UploadTask.TaskSnapshot> {
                override fun onProgress(snapshot: UploadTask.TaskSnapshot) {
                    val progress=(100.0*snapshot.bytesTransferred / snapshot.totalByteCount)
                    percentageProgress!!.progress=progress.toInt()
                }
            })
    }

    private fun sendMessageOnFirebase(message: String?, messageType: String) {
        isSendMessage = true
        var message = message
        var notificationMessage = "has sent you a new message"
        var key = firebaseChats.push().key
        var data: LiveChatModel = LiveChatModel(
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
                    notificationMessage = "has sent you a image"
                }

                if (messageType.equals("audio")) {
                    message = "Audio"
                    notificationMessage = "has sent you a audio"
                }

                if (messageType.equals("video")) {
                    message = "Video"
                    notificationMessage = "has sent you a video"

                }

                if (messageType.equals("location")) {
                    message = "Location"
                    notificationMessage = "has sent you a location"
                }

//                firebaseChatFriends.child(senderId).child(receiverId).setValue(
//                    ChatFriendsModel(
//                        userId=  receiverId,
//                        name = intent.getStringExtra(MyConstants.OTHER_USER_NAME).toString(),
//                        lastMessage = message.toString(),
//                        image = intent.getStringExtra(MyConstants.OTHER_USER_IMAGE).toString(),
//                        origonalMessage = message.toString(),
//                        seenStatus = "1",
//                        blockStatus =  "0",
//                        time = Calendar.getInstance().time.time.toString(),
//                    )
//                )

                var senderData: HashMap<String, String> = HashMap<String, String>()
                senderData.put("userId", receiverId)
                senderData.put(
                    "name", intent.getStringExtra(MyConstants.OTHER_USER_NAME).toString()
                )
                senderData.put("lastMessage", "New Message")
                senderData.put(
                    "image", intent.getStringExtra(MyConstants.OTHER_USER_IMAGE).toString()
                )
                senderData.put("origonalMessage", message.toString())
                senderData.put("seenStatus", "1")
                senderData.put("blockStatus", "0")
                senderData.put("time", Calendar.getInstance().time.time.toString())
                firebaseChatFriends.child(senderId).child(receiverId)
                    .updateChildren(senderData as Map<String, Any>)


                var data: HashMap<String, String> = HashMap<String, String>()
                data.put("userId", senderId)
                data.put(
                    "name", MyUtils.getStringValue(
                        this@ChatLiveActivity,
                        MyConstants.USER_NAME
                    )
                )
                data.put("lastMessage", "New Message")
                data.put(
                    "image", MyUtils.getStringValue(
                        this@ChatLiveActivity,
                        MyConstants.USER_IMAGE
                    ).toString()
                )
                data.put("origonalMessage", message.toString())
                data.put("seenStatus", "0")
                data.put("blockStatus", "0")
                data.put("time", Calendar.getInstance().time.time.toString())

                firebaseChatFriends.child(receiverId).child(senderId).updateChildren(
                    data as Map<String, Any>
                ).addOnCompleteListener {
                    if (!token.equals("")) {
//                        Toast.makeText(this@ChatLiveActivity,"send",Toast.LENGTH_SHORT).show()
                        MyNotification.sendNotification(
                            MyUtils.getStringValue(this, MyConstants.USER_NAME).toString(),
                            notificationMessage,
                            token,
                            MyConstants.NOTI_REQUEST_TYPE,
                            roomId
                        )
                    }
                }
                binding!!.edtMessage.setText("")

                if (chatsList.size == 0 || chatsList.size == 1) {
                    if (intent.getStringExtra(MyConstants.FROM) == null) {
                        if (!MyUtils.listFriends.contains(receiverId)) {
                            MyUtils.listFriends.add(receiverId)
                        }
                    }

                }
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
        showPercentageDialog()
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
            percentageDialog!!.cancel()

        })
            .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { it -> // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                val result: Task<Uri> = it.getStorage().getDownloadUrl()
                result.addOnSuccessListener { uri ->
                    percentageProgress!!.progress=100;
                    Handler().postDelayed({
                        percentageDialog!!.cancel()
                        val imageUri: String = uri.toString()
                        sendMessageOnFirebase(imageUri, "image")
                    },1000)
                }
            })

            ?.addOnProgressListener(object: OnProgressListener<UploadTask.TaskSnapshot> {
                override fun onProgress(snapshot: UploadTask.TaskSnapshot) {
                    val progress=(100.0*snapshot.bytesTransferred / snapshot.totalByteCount)
                    percentageProgress!!.progress=progress.toInt()
                }
            })
    }

    private fun clicks() {
        binding!!.imgBack.setOnClickListener { finish() }
        binding!!.btnDown.setOnClickListener {
            binding!!.rcChat.smoothScrollToPosition(chatsList.size - 1)
binding!!.btnDown.visibility=View.GONE
        }
    }

var percentageProgress:ProgressCircula?=null
    var percentageDialog:Dialog?=null
    private fun showPercentageDialog(){
         percentageDialog=Dialog(this@ChatLiveActivity)
        percentageDialog!!.setContentView(R.layout.dialog_custom_progress)
        percentageDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        percentageDialog!!.show()
        percentageProgress=percentageDialog!!.findViewById(R.id.myProgressBar)
        percentageProgress!!.progress=0

    }
    private fun getOnlineStatus(receiverId: String) {
        firebaseOnlineStatus.child(receiverId).child(MyConstants.NODE_ONLINE_STATUS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var onlineStatus = snapshot.getValue(String::class.java)
                        Log.d("mylog",onlineStatus.toString())
                        if (!onlineStatus.equals("Online")  ) {
                            if(onlineStatus!!.contains(senderId)){
                                binding!!.txtOnlineStatus.setText("Typing...")
                            }else if(onlineStatus!!.contains("Typing")){
                                binding!!.txtOnlineStatus.setText("Online")
                            }
                            else {
                                try {
                                    binding!!.txtOnlineStatus.setText(
                                        "Last Seen: " + getFormattedDate(
                                            onlineStatus!!.toLong()
                                        ) + "                                                "
                                    )
                                } catch (e: Exception) {
                                    binding!!.txtOnlineStatus.setText("Offline")
                                }
                            }
                        } else {
                            binding!!.txtOnlineStatus.setText(onlineStatus)
                        }
                        Handler().postDelayed({
                            binding!!.txtOnlineStatus.isSelected = true
                        }, 3000)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun getChatsFromFirebase() {
//        stateValueEventListner=
        firebaseChats.child(roomId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    chatsList.clear()
                    for (postSnapshot in snapshot.children) {
                        val chat: LiveChatModel? = postSnapshot.getValue(LiveChatModel::class.java)
                        if (chat!!.time!!.toLong() > lastDeleteTime) {
                            chatsList.add(chat!!)
                        }
//                         here you can access to name property like university.name

                    }

                    if(binding!!.rcChat.adapter==null) {
                        binding!!.rcChat.adapter =
                            ChatLiveAdapter(this@ChatLiveActivity, chatsList, roomId.toString())
                    }else{
                        binding!!.rcChat.adapter!!.notifyDataSetChanged()
                    }
                    binding!!.rcChat.setItemViewCacheSize(chatsList.size)
//                    if (isScrolling) {
//                        isScrolling = false
                    try {
                        if (isSendMessage) {
                            isSendMessage = false
                            binding!!.rcChat.scrollToPosition(chatsList.size - 1)
                        }else if(isFirstTimeOnScreen){
                            isFirstTimeOnScreen=false
                            binding!!.rcChat.scrollToPosition(chatsList.size - 1)

                        }else if(lastPosition>=chatsList.size - 4){
                            binding!!.rcChat.scrollToPosition(chatsList.size - 1)
                        }else{
                            binding!!.btnDown.visibility=View.VISIBLE
                        }
                    } catch (e: java.lang.Exception) {
                    }

                    if (chatsList.size == 1) {
                        if (onLiveChatScreen) {
                            firebaseChatFriends.child(senderId).child(receiverId)
                                .child("seenStatus")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            firebaseChatFriends.child(senderId).child(receiverId)
                                                .child("seenStatus")
                                                .setValue("1")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })

                        }
                    } else {
                        if (onLiveChatScreen) {
                            firebaseChatFriends.child(senderId).child(receiverId)
                                .child("seenStatus")
                                .setValue("1")
                        }
                    }
//                    }
                }
            }


            override fun onCancelled(error: DatabaseError) {

            }
        })


//        firebaseChats.addValueEventListener(stateValueEventListner!!);
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

    private fun checkAudioPermission(): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        val res: Int = checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
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
                    if (audioPermissionEnable) {
                        binding!!.imgCamera.visibility = View.GONE
                        binding!!.imgSend.visibility = View.GONE
                        binding!!.edtMessage.visibility = View.GONE
                    } else {
                        audioPermissionEnable = true
                    }

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

    override fun onDestroy() {
        super.onDestroy()
        MyUtils.currentChatId=""
        onLiveChatScreen = false
//        firebaseChats.removeEventListener(stateValueEventListner!!);
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onLiveChatScreen = false
//        firebaseChats.removeEventListener(stateValueEventListner!!);
    }

    var audioPermissionEnable = false
    override fun onResume() {
        super.onResume()

        audioPermissionEnable = checkAudioPermission()


        if (!MyUtils.getStringValue(this@ChatLiveActivity, MyConstants.USER_PHONE).equals(""))
            firebaseOnlineStatus.child(
                MyUtils.getStringValue(
                    this@ChatLiveActivity,
                    MyConstants.USER_PHONE
                )
            ).child(MyConstants.NODE_ONLINE_STATUS).setValue("Online")
    }


    override fun onPause() {
        super.onPause()
        if (!MyUtils.getStringValue(this@ChatLiveActivity, MyConstants.USER_PHONE).equals(""))
            firebaseOnlineStatus.child(
                MyUtils.getStringValue(
                    this@ChatLiveActivity,
                    MyConstants.USER_PHONE
                )
            ).child(MyConstants.NODE_ONLINE_STATUS)
                .setValue(Calendar.getInstance().timeInMillis.toString())
    }


    fun getFormattedDate(smsTimeInMilis: Long): String? {
        val smsTime: Calendar = Calendar.getInstance()
        smsTime.setTimeInMillis(smsTimeInMilis)
        val now: Calendar = Calendar.getInstance()
        val dateTimeFormatString = "dd/MM/yyyy"
        val HOURS = (60 * 60 * 60).toLong()

        return if (now.get(Calendar.DATE) === smsTime.get(Calendar.DATE)) {
            "Today at " + DateFormat.format("hh:mm a", smsTime).toString()
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) === 1) {
            "Yesterday at " + DateFormat.format("hh:mm a", smsTime).toString()
        } else if (now.get(Calendar.YEAR) === smsTime.get(Calendar.YEAR)) {
            DateFormat.format(dateTimeFormatString, smsTime)
                .toString() + " at " + DateFormat.format("hh:mm a", smsTime).toString()
        } else {
            DateFormat.format("dd/MM/yyyy", smsTime)
                .toString() + " at " + DateFormat.format("hh:mm a", smsTime).toString()
        }
    }


    fun getDisplayableTime(delta: Long): String? {
        var difference: Long = 0
        val mDate = Calendar.getInstance().timeInMillis
        if (mDate >= delta) {
            difference = mDate - delta
            val seconds = difference / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val months = days / 31
            val years = days / 365
            return if (seconds < 0) {
                "not yet"
            } else if (seconds < 60) {
                if (seconds == 1L) "one second ago" else "$seconds seconds ago"
            } else if (seconds < 120) {
                "a minute ago"
            } else if (seconds < 2700) // 45 * 60
            {
                "$minutes minutes ago"
            } else if (seconds < 5400) // 90 * 60
            {
                "an hour ago"
            } else if (seconds < 86400) // 24 * 60 * 60
            {
                "$hours hours ago"
            } else if (seconds < 172800) // 48 * 60 * 60
            {
                "yesterday"
            } else if (seconds < 2592000) // 30 * 24 * 60 * 60
            {
                "$days days ago"
            } else if (seconds < 31104000) // 12 * 30 * 24 * 60 * 60
            {
                if (months <= 1) "one month ago" else "$days months ago"
            } else {
                if (years <= 1) "one year ago" else "$years years ago"
            }
        }
        return null
    }



}
    
