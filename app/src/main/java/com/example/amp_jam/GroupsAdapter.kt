package com.example.amp_jam

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.amp_jam.databinding.ListItemBinding
import com.google.firebase.firestore.DocumentSnapshot

class GroupsAdapter(private val items: List<DocumentSnapshot>, private val displayField: String) : RecyclerView.Adapter<GroupsAdapter.ItemViewHolder>() {
    // Map para almacenar los IDs de los amigos seleccionados
    val selectedGroups = mutableMapOf<String, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, displayField)

        holder.binding.checkBox.isChecked = selectedGroups[item.id] == true

        // Escuchar checkbox de selección
        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val groupFriends = item["participantes"] as List<String>

            if (isChecked) {
                for (friendId in groupFriends) {
                    selectedGroups[friendId] = true
                }
            } else {
                for (friendId in groupFriends) {
                    selectedGroups.remove(friendId)
                }
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