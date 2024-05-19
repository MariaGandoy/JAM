package com.example.amp_jam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.amp_jam.databinding.ListItemBinding
import com.google.firebase.firestore.DocumentSnapshot

// Inicialmente intenté hacer un adaptador para reutilizarlo con el listado que vamos a
// tener que hacer de checkboxes en la parte de seleccionar grupos para crear el evento
// pero me dio muchos errores y al final lo hice para Friends solo

class FriendsAdapter(private val items: List<DocumentSnapshot>, private val displayField: String) : RecyclerView.Adapter<FriendsAdapter.ItemViewHolder>() {
    // Map para almacenar los IDs de los amigos seleccionados
    val selectedFriends = mutableMapOf<String, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, displayField)

        holder.binding.checkBox.isChecked = selectedFriends[item.id] == true

        // Escuchar checkbox de selección
        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedFriends[item.id] = true
            } else {
                selectedFriends.remove(item.id)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class ItemViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DocumentSnapshot, displayField: String) {
            binding.checkBox.text = item.getString(displayField) ?: "Unknown"
        }
    }
}
