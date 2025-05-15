package com.wtoledo.explog.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.wtoledo.explog.models.Expense
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val expensesRef: CollectionReference = db.collection("expense")

    private val _currentMonthExpensesSum = MutableLiveData<Double>(0.0)
    val currentMonthExpensesSum: LiveData<Double> = _currentMonthExpensesSum

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _previousMonthExpensesSum = MutableLiveData<Double>(0.0)
    val previousMonthExpensesSum: LiveData<Double> get() = _previousMonthExpensesSum

    private val _lastFiveExpenses = MutableLiveData<List<Expense>>()
    val lastFiveExpenses: LiveData<List<Expense>> get() = _lastFiveExpenses

    private val firestoreDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        Log.d("HomeViewModel", "expensesRef path: ${expensesRef.path}")
        loadCurrentMonthExpensesSum()
        loadPreviousMonthExpensesSum()
        // loadLastFiveExpenses()
    }

    // gastos del mes actual
    fun loadCurrentMonthExpensesSum() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                // Set calendar to the beginning of the current month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.time

                // Set calendar to the end of the current month
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfMonth = calendar.time

                val startOfMonthString = firestoreDateFormat.format(startOfMonth)
                val endOfMonthString = firestoreDateFormat.format(endOfMonth)

                Log.d("HomeViewModel", """startOfMonthString=$startOfMonthString""")
                Log.d("HomeViewModel", """endOfMonthString=$endOfMonthString""")

                val querySnapshot = expensesRef
                    .whereGreaterThanOrEqualTo("date", startOfMonthString)
                    .whereLessThanOrEqualTo("date", endOfMonthString)
                    .get()
                    .await()
                Log.d("HomeViewModel", "Found ${querySnapshot.size()} expenses.")

                var sum = 0.0
                for (document in querySnapshot.documents) {
                    val expense = document.toObject(Expense::class.java)
                    sum += expense?.amount ?: 0.0
                }
                _currentMonthExpensesSum.postValue(sum)

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading current month expenses", e)
                _errorMessage.postValue("Error loading current month expenses: ${e.localizedMessage}")
            } finally {
                //_isLoading.postValue(false)
            }
        }
    }

    // gastos del mes anterior
    fun loadPreviousMonthExpensesSum() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -1)

                // Fecha Ini
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfPreviousMonth = calendar.time

                // Fecha Fin
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfPreviousMonth = calendar.time

                val startOfPreviousMonthString = firestoreDateFormat.format(startOfPreviousMonth)
                val endOfPreviousMonthString = firestoreDateFormat.format(endOfPreviousMonth)

                Log.d("HomeViewModel", "Previous Month Start String: $startOfPreviousMonthString")
                Log.d("HomeViewModel", "Previous Month End String: $endOfPreviousMonthString")

                val querySnapshot = expensesRef
                    .whereGreaterThanOrEqualTo("date", startOfPreviousMonthString)
                    .whereLessThanOrEqualTo("date", endOfPreviousMonthString)
                    .get()
                    .await()
                Log.d("HomeViewModel", "Found ${querySnapshot.size()} previous month expenses.")

                var sum = 0.0
                for (document in querySnapshot.documents) {
                    val expense = document.toObject(Expense::class.java)
                    sum += expense?.amount ?: 0.0
                }
                _previousMonthExpensesSum.postValue(sum)

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading previous month expenses", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // últimos 5 gastos
    fun loadLastFiveExpenses() {
        viewModelScope.launch {
            // TODO: Implement the logic to fetch the last 5 expenses
            try {
                val querySnapshot = expensesRef
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING) // Ordena por fecha descendente
                    .limit(5)
                    .get()
                    .await()

                val lastFive = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Expense::class.java)
                }
                _lastFiveExpenses.postValue(lastFive)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading last five expenses", e)
            }
        }
    }


}