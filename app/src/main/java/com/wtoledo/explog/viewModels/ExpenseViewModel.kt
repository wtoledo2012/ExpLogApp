package com.wtoledo.explog.viewModels

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers

import com.wtoledo.explog.models.Expense
import com.wtoledo.explog.models.Category

class ExpenseViewModel : ViewModel() {

    private val _expense = MutableLiveData<Expense>()
    val expense: LiveData<Expense> = _expense

    private val _categories = listOf(
        Category(1,"Casa", Icons.Filled.Home),
        Category(2,"Alimentación", Icons.Filled.ShoppingCart),
        Category(3,"Ocio", Icons.Filled.Star),
        Category(4,"Transporte", Icons.Filled.LocationOn),
        Category(5,"Serv. Básicos", Icons.Filled.Settings),
        Category(6,"Vida y Salud", Icons.Filled.Favorite),
        Category(7,"Ropa", Icons.Filled.AccountBox),
        Category(8,"Otros", Icons.Filled.Favorite)
    )
    val categories: List<Category> = _categories

    private val db = Firebase.firestore
    private val expensesRef: CollectionReference = db.collection("expense")

    private val _isSaveSuccessful = MutableLiveData<Boolean?>()
    val isSaveSuccessful: LiveData<Boolean?> = _isSaveSuccessful

    init {
        _expense.value = Expense()
        _isSaveSuccessful.value = null
    }

    fun updateDescription(description: String) {
        _expense.value = _expense.value?.copy(description = description)
    }

    fun updateAmount(amount: Double) {
        _expense.value = _expense.value?.copy(amount = amount)
    }

    fun updateDate(date: String) {
        _expense.value = _expense.value?.copy(date = date)
    }

    fun updateEstablishmentName(name: String) {
        _expense.value = _expense.value?.copy(localName = name)
    }

    fun updateCategory(category: String) {
        //_expense.value = _expense.value?.copy(category = category)
        val selectedCategory = _categories.find { it.name == category }
        _expense.value = _expense.value?.copy(categoryId = selectedCategory?.id ?: 0)
    }

    fun saveExpense() {
        val expenseToSave = _expense.value
        if (expenseToSave != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val documentReference = expensesRef.add(expenseToSave).await()
                    Log.d("ExpenseViewModel", "Expense saved successfully with ID: ${documentReference.id}")
                    _expense.postValue(Expense())
                    _isSaveSuccessful.postValue(true)
                } catch (e: Exception) {
                    Log.e("ExpenseViewModel", "Error saving expense", e)
                    _isSaveSuccessful.postValue(false)
                }
            }

        }
    }

    fun resetIsSaveSuccessful(){
        _isSaveSuccessful.value = null
    }

    fun getCategoryById(categoryId: Int): Category? {
        return _categories.find { it.id == categoryId }
    }

    override fun onCleared() {
        super.onCleared()
        _isSaveSuccessful.value = false
    }
}