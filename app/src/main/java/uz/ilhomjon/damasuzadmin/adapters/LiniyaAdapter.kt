package uz.ilhomjon.damasuzadmin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import uz.ilhomjon.damasuzadmin.databinding.ItemLiniyaBinding
import uz.ilhomjon.damasuzadmin.models.Liniya
import uz.ilhomjon.damasuzadmin.models.Shopir

class LiniyaAdapter(val list: List<Liniya>, val shList:List<Shopir>, val rvLiniyaClick: RvLiniyaClick) : RecyclerView.Adapter<LiniyaAdapter.Vh>() {

    inner class Vh(var itemRv: ItemLiniyaBinding) : RecyclerView.ViewHolder(itemRv.root) {
        fun onBind(liniya: Liniya) {
            itemRv.itemName.text = liniya.name

            var count = 0
            var all = 0
            for (sHopir in shList) {
                if (sHopir.liniyaId==liniya.id){
                    all++
                    if (sHopir.isOnline){
                        count++
                    }
                }
            }
            itemRv.itemTvFaol.text = count.toString()
            itemRv.itemTvJami.text = all.toString()

            itemRv.root.setOnClickListener {
                rvLiniyaClick.rootClick(liniya)
            }
            itemRv.itemLiniyaMore.setOnClickListener {
                rvLiniyaClick.moreClick(liniya, itemRv.itemLiniyaMore)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemLiniyaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    interface RvLiniyaClick{
        fun rootClick(liniya: Liniya)
        fun moreClick(liniya: Liniya, imageView: ImageView)
    }
}