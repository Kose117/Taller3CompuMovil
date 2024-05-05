package com.example.taller3compumovil

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.taller3compumovil.databinding.ActivityLoginBinding
import models.LoginRequest
import models.LoginResponse
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.botonSiguiente.setOnClickListener {
            val email = binding.mail.text.toString()
            val password = binding.contra.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                login(email, password)
            }
        }
    }

    private fun login(email: String, password: String){
        val loginRequest = LoginRequest(email, password)

        RetrofitClient.create(applicationContext).loginUser(loginRequest).enqueue(object :
            Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>){
                if(response.isSuccessful){
                    val token = response.body()?.token
                    Log.i("AUTH TOKEN", token ?: "null")
                    if(token != null){
                        guardarToken(token)
                        val intent = Intent(this@LoginActivity, MapsActivity::class.java)
                        startActivity(intent)
                    }
                }else{
                    Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error en la conexión", Toast.LENGTH_SHORT).show()
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
}