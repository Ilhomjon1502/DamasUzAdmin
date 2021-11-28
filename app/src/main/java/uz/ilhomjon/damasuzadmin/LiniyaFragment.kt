package uz.ilhomjon.damasuzadmin

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import uz.ilhomjon.damasuzadmin.adapters.LiniyaAdapter
import uz.ilhomjon.damasuzadmin.databinding.FragmentLiniyaBinding
import uz.ilhomjon.damasuzadmin.databinding.ItemDialogLiniyaBinding
import uz.ilhomjon.damasuzadmin.models.Liniya
import uz.ilhomjon.damasuzadmin.models.MyLatLng
import uz.ilhomjon.damasuzadmin.models.Shopir

class LiniyaFragment : Fragment() {

    lateinit var binding: FragmentLiniyaBinding
    lateinit var liniyaList: ArrayList<Liniya>

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var referenceLiniya: DatabaseReference
    lateinit var referenceShopir: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLiniyaBinding.inflate(layoutInflater)

        binding.liniyaAdd.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context, R.style.NewDialog).create()
            val itemLiniyaBinding = ItemDialogLiniyaBinding.inflate(layoutInflater)
            alertDialog.setView(itemLiniyaBinding.root)

            itemLiniyaBinding.btnAddLiniya.setOnClickListener {
                val name = itemLiniyaBinding.edtNameDialog.text.toString().trim()
                var has = false
                for (liniya in liniyaList) {
                    if (liniya.name == name) {
                        has = true
                        Toast.makeText(
                            context,
                            "$name nomli liniya avval yaratilgan",
                            Toast.LENGTH_SHORT
                        ).show()
                        break
                    }
                }
                if (name != "" && name.length < 30 && !has) {
                    findNavController().navigate(
                        R.id.addLiniyaMapsFragment,
                        bundleOf("name" to name)
                    )
                    alertDialog.cancel()
                } else {
                    Toast.makeText(context, "Liniya nomini to'g'ri kiriting", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            itemLiniyaBinding.btnBekor.setOnClickListener { alertDialog.cancel() }
            alertDialog.show()
        }

        firebaseDatabase = FirebaseDatabase.getInstance()
        referenceLiniya = firebaseDatabase.getReference("liniya")
        referenceShopir = firebaseDatabase.getReference("shopir")

        binding.progressLiniya.visibility = View.VISIBLE

        referenceLiniya.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                liniyaList = ArrayList<Liniya>()

                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Liniya::class.java)
                    if (value != null) {
                        liniyaList.add(value)
                    }
                }
                binding.progressLiniya.visibility = View.GONE
                if (liniyaList.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                }

                referenceShopir.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val shList = ArrayList<Shopir>()
                        val children1 = snapshot.children
                        for (child in children1) {
                            val value = child.getValue(Shopir::class.java)
                            if (value != null) {
                                shList.add(value)
                            }
                        }

                        val liniyaAdapter = LiniyaAdapter(liniyaList, shList, object : LiniyaAdapter.RvLiniyaClick{
                            override fun rootClick(liniya: Liniya) {
                                findNavController().navigate(R.id.shopirListFragment, bundleOf("liniya" to liniya))
                            }

                            override fun moreClick(liniya: Liniya, imageView: ImageView) {
                                val popupMenu = PopupMenu(context, imageView)
                                popupMenu.inflate(R.menu.liniya_menu)
                                popupMenu.setOnMenuItemClickListener {

                                    when(it.itemId){
                                        R.id.lin_nom_ozgartirish ->{
                                            val alertDialog = AlertDialog.Builder(context).create()
                                            val itemDialogLiniyaBinding = ItemDialogLiniyaBinding.inflate(layoutInflater)
                                            itemDialogLiniyaBinding.btnAddLiniya.setText("O'zgartirish")
                                            alertDialog.setView(itemDialogLiniyaBinding.root)
                                            itemDialogLiniyaBinding.edtNameDialog.setText(liniya.name)
                                            itemDialogLiniyaBinding.btnAddLiniya.setOnClickListener {
                                                val name = itemDialogLiniyaBinding.edtNameDialog.text.toString().trim()
                                                if (name!=""){
                                                    liniya.name = name
                                                    referenceLiniya.child(liniya.id!!).setValue(liniya)
                                                    Toast.makeText(context, "Nom o'zgartirildi", Toast.LENGTH_SHORT).show()
                                                    alertDialog.cancel()
                                                }else {
                                                    Toast.makeText(
                                                        context,
                                                        "Nomni to'g'ri kiriting",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            itemDialogLiniyaBinding.btnBekor.setOnClickListener { alertDialog.cancel() }
                                            alertDialog.show()
                                        }
                                        R.id.liniya_hay_roy ->{
                                            findNavController().navigate(R.id.shopirListFragment, bundleOf("liniya" to liniya))
                                        }
                                        R.id.liniya_ochirish->{
                                            val dialog = AlertDialog.Builder(context)
                                            dialog.setTitle("O'chirilsinmi?")
                                            dialog.setMessage("Agar hozir ${liniya.name} ni o'chirsangiz undagi barcha haydovchilar o'chib ketadi")

                                            dialog.setPositiveButton("Ha roziman"
                                            ) { dialog, which ->
                                                referenceLiniya.child(liniya.id!!).removeValue()
                                                Toast.makeText(context, "${liniya.name} liniya o'chirildi", Toast.LENGTH_SHORT).show()
                                            }
                                            dialog.setNegativeButton("Yo'q"
                                            ) { dialog, which -> }

                                            dialog.show()
                                        }
                                        R.id.liniya_xarita_tahrir->{
                                            findNavController().navigate(R.id.addLiniyaMapsFragment, bundleOf("key" to 1, "liniya" to liniya))
                                        }
                                    }

                                    true
                                }
                                popupMenu.show()
                            }
                        })

                        binding.rvLiniya.adapter = liniyaAdapter

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Internetga qayta ulaning \n${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        return binding.root
    }
}

fun latLngToMyLatLng(latLngList: ArrayList<LatLng>): ArrayList<MyLatLng> {

    val list = ArrayList<MyLatLng>()
    for (latLng in latLngList) {
        list.add(MyLatLng(latLng.latitude, latLng.longitude))
    }
    return list
}

fun myLatLngToLatLng(myLatLngList: ArrayList<MyLatLng>): ArrayList<LatLng> {
    val list = ArrayList<LatLng>()
    for (myLatLng in myLatLngList) {
        list.add(LatLng(myLatLng.latitude!!, myLatLng.longitude!!))
    }
    return list
}