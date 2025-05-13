package com.wtoledo.explog.models

import com.google.gson.annotations.SerializedName

data class DocumentAiResponse(
    val document: Document? = null
)

data class Document(
    val text: String? = null,
    val entities: List<Entity>? = null,
    val pages: List<Page>? = null
)

data class Entity(
    @SerializedName("type")
    val type: String,
    val mentionText: String?,
    val normalizedValue: NormalizedValue?
)
data class NormalizedValue(
    val dateValue: DateValue?
)
data class DateValue(
    val year: Int?,
    val month: Int?,
    val day: Int?
)
data class Page(
    val pageNumber: Int?,
    val dimensions: Dimension?,
)

data class Dimension(
    val width: Double,
    val height: Double,
    val unit: String
)