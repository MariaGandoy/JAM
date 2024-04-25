package com.example.amp_jam.ui.theme.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.amp_jam.CamaraActivity
import com.example.amp_jam.MapEventFragment
import com.example.amp_jam.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment : Fragment() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1    }

}
    fun onCreate(savedInstanceState: Bundle?) {

        var takePictureLauncher: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Asegúrate de que hay datos en el resultado
                    if (result.data != null && result.data?.extras?.get("data") != null) {
                        val imageBitmap = result.data?.extras?.get("data") as Bitmap

                        // Comprueba que el Bitmap no es null
                        if (imageBitmap != null) {
                            // Aquí puedes verificar el tamaño del Bitmap, por ejemplo
                            Log.d(
                                "JAM_NAVIGATION",
                                "Width of the captured image: ${imageBitmap.width}"
                            )
                            Log.d(
                                "JAM_NAVIGATION",
                                "Height of the captured image: ${imageBitmap.height}"
                            )

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
                CameraFragment.REQUEST_CAMERA_PERMISSION
            )
        } else {
            // Si ya tenemos permiso hacemos la foto.
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MapEventFragment.REQUEST_CAMERA_PERMISSION) {
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

    private fun setupCameraButtons(view: View) {
        val folderBtn = view.findViewById<ImageButton>(R.id.imageButton2)
        folderBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[Camera] Click FOLDER button")
        }

        val pictureBtn = view.findViewById<ImageButton>(R.id.imageButton4)
        pictureBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[Camera] Click TAKE PICTURE button")
            Toast.makeText(this.context, "Abrir cámara", Toast.LENGTH_SHORT).show()

            val intent = Intent(activity, CamaraActivity::class.java)
            startActivity(intent)
        }

        val filtersBtn = view.findViewById<ImageButton>(R.id.imageButton3)
        filtersBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[Camera] Click FILTERS button")
        }

        val settingsBtn = view.findViewById<ImageButton>(R.id.imageButton5)
        settingsBtn.setOnClickListener {
            Log.d("JAM_NAVIGATION", "[Camera] Click SETTINGS button")
        }
    }
}
