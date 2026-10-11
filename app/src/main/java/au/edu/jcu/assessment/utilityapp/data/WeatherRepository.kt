package au.edu.jcu.assessment.utilityapp.data

import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit
import au.edu.jcu.assessment.utilityapp.data.model.Weather
import au.edu.jcu.assessment.utilityapp.data.remote.WeatherApi
import au.edu.jcu.assessment.utilityapp.data.remote.toWeather
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Single source of truth for weather. The UI never talks to the network directly. */
interface WeatherRepository {

    /** Fetches the current weather. Failures come back as [Result.failure], never as a crash. */
    suspend fun getCurrentWeather(city: City, unit: TemperatureUnit): Result<Weather>
}

/** The fields we ask Open-Meteo for. */
private const val CURRENT_FIELDS = "temperature_2m,weather_code,is_day"

class NetworkWeatherRepository(private val api: WeatherApi) : WeatherRepository {

    override suspend fun getCurrentWeather(city: City, unit: TemperatureUnit): Result<Weather> =
        try {
            val response = api.getCurrentWeather(
                latitude = city.latitude,
                longitude = city.longitude,
                currentFields = CURRENT_FIELDS,
                temperatureUnit = unit.apiValue
            )
            response.toWeather()
                ?.let { Result.success(it) }
                ?: Result.failure(IOException("Weather response was missing data"))
        } catch (e: CancellationException) {
            // Never swallow cancellation, or the ViewModel cannot cancel a stale request.
            throw e
        } catch (e: Exception) {
            // Network, HTTP and JSON problems are all reported the same way.
            Result.failure(e)
        }
}
