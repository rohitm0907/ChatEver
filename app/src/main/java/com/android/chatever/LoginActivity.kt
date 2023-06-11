package com.android.chatever

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.chatever.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
//MyUtils.showProgress(this@LoginActivity)
        binding.btnNext.setOnClickListener {
            if (!binding.edtPhoneNumber.text.trim().isNullOrEmpty()) {
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