package com.example.taller3compumovil.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taller3compumovil.databinding.DisponiblestrowBinding
import models.User

class ActivosAdapter(private val context: Context, private val buttonClickListener: OnButtonClickListener) : RecyclerView.Adapter<ActivosAdapter.UserViewHolder>() {

    private var userList: List<User>? = null

    interface OnButtonClickListener {
        fun onButtonClick(user: User)
    }

    inner class UserViewHolder(private val binding: DisponiblestrowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                nombre.text = "${user.name} ${user.lastname}"

                // Verifica si la URL de la imagen de perfil no es nula antes de intentar establecerla en el ImageView
                user.profile_picture?.let { imageUrl ->
                    perfilImg.setImageURI(Uri.parse(imageUrl)) // Asegúrate de tener permiso para leer desde el URI
                }
                boton.setOnClickListener {
                    buttonClickListener.onButtonClick(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = DisponiblestrowBinding.inflate(inflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList?.get(position)
        user?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return userList?.size ?: 0
    }

    fun setUsers(users: List<User>?) {
        userList = users
        notifyDataSetChanged() // Notificar al RecyclerView que los datos han cambiado
    }
}
