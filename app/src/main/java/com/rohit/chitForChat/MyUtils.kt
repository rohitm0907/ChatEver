package com.rohit.chitForChat

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


object MyUtils {
    var dialog: Dialog? = null;
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
        dialog!!.cancel()
    }



    fun requestAccessFineLocationPermission(activity: AppCompatActivity, requestId: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
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

}