package com.rohit.chitForChat

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.permissionx.guolindev.PermissionX

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        getSupportActionBar()!!.hide()
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
        timerStart()
    }
}