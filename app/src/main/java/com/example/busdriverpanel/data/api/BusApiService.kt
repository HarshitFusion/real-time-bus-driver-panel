package com.example.busdriverpanel.data.api

import com.example.busdriverpanel.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface BusApiService {
    
    @POST("driver/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    @POST("bus/location")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Body locationUpdate: LocationUpdateRequest
    ): Response<ApiResponse<String>>
    
    @GET("bus/{busId}/status")
    suspend fun getBusStatus(
        @Header("Authorization") token: String,
        @Path("busId") busId: String
    ): Response<ApiResponse<BusInfo>>
    
    @POST("bus/{busId}/status")
    suspend fun updateBusStatus(
        @Header("Authorization") token: String,
        @Path("busId") busId: String,
        @Body status: Map<String, String>
    ): Response<ApiResponse<String>>
}
