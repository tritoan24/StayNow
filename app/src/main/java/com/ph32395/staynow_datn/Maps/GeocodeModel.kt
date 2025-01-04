package com.ph32395.staynow_datn.Maps

data class GeocodeResponse(
    val results: List<GeocodeResult>,
    val status: String
)

data class GeocodeResult(
    val formatted_address: String,
    val geometry: Geometry,
    val compound:Compounds,
    val address:String
)

data class Compounds(
    val district:String,
    val commune:String,
    val province:String,
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)
