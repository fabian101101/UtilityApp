package au.edu.jcu.assessment.utilityapp.data.remote

import au.edu.jcu.assessment.utilityapp.data.model.Weather
import au.edu.jcu.assessment.utilityapp.data.model.WeatherCondition
import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

/** Top level of the Open-Meteo response. Only the `current` block is used. */
data class WeatherResponse(
    @SerializedName("current") val current: CurrentWeatherDto?
)

/**
 * The `current` block, e.g. `{"temperature_2m": 21.4, "weather_code": 3, "is_day": 1}`.
 * Fields are nullable because Gson does not enforce Kotlin null-safety.
 */
data class CurrentWeatherDto(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("is_day") val isDay: Int?
)

/** Converts the network model to the app's [Weather], or null if required data is missing. */
fun WeatherResponse.toWeather(): Weather? {
    val data = current ?: return null
    val temperature = data.temperature ?: return null
    val code = data.weatherCode ?: return null
    return Weather(
        temperature = temperature.roundToInt(),
        condition = WeatherCondition.fromCode(code),
        isDay = data.isDay != 0
    )
}
