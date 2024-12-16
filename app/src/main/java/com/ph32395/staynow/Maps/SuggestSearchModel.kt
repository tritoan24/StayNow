package com.ph32395.staynow.Maps

data class SuggestionResponse(
    val predictions: List<Prediction>,
    val status: String
)

data class Prediction(
    val description: String,
    val place_id: String,
    val structured_formatting: StructuredFormatting?
)

data class StructuredFormatting(
    val secondary_text: String? // Giá trị này có thể null
)