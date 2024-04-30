package com.example.amp_jam.ui.theme.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.amp_jam.AddFriendsActivity
import com.example.amp_jam.MainActivity
import com.example.amp_jam.Post
import com.example.amp_jam.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var locationSwitch: Switch
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupLogOut(view)
        setupAddFriends(view)
        loadUserProfileData(view)

        return view
    }


    private fun setupLogOut(view: View) {
        val toolbarLogout = view.findViewById<ImageView>(R.id.toolbarLogout)
        toolbarLogout.setOnClickListener {
            Log.d("ProfileFragment", Firebase.auth.currentUser.toString())
            Firebase.auth.signOut()

            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()

            Log.d("ProfileFragment", Firebase.auth.currentUser.toString())
        }
    }


    private fun setupAddFriends(view: View) {
        val addBtn = view.findViewById<Button>(R.id.button5)
        addBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[ProfileFragment] Click ADD FRIENDS MENU EVENT button")
            val intent = Intent(activity, AddFriendsActivity::class.java)
            startActivity(intent)

        }
    }

    private fun loadUserProfileData(view: View) {
        val currentUser = auth.currentUser
        val userNameTextView = view.findViewById<TextView>(R.id.textView3)
        val profileImageView = view.findViewById<ImageView>(R.id.imageView3)
        val postsTextView = view.findViewById<TextView>(R.id.postsTextView)

        if (currentUser != null) {
            // Load user profile picture and name
            currentUser.photoUrl?.let {
                Glide.with(requireContext())
                    .load(it)
                    .placeholder(R.drawable.logo)
                    .into(profileImageView)
            } ?: profileImageView.setImageResource(R.drawable.logo)

            // Load user name
            firestore.collection("usuarios").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    userNameTextView.text = name ?: "@usuario10"
                }
                .addOnFailureListener { exception ->
                    Log.d("ProfileFragment", "get failed with ", exception)
                    userNameTextView.text = "@usuario10"
                }

            // Load posts
            firestore.collection("usuarios").document(currentUser.uid).collection("posts")
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val stringBuilder = StringBuilder()
                        for (document in documents) {
                            val title = document.getString("titulo") ?: "Sin título"
                            val date = document.getString("fecha") ?: "Fecha desconocida"
                            val tipo = document.getString("tipo") ?: "Tipo no especificado"
                            val user = document.getString("user") ?: "Usuario desconocido"
                            stringBuilder.append("Título: $title\nFecha: $date\nTipo: $tipo\nUsuario: $user\n\n")
                        }
                        postsTextView.text = stringBuilder.toString()
                    } else {
                        postsTextView.text = "No tienes actividad reciente"
                    }
                }
                .addOnFailureListener {
                    postsTextView.text = "Error al cargar los posts"
                    Log.d("ProfileFragment", "Error loading posts", it)
                }
        } else {
            userNameTextView.text = "@usuario10"
            postsTextView.text = "No tienes actividad reciente"
        }
    }

    private fun setupLocationSwitch(view: View) {
        locationSwitch = view.findViewById(R.id.switch1)
        updateSwitchBasedOnPermissions()

        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestLocationPermissions()
            } else {
                // Opcional: Manejar la desactivación de permisos si es necesario
                Toast.makeText(context, "Ubicación desactivada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSwitchBasedOnPermissions() {
        locationSwitch.isChecked = (
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                )
    }

    private fun requestLocationPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Mostrar una explicación al usuario *asincrónicamente*
            Toast.makeText(context, "Se requiere permiso de ubicación para esta funcionalidad", Toast.LENGTH_LONG).show()
        } else {
            // No se necesita explicación, podemos solicitar el permiso.
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso concedido, realizar la tarea relacionada con la ubicación
                    Toast.makeText(context, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
                } else {
                    // Permiso denegado, desactivar la funcionalidad relacionada con la ubicación
                    Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                    locationSwitch.isChecked = false
                }
                return
            }
            // Otros casos del 'when' para otros permisos que este app podría solicitar
            else -> {
                // Ignorar todas las otras solicitudes.
            }
        }
    }





}
