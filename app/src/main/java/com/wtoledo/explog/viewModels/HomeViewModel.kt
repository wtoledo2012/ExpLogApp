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
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.firestore.ListenerRegistration

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

    private var currentMonthListener: ListenerRegistration? = null
    private var previousMonthListener: ListenerRegistration? = null
    private var lastFiveListener: ListenerRegistration? = null
    init {
        Log.d("HomeViewModel", "expensesRef path: ${expensesRef.path}")
        loadCurrentMonthExpensesSum()
        loadPreviousMonthExpensesSum()
        loadLastFiveExpenses()
    }

    // gastos del mes actual
    private fun loadCurrentMonthExpensesSum() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.time // Fecha Ini

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfMonth = calendar.time // Fecha Fin

                val startOfMonthString = firestoreDateFormat.format(startOfMonth)
                val endOfMonthString = firestoreDateFormat.format(endOfMonth)

                Log.d("HomeViewModel", """startOfMonthString=$startOfMonthString""")
                Log.d("HomeViewModel", """endOfMonthString=$endOfMonthString""")

                currentMonthListener = expensesRef
                    .whereGreaterThanOrEqualTo("date", startOfMonthString)
                    .whereLessThanOrEqualTo("date", endOfMonthString)
                    .addSnapshotListener { querySnapshot, e ->
                        if (e != null) {
                            Log.e("HomeViewModel", "Error listening for current month expenses", e)
                            _errorMessage.postValue("Error loading current month expenses: ${e.localizedMessage}")
                            _isLoading.postValue(false)
                            return@addSnapshotListener
                        }

                        if (querySnapshot != null) {
                            var sum = 0.0
                            for (document in querySnapshot.documents) {
                                val expense = document.toObject(Expense::class.java)
                                sum += expense?.amount ?: 0.0
                            }
                            _currentMonthExpensesSum.postValue(sum)
                            _isLoading.postValue(false)
                        }
                    }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading current month expenses", e)
                _errorMessage.postValue("Error loading current month expenses: ${e.localizedMessage}")
                _isLoading.postValue(false)
            }
        }
    }

    // gastos del mes anterior
    private fun loadPreviousMonthExpensesSum() {
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

                previousMonthListener = expensesRef
                    .whereGreaterThanOrEqualTo("date", startOfPreviousMonthString)
                    .whereLessThanOrEqualTo("date", endOfPreviousMonthString)
                    .addSnapshotListener { querySnapshot, e ->
                        if (e != null) {
                            Log.e("HomeViewModel", "Error en la escucha de gastos del mes anterior", e)
                            _isLoading.postValue(false)
                            return@addSnapshotListener
                        }

                        if (querySnapshot != null) {
                            var sum = 0.0
                            for (document in querySnapshot.documents) {
                                val expense = document.toObject(Expense::class.java)
                                sum += expense?.amount ?: 0.0
                            }
                            _previousMonthExpensesSum.postValue(sum)
                            _isLoading.postValue(false)
                        }
                    }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading previous month expenses", e)
                _isLoading.postValue(false)
            }
        }
    }

    // últimos 5 gastos
    private fun loadLastFiveExpenses() {
        viewModelScope.launch {
            // TODO: Implement the logic to fetch the last 5 expenses
            try {
                lastFiveListener = expensesRef
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(5)
                    .addSnapshotListener { querySnapshot, e ->
                        if (e != null) {
                            Log.e("HomeViewModel", "Error listening for last five expenses", e)
                            return@addSnapshotListener
                        }

                        if (querySnapshot != null) {
                            val lastFive = querySnapshot.documents.mapNotNull { document ->
                                document.toObject(Expense::class.java)
                            }
                            _lastFiveExpenses.postValue(lastFive)
                        }
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error setting up last five expenses listener", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentMonthListener?.remove()
        previousMonthListener?.remove()
        lastFiveListener?.remove()
        Log.d("HomeViewModel", "Firestore listeners removed")
    }

}