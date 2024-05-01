package com.example.taller3compumovil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.example.taller3compumovil.databinding.ActivityLoginBinding
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var uriCamera: Uri

    private val getContentGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        // Lógica para manejar la imagen seleccionada de la galería
        loadImage(uri)
    }

    private val getContentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Lógica para manejar la imagen capturada por la cámara
            loadImage(uriCamera)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        BotonLogin()
        configurarBotonSiguiente()
    }

    private fun BotonLogin() {
        binding.botonSiguiente.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun configurarBotonSiguiente() {
        binding.edit.setOnClickListener {
            showPopupMenuPhotos(it)
        }
    }

    private fun showPopupMenuPhotos(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_fotos, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_take_photo -> {

                    abrirCamara()
                    true
                }
                R.id.action_select_gallery -> {

                    abrirGaleria()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun abrirCamara() {
        // Solicitar permisos de cámara si aún no están concedidos
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA
            )
        } else {
            // Los permisos de cámara ya están concedidos, abrir la cámara
            val file = File(filesDir, "picFromCamera")
            uriCamera = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            getContentCamera.launch(uriCamera)
        }
    }

    private fun loadImage(uri: Uri?) {
        // Lógica para cargar la imagen en la vista
        uri?.let {
            binding.imagenPerfil.setImageURI(uri)
        }
    }

    private fun abrirGaleria() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_GALLERY)
        } else {
            getContentGallery.launch("image/*")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirCamara()
                } else {
                    // Manejar el caso de que el permiso de la cámara sea denegado
                }
            }
            PERMISSION_REQUEST_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentGallery.launch("image/*")
                } else {
                    // Manejar el caso de que el permiso de galería sea denegado
                }
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CAMERA = 1001
        const val PERMISSION_REQUEST_GALLERY = 1002
    }

}