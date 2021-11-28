package uz.ilhomjon.damasuzadmin.models

import java.io.Serializable

class Shopir : Serializable{
    var id:String? = null
    var name:String? = null
    var phoneNumber:String? = null
    var avtoNumber:String? = null
    var liniyaId:String? = null
    var boshJoy:Int = 0
    var location:MyLatLng? = null
    var isOnline = false


    constructor()
    constructor(
        id: String?,
        name: String?,
        phoneNumber: String?,
        avtoNumber: String?,
        boshJoy: Int,
        liniyaId: String?,
        location: MyLatLng?
    ) {
        this.id = id
        this.name = name
        this.phoneNumber = phoneNumber
        this.avtoNumber = avtoNumber
        this.boshJoy = boshJoy
        this.liniyaId = liniyaId
        this.location = location
    }

    constructor(
        id: String?,
        name: String?,
        phoneNumber: String?,
        avtoNumber: String?,
        liniyaId: String?
    ) {
        this.id = id
        this.name = name
        this.phoneNumber = phoneNumber
        this.avtoNumber = avtoNumber
        this.liniyaId = liniyaId
    }

    override fun toString(): String {
        return "Shopir(id=$id, name=$name, phoneNumber=$phoneNumber, avtoNumber=$avtoNumber, boshJoy=$boshJoy, liniyaId=$liniyaId, location=$location)"
    }


}