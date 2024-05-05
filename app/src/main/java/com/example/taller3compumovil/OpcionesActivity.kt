package com.example.taller3compumovil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.taller3compumovil.databinding.ActivityOpcionesBinding
class OpcionesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOpcionesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpcionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("prefs_usuario", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token_jwt", null)

        if(token!=null){
            val intent = Intent(baseContext, MapsActivity::class.java)
            startActivity(intent)
        }else{
            BotonLogin()
            BotonRegister()
        }
    }

    private fun BotonLogin() {
        binding.botonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun BotonRegister() {
        binding.botonRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}