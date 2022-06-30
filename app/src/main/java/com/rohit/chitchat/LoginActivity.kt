package com.rohit.chitchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rohit.chitchat.databinding.ActivityLoginBinding

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