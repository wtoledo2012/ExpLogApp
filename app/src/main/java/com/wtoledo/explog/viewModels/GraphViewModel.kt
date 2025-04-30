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
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withTimeoutOrNull

class GraphViewModel : ViewModel() {

    private val _categoryExpenses = MutableLiveData<List<CategoryExpense>>()
    val categoryExpenses: LiveData<List<CategoryExpense>> = _categoryExpenses

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val db = FirebaseFirestore.getInstance()
    private val expensesRef = db.collection("expense")
    private val categoriesRef: CollectionReference = db.collection("categories")

    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> = _categories

    private val _isLoadingCategories = MutableLiveData<Boolean>(true)
    val isLoadingCategories: LiveData<Boolean> = _isLoadingCategories

    private val _categoriesLoadError = MutableLiveData<String?>(null)
    val categoriesLoadError: LiveData<String?> = _categoriesLoadError

    init {
        loadCategoryExpenses()
    }

    private fun loadCategoryExpenses() {
        _isLoading.value = true
        viewModelScope.launch {
            loadExpensesWithTimeout()
                .catch { exception ->
                    Log.e("GraphViewModel", "Error loading expenses", exception)
                    _errorMessage.postValue("Error loading expenses: ${exception.localizedMessage}")
                    _isLoading.postValue(false)
                }
                .onEach { result ->
                    if (result != null) {
                        val (expenseList, categoryList) = result
                        val categoryExpenseMap = mutableMapOf<String, Double>()
                        expenseList.forEach { expense ->
                            val category = expense.categoryId?.let { getCategoryById(categoryList, it)?.name } ?: "Unknown"
                            val currentTotal = categoryExpenseMap.getOrDefault(category, 0.0)
                            categoryExpenseMap[category] = currentTotal + expense.amount
                        }
                        val categoryExpensesList = categoryExpenseMap.map { (category, total) ->
                            CategoryExpense(category, total)
                        }.sortedByDescending { it.totalAmount }
                        _categoryExpenses.postValue(categoryExpensesList)
                        _categories.postValue(categoryList) //Update the list of categories
                        _errorMessage.postValue(null)
                        _isLoading.postValue(false)
                    } else {
                        _errorMessage.postValue("Error loading expenses or categories (timeout)")
                        _isLoading.postValue(false)
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun loadExpensesWithTimeout(): Flow<Pair<List<Expense>, List<Category>>?> = flow {
        val result: Pair<List<Expense>, List<Category>>? = withTimeoutOrNull(5000) {
            combine(
                getExpensesFlow(),
                getCategoriesFlow()
            ) { expenses, categories ->
                Pair(expenses, categories)
            }.flowOn(Dispatchers.IO).catch{
                Log.e("GraphViewModel", "Error loading expenses inside the combine", it)
                null
            }.flowOn(Dispatchers.IO).firstOrNull()
        }
        emit(result)
    }

    private fun getExpensesFlow(): Flow<List<Expense>> = expensesRef.dataObjects<Expense>()
        .onStart {}
        .catch { exception ->
            Log.e("GraphViewModel", "Error loading expenses", exception)
        }

    private fun getCategoriesFlow(): Flow<List<Category>> = flow {
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
        emit(categories)
    }.catch { exception ->
        Log.e("GraphViewModel", "Error loading categories", exception)
        _categoriesLoadError.postValue("Error al cargar categorías")
    }.flowOn(Dispatchers.IO)

    fun getCategoryById(categories: List<Category>, categoryId: Int): Category? {
        return categories.find { it.id == categoryId }
    }
}