package com.example.amp_jam

import android.Manifest
import android.app.Activity
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
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
class MapPhotoFragment() : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private lateinit var groupsView: RecyclerView
    private val itemList: MutableList<DocumentSnapshot> = mutableListOf()

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Asegúrate de que hay datos en el resultado
                if (result.data != null && result.data?.extras?.get("data") != null) {
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap

                    // Comprueba que el Bitmap no es null
                    if (imageBitmap != null) {
                        // Aquí puedes verificar el tamaño del Bitmap, por ejemplo
                        Log.d("JAM_NAVIGATION", "Width of the captured image: ${imageBitmap.width}")
                        Log.d("JAM_NAVIGATION", "Height of the captured image: ${imageBitmap.height}")

                        // Actualiza el ImageButton
                        val cameraButton = view?.findViewById<ImageButton>(R.id.addFromCamera)
                        cameraButton?.setImageBitmap(imageBitmap)
                        Log.d("JAM_NAVIGATION", "[MapEvent] Image captured and displayed.")
                    } else {
                        // El Bitmap es null
                        Log.d("JAM_NAVIGATION", "Captured image is null.")
                    }
                } else {
                    // Los datos del resultado son null
                    Log.d("JAM_NAVIGATION", "Result data from camera is null.")
                }
            } else {
                Log.d("JAM_NAVIGATION", "Failed to capture image.")
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val imageView = view?.findViewById<ImageButton>(R.id.addFromFiles)
                val imageBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
                imageView?.setImageBitmap(imageBitmap)
                Log.d("JAM_NAVIGATION", "[MapEvent] Image selected")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.map_photo, container, false)

        setupPhotoButtons(view)
        setupGroupsView(view)
        loadGroups()

        return view
    }

    private fun setupGroupsView(view: View) {
        groupsView = view.findViewById<RecyclerView>(R.id.groupsView)
        groupsView.layoutManager = LinearLayoutManager(context)

        groupsView.adapter = GroupsAdapter(itemList, "name")
    }

    private fun setupPhotoButtons(view: View) {
        val cameraBtn = view.findViewById<ImageButton>(R.id.addFromCamera)
        cameraBtn.setOnClickListener {
            openCamera()
        }

        val galleryBtn = view.findViewById<ImageButton>(R.id.addFromFiles)
        galleryBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no tenemos el permiso lo solicitamos
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                MapPhotoFragment.REQUEST_CAMERA_PERMISSION
            )
        } else {
            // Si ya tenemos permiso hacemos la foto.
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MapPhotoFragment.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, lanzar la cámara
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureLauncher.launch(takePictureIntent)
            } else {
                // Permiso denegado
                Log.d("JAM_NAVIGATION", "[MapEvent] Camera permission was denied")
            }
        }
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