package com.wtoledo.explog.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.dataObjects
import com.wtoledo.explog.models.Expense
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExpensesListViewModel : ViewModel() {

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    private val db = FirebaseFirestore.getInstance()
    private val expensesRef: CollectionReference = db.collection("expense")

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        _isLoading.value = true
        expensesRef.orderBy("date", Query.Direction.DESCENDING)
            .dataObjects<Expense>()
            .catch { exception ->
                Log.e("ExpensesListViewModel", "Error loading expenses", exception)
                _errorMessage.postValue("Error loading expenses: ${exception.localizedMessage}")
            }
            .onEach { expenseList ->
                _expenses.postValue(expenseList)
                _errorMessage.postValue(null)
            }
            .launchIn(viewModelScope)
        _isLoading.postValue(false)
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                val querySnapshot = expensesRef
                    .whereEqualTo("date", expense.date)
                    .whereEqualTo("description", expense.description)
                    .whereEqualTo("amount", expense.amount)
                    .whereEqualTo("localName", expense.localName)
                    .whereEqualTo("categoryId", expense.categoryId)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    expensesRef.document(document.id).delete().await()
                }
                loadExpenses()
            } catch (e: Exception) {
                Log.e("ExpensesListViewModel", "Error deleting expense", e)
                _errorMessage.postValue("Error deleting expense: ${e.localizedMessage}")
            }
        }
    }
}