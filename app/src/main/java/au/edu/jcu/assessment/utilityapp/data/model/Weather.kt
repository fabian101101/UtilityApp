package au.edu.jcu.assessment.utilityapp.data.model

/** Current weather, ready to show on screen. [temperature] is in the unit that was requested. */
data class Weather(
    val temperature: Int,
    val condition: WeatherCondition,
    val isDay: Boolean
)

/**
 * Simplified weather type. [suggestedMinutes] is the meditation length that suits it.
 */
enum class WeatherCondition(val suggestedMinutes: Int) {
    Clear(10),
    Cloudy(10),
    Fog(15),
    Rain(15),
    Snow(10),
    Storm(5);

    companion object {
        /** Converts a WMO weather code (as used by Open-Meteo) to a [WeatherCondition]. */
        fun fromCode(code: Int): WeatherCondition = when (code) {
            0, 1 -> Clear
            2, 3 -> Cloudy
            45, 48 -> Fog
            in 51..67, in 80..82 -> Rain
            in 71..77, 85, 86 -> Snow
            in 95..99 -> Storm
            else -> Cloudy
        }
    }
}

/** Temperature unit. [apiValue] is the text the weather API expects. */
enum class TemperatureUnit(val apiValue: String, val symbol: String) {
    Celsius("celsius", "°C"),
    Fahrenheit("fahrenheit", "°F")
}
