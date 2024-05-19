package com.example.amp_jam

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class GroupCreateDialog : DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private val itemList: MutableList<DocumentSnapshot> = mutableListOf()
    private val firestore = FirebaseFirestore.getInstance()
    private val displayField = "name"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.add_groups, null)

            setupRecyclerView(view)
            loadFriends()

            // Configurando la vista del diálogo y los botones
            builder.setView(view)
                .setPositiveButton("Aceptar") { dialog, id ->
                    val groupName = view.findViewById<EditText>(R.id.eventName).text.toString()
                    val selectedFriendsIds = (recyclerView.adapter as? GenericAdapter)?.selectedFriends?.keys?.toList() ?: listOf()

                    saveGroup(groupName, selectedFriendsIds)
                }
                .setNegativeButton("Cancelar") { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun saveGroup(groupName: String, participants: List<String>) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            val groupData = mapOf(
                "name" to groupName,
                "participantes" to participants
            )

            firestore.collection("usuarios").document(userUid).collection("groups")
                .add(groupData)
                .addOnSuccessListener {
                    Log.d("GroupCreateDialog", "Group successfully created")
                }
                .addOnFailureListener { e ->
                    Log.d("GroupCreateDialog", "Error adding group", e)
                }
        } else {
            Log.d("GroupCreateDialog", "User ID is null, unable to save group")
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = GenericAdapter(itemList, displayField)
    }

    private fun loadFriends() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            firestore.collection("usuarios").document(userUid).collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.d("GroupCreateDialog", "Friends List: []")
                    } else {
                        val friendsList = documents.map { it.id }
                        Log.d("GroupCreateDialog", "Friends List: $friendsList")
                        itemList.clear()
                        friendsList.forEach { friendUid ->
                            firestore.collection("usuarios").document(friendUid)
                                .get()
                                .addOnSuccessListener { friendDocument ->
                                    if (friendDocument.exists()) {
                                        itemList.add(friendDocument)
                                        recyclerView.adapter?.notifyDataSetChanged()
                                    } else {
                                        Log.d("GroupCreateDialog", "Friend document does not exist for ID: $friendUid")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.d("GroupCreateDialog", "Error loading friend document for ID: $friendUid", exception)
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("GroupCreateDialog", "Error loading friends list", exception)
                }
        } else {
            Log.d("GroupCreateDialog", "User ID is null, unable to load friends")
        }
    }




}
