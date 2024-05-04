package com.example.taller3compumovil

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3compumovil.adapters.ActivosAdapter
import com.example.taller3compumovil.databinding.ActivityDisponiblesBinding

class DisponiblesActivity : AppCompatActivity() {
    private lateinit var adapter: ActivosAdapter
    private lateinit var binding: ActivityDisponiblesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisponiblesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
        adapter = ActivosAdapter(this, null, 0)
        binding.listaActivos.adapter = adapter
        loadUsuarios()
    }

    private fun loadUsuarios() {
        // Aca hagan lo de jalar
        val cursor = null //eto borrenlo cuando pongan lo otro
        //val cursor = contentResolver.query(/* URI de tu base de datos */, null, "active = 1", null, null)
        if (cursor != null) {
            adapter.swapCursor(cursor)
        } else {
            // Manejar error de carga de datos
        }
    }
}
