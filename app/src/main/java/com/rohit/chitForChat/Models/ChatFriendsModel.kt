package com.rohit.chitForChat.Models

class ChatFriendsModel {
    constructor(){

    }
    constructor(
        userId: String,
        name: String,
        lastMessage: String,
        image:String,
        origonalMessage:String,
        seenStatus:String,
    ) {
        this.name = name
        this.userId = userId
        this.lastMessage = lastMessage
        this.image = image
        this.seenStatus=seenStatus
        this.origonalMessage=origonalMessage
    }
    var origonalMessage: String? = null
        get() = field
        set(value) {
            field = value
        }

    var seenStatus: String? = null
        get() = field
        set(value) {
            field = value
        }
    var image: String? = null
        get() = field
        set(value) {
            field = value
        }
    var name: String? = null
        get() = field
        set(value) {
            field = value
        }
    var userId: String? = null
        get() = field
        set(value) {
            field = value
        }
    var lastMessage: String? = null
        get() = field
        set(value) {
            field = value
        }

}