package com.wtoledo.explog.models

data class Expense(
    var description: String = "",
    var amount: Double = 0.0,
    var date: String = "", // OJO:Date
    var localName: String = "",
    var categoryId: Int = 0
)