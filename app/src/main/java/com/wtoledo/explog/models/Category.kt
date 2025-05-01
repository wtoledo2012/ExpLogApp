package com.wtoledo.explog.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

data class Category(
    val id: Int,
    val name: String,
    val icon: String
){
    fun getImageVector(): ImageVector {
        return when (icon) {
            "Home" -> Icons.Filled.Home
            "ShoppingCart" -> Icons.Filled.ShoppingCart
            "Star" -> Icons.Filled.Star
            "LocationOn" -> Icons.Filled.LocationOn
            "Settings" -> Icons.Filled.Settings
            "Favorite" -> Icons.Filled.Favorite
            "AccountBox" -> Icons.Filled.AccountBox
            "Clothes" -> Icons.Filled.Checkroom
            "BasServ" -> Icons.Filled.Lightbulb
            "Transport" -> Icons.Filled.LocalTaxi
            "Health" -> Icons.Filled.HealthAndSafety
            else -> Icons.Filled.Favorite
        }
    }
}