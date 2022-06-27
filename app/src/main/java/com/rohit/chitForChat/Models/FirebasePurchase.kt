package com.rohit.chitForChat.Models

class FirebasePurchase {
    constructor()
    constructor(
        purchaseType: String,
        amount: String,
        startTime: String,
        endTime:String
    ){
        this.purchaseType=purchaseType
        this.amount=amount
        this.startTime=startTime
        this.endTime=endTime
    }

    var purchaseType: String? = null
        get() = field
        set(value) {
            field = value
        }
    var amount: String? = null
        get() = field
        set(value) {
            field = value
        }

    var startTime: String? = null
        get() = field
        set(value) {
            field = value
        }
    var endTime: String? = null
        get() = field
        set(value) {
            field = value
        }
}