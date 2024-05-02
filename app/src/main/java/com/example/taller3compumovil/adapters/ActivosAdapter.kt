package com.example.taller3compumovil.adapters

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import com.example.taller3compumovil.databinding.DisponiblestrowBinding

class ActivosAdapter(context: Context?, c: Cursor?, flags: Int) :
    CursorAdapter(context, c, flags) {

    private class ViewHolder(binding: DisponiblestrowBinding) {
        val imageView = binding.perfilImg
        val nameTextView = binding.nombre
        val actionButton = binding.boton
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val binding = DisponiblestrowBinding.inflate(inflater, parent, false)
        val holder = ViewHolder(binding)
        binding.root.tag = holder
        return binding.root
    }

    override fun bindView(view: View, context: Context?, cursor: Cursor?) {
        val holder = view.tag as ViewHolder  // Recuperar el holder desde el tag de la vista

        val idIndex = cursor?.getColumnIndex("profile_id") ?: -1
        val nameIndex = cursor?.getColumnIndex("name") ?: -1
        val imageIndex = cursor?.getColumnIndex("profile_image") ?: -1

        if (cursor != null && idIndex != -1 && nameIndex != -1 && imageIndex != -1) {
            val userName = cursor.getString(nameIndex)
            val userProfileImage = cursor.getString(imageIndex)

            holder.nameTextView.text = userName
            holder.imageView.setImageURI(Uri.parse(userProfileImage))  // Asegurense de tener permiso para leer desde el URI

            holder.actionButton.setOnClickListener {
                // Aquí  manejar eventos del botón
            }
        }
    }
}
