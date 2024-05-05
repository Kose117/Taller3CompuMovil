package network

import models.LoginRequest
import models.LoginResponse
import models.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("user/login/")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>


    @POST("user/create/")
    fun registerUser(@Body request: RegisterRequest): Call<LoginResponse>
}