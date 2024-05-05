package network

import models.LoginRequest
import models.LoginResponse
import models.RegisterRequest
import models.availabilityResponse
import models.availabilityState
import models.availabilityStateRequest
import models.user.defaultResponse
import models.user.locationRequest
import models.user.uploadImageResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
    @PUT("user/availability/")
    fun changeAvailability(@Body request: availabilityStateRequest): Call<availabilityState>

    @Authorized
    @PUT("user/location/")
    fun updateUserLocation(@Body request: locationRequest): Call<defaultResponse>


    @Authorized
    @POST("user/upload/picture")
    fun uploadImage(@Part image: MultipartBody.Part): Call<uploadImageResponse>

}