package com.example.amp_jam

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Calendar
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat


class MapEventFragment: DialogFragment() {

    interface MapEventDialogListener {
        fun onEventSubmitted(data: Post)
    }

    var listener: MapEventDialogListener? = null

    private lateinit var auth: FirebaseAuth

    private var currentUser: FirebaseUser? = null

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

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


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.map_event, null)

            // Retrieve current user
            auth = FirebaseAuth.getInstance()
            currentUser = auth.currentUser

            setUpExitButton(view)
            setUpNameEventListener(view)
            setUpDatePickerDialog(view)
            setUpRadioGroupListener(view)
            setupPhotoButtons(view)

            builder.setView(view)
                .setPositiveButton("AÑADIR") { _, _ ->
                    Log.d("JAM_NAVIGATION", "[MapEvent] Add event")
                    val eventData = setEventData(view)
                    submitEvent(eventData)
                }
                .setNegativeButton("CANCELAR") { _, _ ->
                    Log.d("JAM_NAVIGATION", "[MapEvent] Cancel event")
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setUpExitButton(view: View) {
        val exitBtn = view.findViewById<ImageButton>(R.id.exitButton)
        exitBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[MapEvent] Click EXIT button")
        }
    }

    private fun setUpNameEventListener(view: View) {
        val textInputName = view.findViewById<EditText>(R.id.eventName)
        textInputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("JAM_NAVIGATION", "[MapEvent] Event name text changed to: $s")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setUpDatePickerDialog(view: View) {
        val textInputDate = view.findViewById<EditText>(R.id.eventDate)
        textInputDate.setOnClickListener{
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, yearValue, monthValue, dayValue ->
                    textInputDate.setText("$dayValue/$monthValue/$yearValue")
                    Log.d(
                        "JAM_NAVIGATION",
                        "[MapEvent] Event date changed to: $dayValue/$monthValue/$yearValue"
                    )
                },
                year, month, day
            )

            datePickerDialog.show()
        }
    }

    private fun setUpRadioGroupListener(view: View) {
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = view.findViewById<RadioButton>(checkedId)
            Log.d("JAM_NAVIGATION", "[MapEvent] Notify radio group selection changed to: $checkedId")
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
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            // Si ya tenemos permiso hacemos la foto.
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
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

    private fun setEventData(view: View): Post {
        val eventName = view.findViewById<EditText>(R.id.eventName).text.toString()
        val eventDate = view.findViewById<EditText>(R.id.eventDate).text.toString()
        val eventType = "EVENT" // TODO: add SONG and PHOTO
        val userEmail = currentUser?.email ?: ""

        return Post(eventName, eventDate, eventType, userEmail, null, null)
    }

    private fun submitEvent(data: Post) {
        listener?.onEventSubmitted(data)
        dismiss()
    }
}