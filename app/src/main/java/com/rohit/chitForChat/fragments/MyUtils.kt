package com.rohit.chitForChat.fragments

import android.content.Context
import android.widget.Toast

object MyUtils {

fun showToast(context:Context,message:String){
    Toast.makeText(context,message,Toast.LENGTH_LONG).show()
}
}