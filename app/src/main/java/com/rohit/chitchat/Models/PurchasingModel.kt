package com.rohit.chitchat.Models

class PurchasingModel {
    constructor()
    constructor(
        PurchaseType: String,
        itemDistance: String,
        itemPrice: String,
        itemPeriod: String
    ){
        this.PurchaseType=PurchaseType
        this.itemDistance=itemDistance
        this.itemPrice=itemPrice
        this.itemPeriod=itemPeriod
    }

    var PurchaseType: String? = null
        get() = field
        set(value) {
            field = value
        }
    var itemDistance: String? = null
        get() = field
        set(value) {
            field = value
        }
    var itemPrice: String? = null
        get() = field
        set(value) {
            field = value
        }

    var itemPeriod: String? = null
        get() = field
        set(value) {
            field = value
        }
}