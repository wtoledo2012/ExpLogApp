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
    private val categoriesRef: CollectionReference = db.collection("categories")

    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> = _categories

    private val _isLoadingCategories = MutableLiveData<Boolean>(true)
    val isLoadingCategories: LiveData<Boolean> = _isLoadingCategories

    private val _categoriesLoadError = MutableLiveData<String?>(null)
    val categoriesLoadError: LiveData<String?> = _categoriesLoadError

    init {
        _expense.value = Expense()
        _isSaveSuccessful.value = null
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingCategories.postValue(true)
            try {
                val querySnapshot = categoriesRef.get().await()
                val categories = querySnapshot.documents.mapNotNull { document ->
                    val id = document.getLong("id")?.toInt()
                    val name = document.getString("name")
                    val icon = document.getString("icon")

                    if (id != null && name != null && icon != null) {
                        Category(id, name, icon)
                    } else {
                        null
                    }
                }
                _categories.postValue(categories)
                _categoriesLoadError.postValue(null)
            } catch (e: Exception) {
                Log.e("ExpenseViewModel", "Error loading categories", e)
                _categoriesLoadError.postValue("Error al cargar categorías")
            } finally {
                _isLoadingCategories.postValue(false)
            }
        }
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
        val selectedCategory = _categories.value?.find { it.name == category }
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
        return _categories.value?.find { it.id == categoryId }
    }

    override fun onCleared() {
        super.onCleared()
        _isSaveSuccessful.value = false
    }
}