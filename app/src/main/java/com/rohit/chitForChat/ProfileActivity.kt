package com.rohit.chitForChat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.rohit.chitForChat.Models.Users
import com.rohit.chitForChat.MyConstants.NODE_USERS
import com.rohit.chitForChat.databinding.ActivityProfileBinding
import com.rohit.chitForChat.fragments.MyUtils
import java.io.ByteArrayOutputStream
import java.util.*


class ProfileActivity : AppCompatActivity() {
    var userImage: Bitmap? = null
    var firebaseUsers =
        FirebaseDatabase.getInstance("https://chitforchat-d1ee5-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference(NODE_USERS)
    lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MyUtils.showToast(this, intent.getStringExtra(MyConstants.PHONE_NUMBER).toString())
        binding.imgUser.setOnClickListener {
            ImagePicker.with(this)
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        binding.btnSave.setOnClickListener {
            uploadFile(userImage!!)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!
            // Use Uri object instead of File to avoid storage permissions
            binding.imgUser.setImageURI(uri)

            userImage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        this@ProfileActivity.contentResolver,
                        uri
                    )
                )
            } else {
                MediaStore.Images.Media.getBitmap(this@ProfileActivity.contentResolver, uri)
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadFile(bitmap: Bitmap) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.getReference()
        val mountainImagesRef: StorageReference =
            storageRef.child("images/" + "rohit" + Calendar.getInstance().time + ".jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
        val data = baos.toByteArray()
        val uploadTask: UploadTask = mountainImagesRef.putBytes(data)
        uploadTask.addOnFailureListener(OnFailureListener {
            // Handle unsuccessful uploads
        })
            .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { it -> // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                val result: Task<Uri> = it.getStorage().getDownloadUrl()
                result.addOnSuccessListener { uri ->
                    val imageUri: String = uri.toString()
                    val name: String = binding.edtName.text.toString()
                    uploadData(
                        intent.getStringExtra(MyConstants.PHONE_NUMBER).toString(),
                        name,
                        imageUri.toString()
                    )
                }
            })
    }

    private fun uploadData(phone: String, name: String, imageUri: String) {
        var users: Users = Users();
        users!!.name = name
        users.phone = phone
        users.image = imageUri!!
        firebaseUsers.child(phone).setValue(users!!).addOnCompleteListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }


    }


}