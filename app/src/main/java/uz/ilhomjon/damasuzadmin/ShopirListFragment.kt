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
import com.google.firebase.database.*
import uz.ilhomjon.damasuzadmin.adapters.SHopirAdapter
import uz.ilhomjon.damasuzadmin.databinding.FragmentShopirListBinding
import uz.ilhomjon.damasuzadmin.databinding.ItemDialogLiniyaBinding
import uz.ilhomjon.damasuzadmin.databinding.ItemDialogShopirAddBinding
import uz.ilhomjon.damasuzadmin.models.Liniya
import uz.ilhomjon.damasuzadmin.models.Shopir

class ShopirListFragment : Fragment() {

    lateinit var binding:FragmentShopirListBinding

    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var referenceShopir: DatabaseReference
    lateinit var shopirList:ArrayList<Shopir>
    lateinit var liniya:Liniya

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShopirListBinding.inflate(layoutInflater)

        binding.shopirAdd.setOnClickListener {
            val alertDialog = AlertDialog.Builder(context, R.style.NewDialog).create()
            val itemDialogShopirListBinding = ItemDialogShopirAddBinding.inflate(layoutInflater)
            itemDialogShopirListBinding.tvLiniyaName.text = liniya.name
            alertDialog.setView(itemDialogShopirListBinding.root)

            itemDialogShopirListBinding.btnAdd.setOnClickListener {
                val name = itemDialogShopirListBinding.edtName.text.toString().trim()
                val avto = itemDialogShopirListBinding.edtAvtoRaqam.text.toString().trim()
                val phone = itemDialogShopirListBinding.edtPhoneNumber.text.toString().trim()

                if (name != "" && avto != "" && phone != "") {
                    val key = referenceShopir.push().key
                    val shopir = Shopir(key, name, phone, avto, liniya.id)
                    referenceShopir.child(key!!).setValue(shopir)
                    Toast.makeText(context, "$name saqlandi", Toast.LENGTH_SHORT).show()
                    alertDialog.cancel()
                } else {
                    Toast.makeText(
                        context,
                        "Ma'lumotlarni to'liq va to'g'ri kiriting",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            itemDialogShopirListBinding.btnBekor.setOnClickListener { alertDialog.cancel() }
            alertDialog.show()
        }

        firebaseDatabase = FirebaseDatabase.getInstance()
        referenceShopir = firebaseDatabase.getReference("shopir")
        liniya = arguments?.getSerializable("liniya") as Liniya

        referenceShopir.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                shopirList = ArrayList()
                binding.progressSh.visibility = View.GONE
                val children = snapshot.children
                for (child in children) {
                    val value = child.getValue(Shopir::class.java)
                    if (value!=null) {
                        if (liniya.id == value.liniyaId)
                            shopirList.add(value)
                    }
                }

                if (shopirList.isEmpty()){
                    binding.tvEmpty.visibility = View.VISIBLE
                }else{
                    binding.tvEmpty.visibility = View.GONE
                }
                val shopirAdapter = SHopirAdapter(shopirList, object : SHopirAdapter.OnCLick{
                    override fun popupClick(shopir: Shopir, imageView: ImageView) {
                        val popupMenu = PopupMenu(context, imageView)
                        popupMenu.inflate(R.menu.shopir_menu)

                        popupMenu.setOnMenuItemClickListener {

                            when(it.itemId){
                                R.id.edit_drive ->{
                                    val dialog = AlertDialog.Builder(context, R.style.NewDialog).create()
                                    val itemDialogShopirAddBinding = ItemDialogShopirAddBinding.inflate(layoutInflater)
                                    dialog.setView(itemDialogShopirAddBinding.root)

                                    itemDialogShopirAddBinding.tvLiniyaName.text = liniya.name
                                    itemDialogShopirAddBinding.edtName.setText(shopir.name)
                                    itemDialogShopirAddBinding.edtAvtoRaqam.setText(shopir.avtoNumber)
                                    itemDialogShopirAddBinding.edtPhoneNumber.setText(shopir.phoneNumber)

                                    itemDialogShopirAddBinding.btnAdd.setOnClickListener {
                                        val name = itemDialogShopirAddBinding.edtName.text.toString().trim()
                                        val phoneNumber = itemDialogShopirAddBinding.edtPhoneNumber.text.toString().trim()
                                        val avtoRaqam = itemDialogShopirAddBinding.edtAvtoRaqam.text.toString().trim()

                                        if (name!="" && phoneNumber!="" && avtoRaqam!=""){
                                            shopir.name = name
                                            shopir.phoneNumber = phoneNumber
                                            shopir.avtoNumber = avtoRaqam

                                            referenceShopir.child(shopir.id!!).setValue(shopir)
                                            Toast.makeText(context, "Ma'lumot o'zgartirildi", Toast.LENGTH_SHORT).show()
                                            dialog.cancel()
                                        }
                                    }

                                    itemDialogShopirAddBinding.btnBekor.setOnClickListener { dialog.cancel() }

                                    dialog.show()
                                }
                                R.id.delete_drive->{
                                    val alertDialog = AlertDialog.Builder(context)
                                    alertDialog.setTitle("Ushbu haydovchi o'chirilsinmi?")
                                    alertDialog.setPositiveButton("Ha"
                                    ) { dialog, which ->
                                        referenceShopir.child(shopir.id!!).removeValue()
                                        Toast.makeText(context, "${shopir.name} o'chirildi", Toast.LENGTH_SHORT).show()
                                    }
                                    alertDialog.setNegativeButton("Yo'q"
                                    ) { dialog, which -> }

                                    alertDialog.show()
                                }
                            }
                            true
                        }

                        popupMenu.show()
                    }
                    override fun rootCLick(shopir: Shopir) {
                        findNavController().navigate(R.id.mapsFragment, bundleOf("keyShopir" to shopir, "keyLiniya" to liniya))
                    }
                })
                binding.rvShopir.adapter = shopirAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        return binding.root
    }
}