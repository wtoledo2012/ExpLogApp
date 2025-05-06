package com.wtoledo.explog.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Locale

class ScanExpViewModel : ViewModel() {

    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    val _scannedAmount = MutableLiveData<Double>()
    val scannedAmount: LiveData<Double> = _scannedAmount
    val _scannedDate = MutableLiveData<String>()
    val scannedDate: LiveData<String> = _scannedDate
    val _scannedName = MutableLiveData<String>()
    val scannedName: LiveData<String> = _scannedName

    fun processImage(bitmap: Bitmap) {
        _isProcessing.postValue(true)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                _recognizedText.postValue(visionText.text)
                extractData(visionText)
                _isProcessing.postValue(false)
            }
            .addOnFailureListener { e ->
                _recognizedText.postValue("Error: ${e.message}")
                _isProcessing.postValue(false)
            }
    }

    fun extractData(visionText: Text) {
        var totalAmount = 0.0
        var date = ""
        var name = ""

        val lines = visionText.textBlocks.flatMap { it.lines }

        for (line in lines) {
            // Verifica la cantidad
            if (line.text.contains(Regex("(?i)(total)|(importe)|(monto)"))) {
                val amountMatch = Regex("([\\d]+\\.[\\d]+)").find(line.text)
                if (amountMatch != null) {
                    totalAmount = amountMatch.value.toDoubleOrNull() ?: 0.0
                }
            }
            //verifica la fecha
            if (line.text.contains(Regex("\\d{2}[/-]\\d{2}[/-]\\d{2,4}")) || line.text.contains(Regex("\\d{4}[/-]\\d{2}[/-]\\d{2}"))){
                val dateMatch = Regex("\\d{2}[/-]\\d{2}[/-]\\d{2,4}|\\d{4}[/-]\\d{2}[/-]\\d{2}").find(line.text)
                if (dateMatch != null){
                    var format = "dd/MM/yyyy"
                    val dateValue = dateMatch.value
                    if (dateValue.length == 8 || dateValue.length == 10) {
                        if (dateValue.contains("/")) {
                            if (dateValue.length == 8) {
                                format = "dd/MM/yy"
                            }
                        } else if (dateValue.contains("-")){
                            if (dateValue.length == 8) {
                                format = "dd-MM-yy"
                            } else if (dateValue.length == 10) {
                                format = "dd-MM-yyyy"
                            }
                        } else {
                            if (dateValue.length == 8) {
                                format = "ddMMYYYY"
                            } else if (dateValue.length == 10) {
                                format = "yyyyMMdd"
                            }
                        }
                        try {
                            val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                            val correctFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            date = correctFormat.format(dateFormat.parse(dateValue)!!)
                        } catch (e: Exception){
                            println(e)
                        }
                    }

                }

            }
            if(name.isEmpty()){
                name = line.text.trim()
            }
        }
        _scannedAmount.postValue(totalAmount)
        _scannedDate.postValue(date)
        _scannedName.postValue(name)

    }
}