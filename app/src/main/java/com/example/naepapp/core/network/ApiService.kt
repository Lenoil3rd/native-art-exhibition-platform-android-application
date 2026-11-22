package com.example.naepapp.core.network

import com.example.naepapp.core.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface RefreshApi {
    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Envelope<TokenPair>
}

interface ApiService {
    @GET("health")
    suspend fun health(): Response<Unit>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Envelope<AuthResult>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Envelope<AuthResult>

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>

    @GET("me")
    suspend fun getMe(): Envelope<UserDto>

    @PATCH("me")
    suspend fun updateMe(@Body request: UpdateMeRequest): Envelope<UserDto>

    @PUT("me")
    suspend fun updateMePut(@Body request: UpdateMeRequest): Envelope<UserDto>

    @PATCH("users/me")
    suspend fun updateMeUsersPatch(@Body request: UpdateMeRequest): Envelope<UserDto>

    @PUT("users/me")
    suspend fun updateMeUsersPut(@Body request: UpdateMeRequest): Envelope<UserDto>

    @POST("me/onboarding")
    suspend fun postOnboarding(@Body request: OnboardingRequest): Envelope<UserDto>

    @GET("me/creations")
    suspend fun getMyCreations(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null
    ): Envelope<List<CreationSummaryDto>>

    @GET("categories")
    suspend fun getCategories(): Envelope<List<CategoryDto>>

    @GET("creations")
    suspend fun getCreations(
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null
    ): Envelope<List<CreationSummaryDto>>

    @GET("creations/{id}")
    suspend fun getCreationDetail(@Path("id") id: String): Envelope<CreationDetailDto>

    @POST("creations")
    suspend fun createCreation(@Body request: CreationRequest): Envelope<CreationDetailDto>

    @PATCH("creations/{id}")
    suspend fun updateCreation(
        @Path("id") id: String,
        @Body request: CreationRequest
    ): Envelope<CreationDetailDto>

    @DELETE("creations/{id}")
    suspend fun deleteCreation(@Path("id") id: String): Response<Unit>

    @POST("creations/{id}/view")
    suspend fun viewCreation(@Path("id") id: String): Response<Unit>

    @PUT("creations/{id}/like")
    suspend fun likeCreation(@Path("id") id: String): Envelope<LikeResult>

    @DELETE("creations/{id}/like")
    suspend fun unlikeCreation(@Path("id") id: String): Envelope<LikeResult>

    @PATCH("creations/{id}/availability")
    suspend fun updateAvailability(
        @Path("id") id: String,
        @Body request: AvailabilityRequest
    ): Envelope<CreationDetailDto>

    @POST("creations/{id}/inquiries")
    suspend fun sendInquiry(
        @Path("id") id: String,
        @Body request: InquiryRequest
    ): Envelope<InquiryDto>

    @GET("artisans/{id}")
    suspend fun getArtisanProfile(@Path("id") id: String): Envelope<ArtisanReadProfileDto>

    @GET("artisans/{id}/creations")
    suspend fun getArtisanCreations(@Path("id") id: String): Envelope<List<CreationSummaryDto>>

    @POST("artisans")
    suspend fun openGallery(@Body request: OpenGalleryRequest): Envelope<ArtisanReadProfileDto>

    @PATCH("artisans/me")
    suspend fun updateArtisanProfile(@Body request: ArtisanWriteProfileDto): Envelope<ArtisanReadProfileDto>

    @POST("uploads/signature")
    suspend fun getUploadSignature(@Body request: SignatureRequest = SignatureRequest()): Envelope<UploadSignatureDto>
}
