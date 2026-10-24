package com.example.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// Request & Response DTOs
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val id: String,
    val name: String,
    val email: String,
    val role: String // ADMIN, TEACHER, STUDENT
)

data class ApiResponse<T>(
    val status: String, // success, error
    val message: String,
    val data: T
)

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @GET("books")
    suspend fun getBooks(
        @Header("Authorization") token: String,
        @Query("query") query: String?,
        @Query("gradeClass") gradeClass: String?,
        @Query("category") category: String?
    ): Response<ApiResponse<List<BookEntity>>>

    @Multipart
    @POST("books/upload")
    suspend fun uploadBook(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("author") author: RequestBody,
        @Part("subject") subject: RequestBody,
        @Part("gradeClass") gradeClass: RequestBody,
        @Part("year") year: RequestBody,
        @Part("category") category: RequestBody,
        @Part("description") description: RequestBody
    ): Response<ApiResponse<BookEntity>>

    @Multipart
    @POST("books/convert-docx")
    suspend fun convertDocxToPdf(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody> // Returns PDF file stream
}
