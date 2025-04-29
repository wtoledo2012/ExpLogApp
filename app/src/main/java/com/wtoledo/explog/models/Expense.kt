package com.wtoledo.explog.models

data class Expense(
    var description: String = "",
    var amount: Double = 0.00,
    var date: String = "",
    var localName: String = "",
    var categoryId: Int = 0
)