package com.wtoledo.explog.viewModels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wtoledo.explog.services.DocumentAiApi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ScanExpViewModel : ViewModel() {
    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    val _scannedAmount = MutableLiveData<Double>()
    val scannedAmount: LiveData<Double> = _scannedAmount
    val _scannedDate = MutableLiveData<String>()
    val scannedDate: LiveData<String> = _scannedDate
    val _scannedName = MutableLiveData<String>()
    val scannedName: LiveData<String> = _scannedName

    private val documentAiApi: DocumentAiApi

    init {
        // Para logear los request de RetroFit
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://us-central1-expenseslog-48120.cloudfunctions.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        documentAiApi = retrofit.create(DocumentAiApi::class.java)
    }

    fun processImage(bitmap: Bitmap) {
        _isProcessing.postValue(true)
        _scannedDate.postValue("")
        _scannedAmount.postValue(0.0)
        _scannedName.postValue("")
        viewModelScope.launch {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                Log.d("ScanExpViewModel", "Request body size: ${requestBody.contentLength()} bytes")
                Log.d("ScanExpViewModel", "Request body media type: ${requestBody.contentType()}")

                //enviando la image a la API de Cloud Functions
                val response = documentAiApi.processImage(requestBody)

                if (response.isSuccessful) {
                    val documentAiResponse = response.body()
                    documentAiResponse?.let {
                        // Extraer los valores de la respuesta
                        _scannedAmount.postValue(it.scannedAmount ?: 0.0)
                        _scannedDate.postValue(it.scannedDate ?: "")
                        _scannedName.postValue(it.scannedName ?: "")
                    } ?: run {
                        Log.e("ScanExpViewModel", "API response body is null")
                        _scannedDate.postValue("")
                        _scannedAmount.postValue(0.0)
                        _scannedName.postValue("")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ScanExpViewModel", "API request failed: ${response.code()} - $errorBody")
                    _scannedDate.postValue("")
                    _scannedAmount.postValue(0.0)
                    _scannedName.postValue("")
                }
            } catch (e: Exception) {
                Log.e("ScanExpViewModel", "Error processing image", e)
                _scannedDate.postValue("")
                _scannedAmount.postValue(0.0)
                _scannedName.postValue("")
            } finally {
                _isProcessing.postValue(false)
            }
        }
    }

}