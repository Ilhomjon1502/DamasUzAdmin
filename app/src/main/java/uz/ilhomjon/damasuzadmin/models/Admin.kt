package uz.ilhomjon.damasuzadmin.models

class Admin {
    var name:String? = null
    var number:String? = null

    constructor( name: String?, number: String?) {
        this.name = name
        this.number = number
    }

    constructor()

    override fun toString(): String {
        return "Admin(name=$name, number=$number)"
    }
}