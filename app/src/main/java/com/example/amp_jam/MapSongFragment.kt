package com.example.amp_jam

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


/**
 * A simple [Fragment] subclass.
 * Use the [MapEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapSongFragment() : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var groupsView: RecyclerView
    private val itemList: MutableList<DocumentSnapshot> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.map_song, container, false)

        setUpSongTextListener(view)
        setupGroupsView(view)
        loadGroups()

        return view
    }

    private fun setUpSongTextListener(view: View) {
        val textInputSong = view.findViewById<EditText>(R.id.songName)
        textInputSong.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("JAM_NAVIGATION", "[MapSong] Song text changed to: $s")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupGroupsView(view: View) {
        groupsView = view.findViewById<RecyclerView>(R.id.groupsView)
        groupsView.layoutManager = LinearLayoutManager(context)

        groupsView.adapter = GroupsAdapter(itemList, "name")
    }


    private fun loadGroups() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            itemList.clear()
            firestore.collection("usuarios").document(userUid).collection("groups")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.d("GroupCreateDialog", "Groups List: []")
                    } else {
                        for (groupDocument in documents) {
                            itemList.add(groupDocument)
                            groupsView.adapter?.notifyDataSetChanged()
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