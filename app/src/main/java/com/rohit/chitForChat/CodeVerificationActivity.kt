package com.rohit.chitForChat

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohit.chitForChat.databinding.ActivityCodeVerificationBinding
import com.mukesh.OnOtpCompletionListener
import com.rohit.chitForChat.fragments.MyUtils
import java.util.concurrent.TimeUnit

class CodeVerificationActivity : AppCompatActivity() {
    var VerificationId=""
    var auth:FirebaseAuth?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_verification)
        var binding=ActivityCodeVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth= FirebaseAuth.getInstance()
        binding.otpView.setOtpCompletionListener(object: OnOtpCompletionListener {
            override fun onOtpCompleted(otp:String) {
                // do Stuff
//                verifyOtp(otp)
                startActivity(Intent(this@CodeVerificationActivity,ProfileActivity::class.java).putExtra(MyConstants.PHONE_NUMBER,intent.getStringExtra(MyConstants.PHONE_NUMBER)))

            }
        })

//        sentOtp(intent!!.getStringExtra(MyConstants.PhoneNumber).toString());

    }

    private fun verifyOtp(otp:String) {
        val credential = PhoneAuthProvider.getCredential(VerificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }



    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")


                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                   MyUtils.showToast(this,"Invalid OTP")
                    // Update UI
                }
            }
    }









    private fun sentOtp(phoneNumber:String) {


        var  callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.


            }



            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(ContentValues.TAG, "onVerificationFailed", e)
                MyUtils.showToast(this@CodeVerificationActivity,e.toString())
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(ContentValues.TAG, "onCodeSent:$verificationId")
MyUtils.showToast(this@CodeVerificationActivity,"Code Successfully Sent")
                // Save verification ID and resending token so we can use them later
                VerificationId = verificationId

            }
        }

        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber("+91"+phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }
    }
