package com.rohit.chitchat.Models


class ContactModel {
    var name: String? = null
    var mobileNumber: String? = null
    override fun toString(): String {
        return "$name" +
                "  ${mobileNumber!!.replace(" ","")}"
    }
}