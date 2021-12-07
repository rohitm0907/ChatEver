package com.rohit.chitForChat.Models

class LiveChatModel {
    constructor(){

    }
    constructor(
        sender: String,
        receiver: String,
        message: String,
        messageType: String,
        key: String
    ) {
        this.sender = sender
        this.sender = sender
        this.receiver = receiver
        this.key = key
        this.messageType = messageType
        this.message = message


    }

    var sender: String? = null
        get() = field
        set(value) {
            field = value
        }
    var receiver: String? = null
        get() = field
        set(value) {
            field = value
        }
    var key: String? = null
        get() = field
        set(value) {
            field = value
        }
    var messageType: String? = null
        get() = field
        set(value) {
            field = value
        }
    var message: String? = null
        get() = field
        set(value) {
            field = value
        }
}