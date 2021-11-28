package uz.ilhomjon.damasuzadmin.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import uz.ilhomjon.damasuzadmin.R
import uz.ilhomjon.damasuzadmin.databinding.ItemShopirBinding
import uz.ilhomjon.damasuzadmin.models.Shopir

class SHopirAdapter(val list: List<Shopir>, val onCLick: OnCLick) : RecyclerView.Adapter<SHopirAdapter.Vh>() {

    inner class Vh(var itemRv: ItemShopirBinding) : RecyclerView.ViewHolder(itemRv.root) {
        fun onBind(shopir: Shopir) {
            itemRv.itemName.text = shopir.name
            itemRv.itemTvPhoneNumber.text = shopir.phoneNumber
            itemRv.itemTvAvtoNumber.text = shopir.avtoNumber

            if (shopir.isOnline){
                itemRv.root.setBackgroundColor(Color.parseColor("#42BBF3"))
            }

            itemRv.itemLiniyaMore.setOnClickListener {
                onCLick.popupClick(shopir, itemRv.itemLiniyaMore)
            }
            itemRv.root.setOnClickListener {
                onCLick.rootCLick(shopir)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemShopirBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    interface OnCLick{
        fun popupClick(shopir: Shopir, imageView: ImageView)
        fun rootCLick(shopir: Shopir)
    }
}