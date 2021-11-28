package uz.ilhomjon.damasuzadmin.models

import java.io.Serializable

class Yolovchi : Serializable{
    var id:String? = null
    var name:String? = null
    var number:String? = null
    var location:MyLatLng? = null
    var liniyaId:String? = null


    constructor()
    constructor(id: String?, name: String?, number: String?) {
        this.id = id
        this.name = name
        this.number = number
    }

    constructor(id: String?, name: String?, number: String?, location: MyLatLng?) {
        this.id = id
        this.name = name
        this.number = number
        this.location = location
    }
}