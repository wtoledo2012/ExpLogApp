package com.wtoledo.explog.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.dataObjects
import com.wtoledo.explog.models.CategoryExpense
import com.wtoledo.explog.models.Category
import com.wtoledo.explog.models.Expense
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star

class GraphViewModel : ViewModel() {

    private val _categoryExpenses = MutableLiveData<List<CategoryExpense>>()
    val categoryExpenses: LiveData<List<CategoryExpense>> = _categoryExpenses

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val db = FirebaseFirestore.getInstance()
    private val expensesRef = db.collection("expense")

    init {
        loadCategoryExpenses()
    }

    private fun loadCategoryExpenses() {
        _isLoading.value = true
        viewModelScope.launch {
            expensesRef.dataObjects<Expense>()
                .catch { exception ->
                    Log.e("GraphViewModel", "Error loading expenses", exception)
                    _errorMessage.postValue("Error loading expenses: ${exception.localizedMessage}")
                }
                .onEach { expenseList ->
                    val categoryExpenseMap = mutableMapOf<String, Double>()
                    expenseList.forEach { expense ->
                        val category =
                            expense.categoryId?.let { getCategoryById(it)?.name } ?: "Unknown"
                        val currentTotal = categoryExpenseMap.getOrDefault(category, 0.0)
                        categoryExpenseMap[category] = currentTotal + expense.amount
                    }
                    val categoryExpensesList = categoryExpenseMap.map { (category, total) ->
                        CategoryExpense(category, total)
                    }
                    _categoryExpenses.postValue(categoryExpensesList)
                    _errorMessage.postValue(null)
                }
                .launchIn(viewModelScope)
            _isLoading.postValue(false)
        }
    }

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

    fun getCategoryById(categoryId: Int): Category? {
        return _categories.find { it.id == categoryId }
    }
}