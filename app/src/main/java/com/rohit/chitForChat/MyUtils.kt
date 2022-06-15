package com.rohit.chitForChat

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Color.TRANSPARENT
import android.graphics.PixelFormat.TRANSPARENT
import android.location.LocationManager
import android.provider.Settings
import android.widget.GridLayout
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.rohit.chitForChat.Models.ChatFriendsModel
import com.rohit.chitForChat.Models.Users
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*


object MyUtils {
    var applyFilterType = "No Filter"
    var dialog: Dialog? = null;
    var listAllUsersNumbers: ArrayList<String> = ArrayList()
    var listAllUsers: ArrayList<Users> = ArrayList()
    var chatNearbyList: ArrayList<Users> = ArrayList()
    var listFriends: ArrayList<String> = ArrayList()

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun saveStringValue(context: Context, key: String, value: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("MySharedPrefChitForChat", MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        myEdit.putString(key, value)
        myEdit.commit()
    }

    fun getStringValue(context: Context, key: String): String {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("MySharedPrefChitForChat", MODE_PRIVATE)
        return sharedPreferences.getString(key, "").toString()
    }

    fun saveBooleanValue(context: Context, key: String, value: Boolean) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("MySharedPrefChitForChat", MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        myEdit.putBoolean(key, value)
        myEdit.commit()
    }

    fun getBooleanValue(context: Context, key: String): Boolean {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("MySharedPrefChitForChat", MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, false)
    }

    fun clearAllData(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("MySharedPrefChitForChat", MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        myEdit.clear()
        myEdit.commit()
    }

    fun showProgress(context: Context) {
        dialog = Dialog(context);
        dialog!!.setContentView(R.layout.dialog_progress)
        dialog!!.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent);
        dialog!!.show()
    }

    fun stopProgress(context: Context) {
        if(dialog!=null)
        dialog!!.cancel()
    }


    fun requestAccessFineLocationPermission(activity: Activity, requestId: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            requestId
        )
    }

    /**
     * Function to check if the location permissions are granted or not
     */
    fun isAccessFineLocationGranted(context: Context): Boolean {
        return ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Function to check if location of the device is enabled or not
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Function to show the "enable GPS" Dialog box
     */
    fun showGPSNotEnabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.enable_gps))
            .setMessage(context.getString(R.string.required_for_this_app))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.enable_now)) { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

    fun convertIntoTime(timeStamp: String): String {
        var Timestamp: Long = timeStamp.toLong()
        var timeD: Date = Date(Timestamp)
        var sdf: SimpleDateFormat = SimpleDateFormat("hh:mm a")
        return sdf.format(timeD)
    }


    fun convertIntoDate(timeStamp: String): String {
        var Timestamp: Long = timeStamp.toLong()
        var timeD: Date = Date(Timestamp)
        var sdf: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        return sdf.format(timeD)
    }

    fun showProfileDialog(context: Context,imageUrl:String,captions:String,likes:String) {
        var dialog=Dialog(context)
        dialog.setContentView(R.layout.dialog_show_profile)
        var imgUser=dialog.findViewById<CircleImageView>(R.id.imgUser)
        var txtCaption=dialog.findViewById<TextView>(R.id.txtCaption)
        var txtLikes=dialog.findViewById<TextView>(R.id.txtLikes);
        dialog.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.window!!.setLayout(GridLayoutManager.LayoutParams.MATCH_PARENT,GridLayoutManager.LayoutParams.WRAP_CONTENT)
        if(!imageUrl.equals("")){
            Glide.with(context).load(imageUrl).into(imgUser)
        }
        txtCaption.setText(captions)
        txtLikes.setText(likes)
        dialog.show()

    }

}