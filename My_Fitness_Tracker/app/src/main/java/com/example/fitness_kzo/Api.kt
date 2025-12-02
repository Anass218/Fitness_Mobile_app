package com.example.fitness_kzo

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface Api {
    @POST("registrationDAO.php")
    suspend fun registerUser(@Body request: RegistrationRequest): RegistrationResponse

    @POST("loginDAO.php")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse

    @POST("logoutDAO.php")
    suspend fun logoutUser(@Body request: LogoutRequest): LogoutResponse

    @POST("editProfile.php")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): ProfileUpdateResponse

    @GET("targets.php")
    suspend fun getTargets(@Query("user_id") userId: Int): Response<TargetsResponse>

    @POST("targets.php")
    suspend fun updateTargets(@Body request: UpdateTargetsRequest): Response<UpdateTargetsResponse>

    // Add endpoints for activity calories
    @GET("running.php")
    suspend fun getRunningCalories(@Query("user_id") userId: Int): Response<ActivityCaloriesResponse>

    @GET("cycling.php")
    suspend fun getCyclingCalories(@Query("user_id") userId: Int): Response<ActivityCaloriesResponse>
    @POST("cycling.php")
    suspend fun saveCyclingSession(
        @Body request: SaveCyclingRequest
    ): Response<SaveCyclingResponse>

    @GET("swimming.php")
    suspend fun getSwimmingCalories(@Query("user_id") userId: Int): Response<ActivityCaloriesResponse>
    @POST("swimming.php")
    suspend fun saveSwimmingSession(
        @Body request: SwimmingActivity.SaveSwimmingRequest
    ): Response<SaveSwimmingResponse>

    @GET("weightlifting.php")
    suspend fun getWeightliftingCalories(@Query("user_id") userId: Int): Response<ActivityCaloriesResponse>
    @POST("weightlifting.php")
    suspend fun saveWeightliftingSession(
        @Body request: SaveWeightliftingRequest
    ): Response<SaveWeightliftingResponse>

    @GET("yoga.php")
    suspend fun getYogaCalories(@Query("user_id") userId: Int): Response<ActivityCaloriesResponse>
    @POST("yoga.php")
    fun saveYogaSession(
        @Body request: SaveYogaRequest
    ): Call<SaveYogaResponse>

    @POST("running.php")
    suspend fun saveRunningSession(
        @Body request: SaveRunningRequest
    ): Response<SaveRunningResponse>
    @GET("get_all_activities.php")
    suspend fun getAllActivities(
        @Query("user_id") userId: Int
    ): Response<AllActivitiesResponse>

    @HTTP(method = "DELETE", path = "running.php", hasBody = true)
    suspend fun deleteRunningRecord(@Body request: DeleteRequest): Response<DeleteResponse>

    @HTTP(method = "DELETE", path = "cycling.php", hasBody = true)
    suspend fun deleteCyclingRecord(@Body request: DeleteRequest): Response<DeleteResponse>

    @HTTP(method = "DELETE", path = "swimming.php", hasBody = true)
    suspend fun deleteSwimmingRecord(@Body request: DeleteRequest): Response<DeleteResponse>

    @HTTP(method = "DELETE", path = "weightlifting.php", hasBody = true)
    suspend fun deleteWeightliftingRecord(@Body request: DeleteRequest): Response<DeleteResponse>

    @HTTP(method = "DELETE", path = "yoga.php", hasBody = true)
    suspend fun deleteYogaRecord(@Body request: DeleteRequest): Response<DeleteResponse>
    data class AllActivitiesResponse(
        val success: Boolean,
        val data: List<ActivityRecordForRF>? = null
    )

    data class ActivityRecordForRF(
        val id: Int,
        val user_id: Int,
        val type: String,
        val duration_minutes: Float,
        val details: String,
        val calorie_burned_kcal: Float,
        val recorded_at: String
    )

}

data class TargetsResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TargetsData? = null
)

data class TargetsData(
    val running_target: Int,
    val cycling_target: Int,
    val swimming_target: Int,
    val weightlifting_target: Int,
    val yoga_target: Int,
    val target_start_date: String?,
    val target_end_date: String?
)

data class UpdateTargetsRequest(
    val user_id: Int,
    val running_target: Int,
    val cycling_target: Int,
    val swimming_target: Int,
    val weightlifting_target: Int,
    val yoga_target: Int,
    val target_start_date: String,
    val target_end_date: String
)

data class UpdateTargetsResponse(
    val success: Boolean,
    val message: String
)

data class ActivityCaloriesResponse(
    val success: Boolean,
    val data: List<ActivityRecord>? = null
)

data class ActivityRecord(
    val calorie_burned_kcal: Float
)
data class DeleteRequest(val id: Int)
data class DeleteResponse(val success: Boolean, val message: String)