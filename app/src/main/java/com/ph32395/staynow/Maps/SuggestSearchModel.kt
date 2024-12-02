package com.ph32395.staynow.Maps

data class SuggestionResponse(
    val predictions: List<Prediction>,
    val status: String
)

data class Prediction(
    val description: String,
    val place_id: String
)
