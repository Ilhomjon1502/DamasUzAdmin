package uz.ilhomjon.damasuzadmin.models

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class Liniya :Serializable{
    var id:String? = null
    var name:String? = null
   var locationListYoli:ArrayList<MyLatLng>? = null
    var faol:Int = 0

    constructor(id: String?, name: String?, locationListYoli: ArrayList<MyLatLng>?) {
        this.id = id
        this.name = name
        this.locationListYoli = locationListYoli
    }

    constructor()

    override fun toString(): String {
        return "Liniya(id=$id, name=$name, locationListYoli=$locationListYoli)"
    }
}
class MyLatLng:Serializable{
    var latitude:Double? = null
    var longitude:Double? = null

    constructor(latitude: Double?, longitude: Double?) {
        this.latitude = latitude
        this.longitude = longitude
    }

    constructor()

    override fun toString(): String {
        return "MyLatLng(latitude=$latitude, longitude=$longitude)"
    }
}