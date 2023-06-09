package com.rohit.chatever.Models

class LiveChatModel {
    constructor(){

    }
    constructor(
        sender: String,
        receiver: String,
        message: String,
        messageType: String,
        key: String,
        time: String,
        seenStatus:String,
        deleteStatus:String,
    ) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
        this.messageType = messageType
        this.key = key
        this.time=time
        this.seenStatus=seenStatus
        this.deleteStatus=deleteStatus
    }

    var deleteStatus: String? = null
        get() = field
        set(value) {
            field = value
        }
    var seenStatus: String? = null
        get() = field
        set(value) {
            field = value
        }
    var time: String? = null
        get() = field
        set(value) {
            field = value
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