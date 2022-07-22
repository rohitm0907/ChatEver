package com.rohit.chitchat

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
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
import com.rohit.chitchat.Models.Users
import com.rohit.chitchat.MyConstants.NODE_USERS
import com.rohit.chitchat.databinding.ActivityProfileBinding
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class ProfileActivity : AppCompatActivity() {
    var userImage: Bitmap? = null
    var imgUri: Uri? = null
    var firebaseUsers = FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
        .getReference(NODE_USERS)
    lateinit var binding: ActivityProfileBinding
    var date: String = ""
    var selectedGender = ""
    var firebaseChatFriends =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_CHAT_FIRENDS)
    var firebaseLikedUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_LIKED_USERS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.edtName.setText(MyUtils.getStringValue(this@ProfileActivity, MyConstants.USER_NAME))
        selectedGender = MyUtils.getStringValue(this@ProfileActivity, MyConstants.USER_GENDER)

        if (selectedGender.equals("Male")) {
            spGender.selectItemByIndex(0)
        } else if (selectedGender.equals("Female")) {
            spGender.selectItemByIndex(1)
        } else if (selectedGender.equals("Others")) {
            spGender.selectItemByIndex(2)
        }

        binding.edtCaptions.setText(
            MyUtils.getStringValue(
                this@ProfileActivity,
                MyConstants.USER_CAPTIONS
            )
        )

        if (!MyUtils.getStringValue(this@ProfileActivity, MyConstants.USER_IMAGE).equals("")) {
            Glide.with(this@ProfileActivity)
                .load(MyUtils.getStringValue(this@ProfileActivity, MyConstants.USER_IMAGE))
                .placeholder(R.drawable.user).into(binding.imgUser)
        }

        if (MyUtils.getBooleanValue(this@ProfileActivity, MyConstants.IS_LOGIN)) {
            setUserTotalLikes()
        }


        binding.imgEdit.setOnClickListener {
            ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding.imgUser.setOnClickListener {
            showDialog(this, MyUtils.getStringValue(this@ProfileActivity, MyConstants.USER_IMAGE))
        }

        binding.btnSave.setOnClickListener {
            MyUtils.showProgress(this@ProfileActivity)
            if (userImage != null) {
                uploadFile(userImage!!)
            } else {
                if(!binding.edtName.text.toString().trim().equals("")) {
                    uploadData(
                        intent.getStringExtra(MyConstants.PHONE_NUMBER).toString(),
                        binding.edtName.text.toString(),
                        binding.edtCaptions.text.toString(),
                        MyUtils.getStringValue(this@ProfileActivity, MyConstants.USER_IMAGE),
                        selectedGender
                    )
                }else{
                    MyUtils.stopProgress(this@ProfileActivity)
                    Toast.makeText(this@ProfileActivity,"Please enter your name",Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.spGender.setOnSpinnerItemSelectedListener(
            OnSpinnerItemSelectedListener<String?> { oldIndex, oldItem, newIndex, newItem ->
                selectedGender = newItem!!
            })
    }

    private fun setUserTotalLikes() {
        llLike.visibility = View.VISIBLE
        firebaseLikedUsers.child(
            MyUtils.getStringValue(
                this@ProfileActivity,
                MyConstants.USER_PHONE
            )
        )
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        txtLikes.text = snapshot.childrenCount.toString()
                    } else {
                        txtLikes.text = "0"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }


    fun showDialog(context: Context, url: String?) {
        var dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_image)
        var imgUser = dialog.findViewById<ImageView>(R.id.imgUser)
        var imgBack = dialog.findViewById<ImageView>(R.id.imgBack)

        dialog.getWindow()!!.setBackgroundDrawableResource(android.R.color.black);
        dialog.window!!.setLayout(
            GridLayoutManager.LayoutParams.MATCH_PARENT,
            GridLayoutManager.LayoutParams.MATCH_PARENT
        )
        imgBack.setOnClickListener {
            dialog.cancel()
        }

        imgUser.visibility = View.VISIBLE

        Glide.with(context).load(url).placeholder(R.drawable.user).into(imgUser)

        if (imgUri != null) {
            Glide.with(context).load(File(imgUri!!.path)).placeholder(R.drawable.user).into(imgUser)
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            imgUri = data?.data!!
            // Use Uri object instead of File to avoid storage permissions
            binding.imgUser.setImageURI(imgUri)

            userImage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        this@ProfileActivity.contentResolver,
                        imgUri!!
                    )
                )
            } else {
                MediaStore.Images.Media.getBitmap(this@ProfileActivity.contentResolver, imgUri)
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        }
    }

    //uploading image to firebase
    private fun uploadFile(bitmap: Bitmap) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.getReference()
        val mountainImagesRef: StorageReference =
            storageRef.child("profilePics/" + "rohit" + Calendar.getInstance().time + ".jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()
        val uploadTask: UploadTask = mountainImagesRef.putBytes(data)
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
            MyUtils.stopProgress(this@ProfileActivity)
        })
            .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { it -> // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                val result: Task<Uri> = it.getStorage().getDownloadUrl()
                result.addOnSuccessListener { uri ->
                    val imageUri: String = uri.toString()
                    val name: String = binding.edtName.text.toString()
                    uploadData(
                        intent.getStringExtra(MyConstants.PHONE_NUMBER).toString(),
                        name,
                        binding.edtCaptions.text.toString(),
                        imageUri.toString(),
                        selectedGender
                    )
                }
            })
    }


    //upload detail on firebase
    private fun uploadData(
        phone: String,
        name: String,
        captions: String,
        imageUri: String,
        selectedGender: String
    ) {
        var users: Users = Users();
        users!!.name = name
        users.phone = phone
        users.image = imageUri!!
        users.lat = ""
        users.long = ""
        users.gender = selectedGender
        if (!captions.equals("")) {
            users.captions = captions
        } else {
            users.captions = "No Captions"
        }

        if (MyUtils.getStringValue(this, MyConstants.GHOST_MODE).equals("")) {
            users.ghostMode = "off"

        } else {
            users.ghostMode = MyUtils.getStringValue(this, MyConstants.GHOST_MODE)
        }

        if (!MyUtils.getBooleanValue(this@ProfileActivity, MyConstants.IS_LOGIN)) {
            users.totalLikes = "0"
            users.token = "no token"
        }


        if (!MyUtils.getBooleanValue(this@ProfileActivity, MyConstants.IS_LOGIN)) {
            if (!MyUtils.referenceMobile.equals("")) {
                firebaseLikedUsers.child(MyUtils.referenceMobile).child(phone + "r")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                firebaseLikedUsers.child(phone).child(MyUtils.referenceMobile + "r")
                                    .setValue("reference")
                                firebaseLikedUsers.child(MyUtils.referenceMobile).child(phone + "r")
                                    .setValue("reference").addOnCompleteListener {
                                        MyUtils.referenceMobile = ""
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }
        }


        firebaseUsers.child(phone).setValue(users!!).addOnCompleteListener {
            MyUtils.stopProgress(this@ProfileActivity)

            MyUtils.saveStringValue(
                this@ProfileActivity,
                MyConstants.USER_NAME,
                name
            )
            MyUtils.saveStringValue(
                this@ProfileActivity,
                MyConstants.USER_IMAGE,
                imageUri!!
            )
            MyUtils.saveStringValue(
                this@ProfileActivity,
                MyConstants.USER_PHONE,
                phone
            )
            MyUtils.saveStringValue(
                this@ProfileActivity,
                MyConstants.USER_CAPTIONS,
               captions.toString()
            )

            MyUtils.saveStringValue(
                this@ProfileActivity,
                MyConstants.GHOST_MODE,
                users.ghostMode.toString()
            )

            MyUtils.saveStringValue(this@ProfileActivity, MyConstants.USER_GENDER, selectedGender)

            if (MyUtils.getBooleanValue(this@ProfileActivity, MyConstants.IS_LOGIN)) {
                userImage = null
                updateImage(name, imageUri, captions)
            } else {
                userImage = null
                finishAffinity()
                startActivity(Intent(this, HomeActivity::class.java))
            }

            MyUtils.saveBooleanValue(
                this@ProfileActivity,
                MyConstants.IS_LOGIN,
                true
            )
        }
    }

    private fun updateImage(name: String, image: String, caption: String) {
        firebaseChatFriends.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot in snapshot.children) {
                    var keyValue = postSnapshot.key

                    val hashMap: HashMap<String, String>? =
                        postSnapshot.getValue() as HashMap<String, String>
                    var value = hashMap!!.keys
                    println("Key_Data==" + value)

                    var userList = value.filter {
                        it.toString().equals(
                            MyUtils.getStringValue(
                                this@ProfileActivity,
                                MyConstants.USER_PHONE
                            )
                        )
                    }
                    if (userList.size > 0) {
                        firebaseChatFriends.child(keyValue.toString()).child(userList[0])
                            .child("image").setValue(image)
                        firebaseChatFriends.child(keyValue.toString()).child(userList[0])
                            .child("name").setValue(name)
                        firebaseChatFriends.child(keyValue.toString()).child(userList[0])
                            .child("caption").setValue(caption)
                    }
//                    println("rohit: "+keyValue)

                }
                MyUtils.showToast(this@ProfileActivity, "Updated Successfully")
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}