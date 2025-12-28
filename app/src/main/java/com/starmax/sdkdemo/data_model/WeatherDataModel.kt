package com.starmax.sdkdemo.data_model

data class WeatherResponse(
    val status: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val city: String,
    val days: List<WeatherDay>
)

data class WeatherDay(
    val uv: Int,
    val temp: Int,
    val sunset_hour: Int,
    val moonset_hour: Int,
    val sunrise_hour: Int,
    val dampness: Int,
    val moonset_minute: Int,
    val air_quality: Int,
    val type: Int,
    val sunrise_minute: Int,
    val seeing: Int,
    val min_temp: Int,
    val moonrise_hour: Int,
    val wind_speed: Int,
    val moonrise_minute: Int,
    val max_temp: Int,
    val sunset_minute: Int
) {
    fun getWeatherTypeIcon(): String {
        return when (type) {
            0 -> "☀️" // Sunny
            1 -> "⛅" // Partly Cloudy
            2 -> "☁️" // Cloudy
            3 -> "🌧️" // Rainy
            4 -> "⛈️" // Thunderstorm
            5 -> "🌨️" // Snowy
            6 -> "🌫️" // Foggy
            else -> "🌤️"
        }
    }

    fun getWeatherTypeName(): String {
        return when (type) {
            0 -> "Sunny"
            1 -> "Partly Cloudy"
            2 -> "Cloudy"
            3 -> "Rainy"
            4 -> "Thunderstorm"
            5 -> "Snowy"
            6 -> "Foggy"
            else -> "Unknown"
        }
    }

    fun getAirQualityText(): String {
        return when (air_quality) {
            1 -> "Good"
            2 -> "Moderate"
            3 -> "Poor"
            4 -> "Unhealthy"
            else -> "Unknown"
        }
    }

    fun getAirQualityColor(): androidx.compose.ui.graphics.Color {
        return when (air_quality) {
            1 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
            2 -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Amber
            3 -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
            4 -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }
}