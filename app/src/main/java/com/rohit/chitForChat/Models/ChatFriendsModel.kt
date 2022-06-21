package com.rohit.chitForChat.Models

class ChatFriendsModel {
    constructor()
    constructor(
        userId: String,
        name: String,
        lastMessage: String,
        image:String,
        origonalMessage:String,
        seenStatus:String,
        blockStatus:String,
        time:String,
        deleteTime:String
    ) {
        this.name = name
        this.userId = userId
        this.lastMessage = lastMessage
        this.image = image
        this.seenStatus=seenStatus
        this.origonalMessage=origonalMessage
        this.blockStatus=blockStatus
        this.time=time
        this.deleteTime=deleteTime
    }

    var deleteTime: String? = null
        get() = field
        set(value) {
            field = value
        }

    var time: String? = null
        get() = field
        set(value) {
            field = value
        }
    var likedStatus: String? = null
        get() = field
        set(value) {
            field = value
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

    var blockStatus: String? = null
        get() = field
        set(value) {
            field = value
        }

}