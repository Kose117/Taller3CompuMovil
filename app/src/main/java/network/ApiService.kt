package network

import models.LoginRequest
import models.LoginResponse
import models.RegisterRequest
import models.availabilityResponse
import models.availabilityState
import models.availabilityStateRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import util.Authorized

interface ApiService {
    @POST("user/login/")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>


    @POST("user/create/")
    fun registerUser(@Body request: RegisterRequest): Call<LoginResponse>

    @Authorized
    @GET("user/availability/")
    fun getAvailableUsers():Call<availabilityResponse>

    @Authorized
    @PUT("user/availability")
    fun changeAvailability(@Body request: availabilityStateRequest): Call<availabilityState>
}