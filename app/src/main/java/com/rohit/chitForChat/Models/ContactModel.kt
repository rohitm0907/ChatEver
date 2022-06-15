package com.rohit.chitForChat.Models

import android.graphics.Bitmap
import android.net.Uri


class ContactModel {
    var name: String? = null
    var mobileNumber: String? = null
    override fun toString(): String {
        return "$name" +
                "  ${mobileNumber!!.replace(" ","")}"
    }
}