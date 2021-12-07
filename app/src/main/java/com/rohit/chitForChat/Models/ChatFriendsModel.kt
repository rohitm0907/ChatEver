package com.rohit.chitForChat.Models

class ChatFriendsModel {
    constructor(){

    }
    constructor(
        userId: String,
        name: String,
        lastMessage: String,
        image:String,
    ) {
        this.name = name
        this.userId = userId
        this.lastMessage = lastMessage
        this.image = image
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