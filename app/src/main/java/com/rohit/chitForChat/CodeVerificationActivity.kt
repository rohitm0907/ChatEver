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

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rohit.chitForChat.databinding.ActivityCodeVerificationBinding
import com.mukesh.OnOtpCompletionListener
import com.rohit.chitForChat.Models.Users
import java.util.concurrent.TimeUnit


class CodeVerificationActivity : AppCompatActivity() {
    var VerificationId = ""
    var auth: FirebaseAuth? = null
    var firebaseUsers =
        FirebaseDatabase.getInstance(MyConstants.FIREBASE_BASE_URL)
            .getReference(MyConstants.NODE_USERS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_verification)
        var binding = ActivityCodeVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
//        sentOtp(intent!!.getStringExtra(MyConstants.PHONE_NUMBER).toString());
        binding.txtMobile.setText(intent.getStringExtra(MyConstants.COUNTRY_CODE)+intent.getStringExtra(MyConstants.PHONE_NUMBER))

        binding.otpView.setOtpCompletionListener(object : OnOtpCompletionListener {
            override fun onOtpCompleted(otp: String) {
                // do Stuff
//                verifyOtp(otp)
                checkAlreadyRegister()

            }
        })



//        sentOtp(intent!!.getStringExtra(MyConstants.PhoneNumber).toString());

    }

    private fun verifyOtp(otp: String) {
        MyUtils.showProgress(this@CodeVerificationActivity)
        val credential = PhoneAuthProvider.getCredential(VerificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    checkAlreadyRegister()

                } else {
                    // Sign in failed, display a message and update the UI
                    MyUtils.stopProgress(this@CodeVerificationActivity)
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    MyUtils.showToast(this, "Invalid OTP")
                    // Update UI
                }
            }
    }


    private fun sentOtp(phoneNumber: String) {
        MyUtils.showProgress(this@CodeVerificationActivity)
        var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                checkAlreadyRegister()
            }


            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
//                Log.w(ContentValues.TAG, "onVerificationFailed", e)
                MyUtils.stopProgress(this@CodeVerificationActivity)
                MyUtils.showToast(this@CodeVerificationActivity, e.toString())
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
                MyUtils.stopProgress(this@CodeVerificationActivity)
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(ContentValues.TAG, "onCodeSent:$verificationId")
                MyUtils.showToast(this@CodeVerificationActivity, "OTP Sent Successfully")
                // Save verification ID and resending token so we can use them later
                VerificationId = verificationId

            }
        }

        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(intent.getStringExtra(MyConstants.COUNTRY_CODE) + phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun checkAlreadyRegister() {
        MyUtils.showProgress(this@CodeVerificationActivity)
        firebaseUsers.child(intent.getStringExtra(MyConstants.PHONE_NUMBER).toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    MyUtils.stopProgress(this@CodeVerificationActivity)
                    if (snapshot.exists()) {
                        var data: Users? = snapshot.getValue(Users::class.java)
                        MyUtils.saveStringValue(
                            this@CodeVerificationActivity,
                            MyConstants.USER_NAME,
                            data!!.name.toString()
                        )
                        MyUtils.saveStringValue(
                            this@CodeVerificationActivity,
                            MyConstants.USER_IMAGE,
                            data!!.image.toString()
                        )
                        MyUtils.saveStringValue(
                            this@CodeVerificationActivity,
                            MyConstants.USER_PHONE,
                            data!!.phone.toString()
                        )

                        MyUtils.saveStringValue(
                            this@CodeVerificationActivity,
                            MyConstants.USER_GENDER,
                            data!!.gender.toString()
                        )
                        MyUtils.saveBooleanValue(
                            this@CodeVerificationActivity,
                            MyConstants.IS_LOGIN,
                            true
                        )
                        finishAffinity()
                        startActivity(
                            Intent(
                                this@CodeVerificationActivity,
                                HomeActivity::class.java
                            ).putExtra(
                                MyConstants.PHONE_NUMBER,
                                intent.getStringExtra(MyConstants.PHONE_NUMBER)
                            )
                        )

                    } else {
                        startActivity(
                            Intent(
                                this@CodeVerificationActivity,
                                ProfileActivity::class.java
                            ).putExtra(
                                MyConstants.PHONE_NUMBER,
                                intent.getStringExtra(MyConstants.PHONE_NUMBER)
                            )
                        )


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    MyUtils.stopProgress(this@CodeVerificationActivity)
                }
            })
    }
}

