package com.rohit.chatever

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.rohit.chatever.MyUtils.referenceMobile


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        getSupportActionBar()!!.hide()
        checkReference()
    }

    private fun checkReference() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    referenceMobile=deepLink!!.getQueryParameter("phone").toString()
//                    Toast.makeText(this@SplashActivity, referenceMobile.toString(),Toast.LENGTH_SHORT).show()
                }



                // Handle the deep link. For example, open the linked content,
                // or apply promotional credit to the user's account.
                // ...

                // ...
            }
            .addOnFailureListener(
                this
            ) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }


    private fun timerStart() {
        Handler().postDelayed({
            if (MyUtils.getBooleanValue(this@SplashActivity, MyConstants.IS_LOGIN)) {
                finishAffinity()
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
        }, 2000)
    }


    override fun onResume() {
        super.onResume()
        checkForUpdate()
//        showDialog(resources.getString(R.string.new_update_Available))
    }

    private fun checkForUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                showDialog(resources.getString(R.string.new_update_Available))
            } else {
                timerStart()
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            timerStart()
        }
    }


    fun showDialog(message: String) {
        var dialog: Dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_yes_no)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        var txtTitle = dialog.findViewById<TextView>(R.id.txtTitle)
        var btnYes = dialog.findViewById<AppCompatButton>(R.id.btnYes)
        var btnNo = dialog.findViewById<AppCompatButton>(R.id.btnNo)
        btnYes.text = "Update"
        btnNo.text = "Later"
        txtTitle.text = message

        btnYes.setOnClickListener {
            dialog.dismiss()
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }
        btnNo.setOnClickListener {
            dialog.dismiss()
            timerStart()
        }


    }

}