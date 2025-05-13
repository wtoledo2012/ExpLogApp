package com.wtoledo.explog.services

//import com.wtoledo.explog.models.DocumentAiResponse
import com.wtoledo.explog.models.ScanResult
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DocumentAiApi {
    //@Multipart
    //@POST("v1/projects/expenseslog-48120/locations/us/processors/7fa97b4fa426d81d:process")
    @POST("documentAiProxy/")
    suspend fun processImage(
        @Body imageBody: RequestBody
    ): Response<ScanResult>
}