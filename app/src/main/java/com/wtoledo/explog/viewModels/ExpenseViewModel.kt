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

    private val _isSaveSuccessful = MutableLiveData<Boolean?>()
    val isSaveSuccessful: LiveData<Boolean?> = _isSaveSuccessful

    private val db = Firebase.firestore
    private val expensesRef: CollectionReference = db.collection("expense")

    private val _categories = listOf(
        Category(1,"Casa", Icons.Filled.Home),
        Category(2,"Comida", Icons.Filled.ShoppingCart),
        Category(3,"Ocio", Icons.Filled.Star),
        Category(4,"Transporte", Icons.Filled.LocationOn),
        Category(5,"Serv. Bás.", Icons.Filled.Settings),
        Category(6,"Vida y Salud", Icons.Filled.Favorite),
        Category(7,"Ropa", Icons.Filled.AccountBox),
        Category(8,"Otros", Icons.Filled.Favorite)
    )
    val categories: List<Category> = _categories

    init {
        _expense.value = Expense()
        _isSaveSuccessful.value = null
    }

    fun updateDescription(description: String) {
        _expense.value = _expense.value?.copy(description = description)
        //validateForm()
        //_isFormTouched.value = true
    }

    fun updateAmount(amount: Double) {
        _expense.value = _expense.value?.copy(amount = amount)
        //validateForm()
        //_isFormTouched.value = true
    }

    fun updateDate(date: String) {
        _expense.value = _expense.value?.copy(date = date)
        //validateForm()
        //_isFormTouched.value = true
    }

    fun updateEstablishmentName(name: String) {
        _expense.value = _expense.value?.copy(localName = name)
        //validateForm()
        //_isFormTouched.value = true
    }

    fun updateCategory(category: String) {
        val selectedCategory = _categories.find { it.name == category }
        _expense.value = _expense.value?.copy(categoryId = selectedCategory?.id ?: 0)
        //validateForm()
        //_isFormTouched.value = true
    }

    fun saveExpense() {
        //if (_isFormValid.value == true) {
            val expenseToSave = _expense.value
            if (expenseToSave != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val documentReference = expensesRef.add(expenseToSave).await()
                        Log.d("ExpenseViewModel", "Expense saved successfully with ID: ${documentReference.id}")
                        _expense.postValue(Expense())
                        _isSaveSuccessful.postValue(true)
                        //_isFormTouched.postValue(false)
                    } catch (e: Exception) {
                        Log.e("ExpenseViewModel", "Error saving expense", e)
                        _isSaveSuccessful.postValue(false)
                    }
                }
            }
        //} else {
        //   _isSaveSuccessful.postValue(false)
        // }
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

    /*private fun validateForm() {
        //if (_isFormTouched.value == true) {
            val currentExpense = _expense.value ?: return
            _isFormValid.value = currentExpense.description.isNotBlank() &&
                    currentExpense.amount != null &&
                    currentExpense.amount != 0.0 &&
                    currentExpense.date.isNotBlank() &&
                    currentExpense.localName.isNotBlank() &&
                    currentExpense.categoryId != null &&
                    currentExpense.categoryId != 0
        //}else{
        //     _isFormValid.value = false
        //}
    }*/
}