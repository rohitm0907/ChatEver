package com.rohit.chitForChat

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohit.chitForChat.databinding.ActivityLoginBinding
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            if (!binding.edtPhoneNumber.text.equals("")) {
                startActivity(
                    Intent(this@LoginActivity, CodeVerificationActivity::class.java)
                        .putExtra(MyConstants.PHONE_NUMBER,binding.ccp.selectedCountryCode.toString()+ binding.edtPhoneNumber.text.toString())
                )
            } else {
                MyUtils.showToast(this, "Please enter your mobile number")
            }
        }
    }
}