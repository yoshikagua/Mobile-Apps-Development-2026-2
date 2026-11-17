package com.example.androidwebservices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidwebservices.databinding.ItemPuntoPosconsumoBinding

class PuntoPosconsumoAdapter :
    ListAdapter<PuntoPosconsumo, PuntoPosconsumoAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPuntoPosconsumoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemPuntoPosconsumoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PuntoPosconsumo) {
            binding.businessName.text = item.razonSocial
            binding.program.text = item.programa
            binding.wasteType.text = item.tipoResiduos
            binding.location.text = item.municipio + " / " + item.departamento
            binding.address.text = item.direccion
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PuntoPosconsumo>() {
            override fun areItemsTheSame(old: PuntoPosconsumo, new: PuntoPosconsumo) =
                old.codigoDane == new.codigoDane && old.direccion == new.direccion

            override fun areContentsTheSame(old: PuntoPosconsumo, new: PuntoPosconsumo) =
                old == new
        }
    }
}
