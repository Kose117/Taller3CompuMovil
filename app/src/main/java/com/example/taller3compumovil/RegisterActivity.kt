package com.example.taller3compumovil

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.taller3compumovil.databinding.ActivityRegisterBinding
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import models.LoginResponse
import models.RegisterRequest
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var uriCamera: Uri

    private var imagen: String? = null

    private val getContentGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
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
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.botonSiguiente.setOnClickListener {
            var name = binding.nombre.text.toString()
            var lastname = binding.apellido.text.toString()
            var email = binding.mail.text.toString()
            var password = binding.contra.text.toString()
            val cc = binding.numID.text.toString()

            if(name.isNotEmpty() && lastname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()){
                register(name, lastname, email, password, cc)
            }
        }

        configurarBotonSiguiente()
    }

    private fun register(name: String, lastname: String, email: String, password: String, cc: String) {
        val registerRequest = RegisterRequest(name, lastname, email, password, cc)

        RetrofitClient.create(applicationContext).registerUser(registerRequest).enqueue(object : Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>){
                if(response.isSuccessful){
                    val token = response.body()?.token
                    Log.i("AUTH TOKEN", token.toString())
                    if(token != null){
                        guardarToken(token)


                        val intent = Intent(baseContext, MapsActivity::class.java)

                        intent.putExtra("imagen", imagen)

                        startActivity(intent)
                    }
                }else{
                    Toast.makeText(this@RegisterActivity, "Server Internal Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun guardarToken(token: String) {
        val sharedPreferences = getSharedPreferences("prefs_usuario", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("token_jwt", token)
            apply()
        }
    }
    ///storage/emulated/0/Android/data/com.example.taller3compumovil/files/Pictures/imagen_perfil.jpg
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

    private fun abrirGaleria() {
        getContentGallery.launch("image/*")
    }


    private fun loadImage(uri: Uri?) {
        uri?.let {
            imagen = saveImageToExternalStorage(BitmapFactory.decodeStream(contentResolver.openInputStream(it)))
            Log.i("IMG", "Imagen guardada en: $imagen")
            binding.imagenPerfil.setImageURI(uri)
        }
    }

    private fun saveImageToExternalStorage(bitmap: Bitmap): String? {
        val filename = "imagen_perfil.jpg"
        var fos: FileOutputStream? = null
        var fileUri: String? = null
        try {
            val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(dir, filename)

            // Redimensiona la imagen al tamaño deseado (720p)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 1280, 720, true)

            fos = FileOutputStream(file)

            // Comprime y guarda la imagen redimensionada
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)

            fos.flush()
            fos.close()
            fileUri = file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fos?.close()
        }
        return fileUri
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