package au.edu.jcu.assessment.utilityapp.data

import au.edu.jcu.assessment.utilityapp.data.model.Coordinates
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit
import au.edu.jcu.assessment.utilityapp.data.model.WeatherCondition
import au.edu.jcu.assessment.utilityapp.data.remote.CurrentWeatherDto
import au.edu.jcu.assessment.utilityapp.data.remote.WeatherApi
import au.edu.jcu.assessment.utilityapp.data.remote.WeatherResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkWeatherRepositoryTest {

    /** Stand-in for the web API so the tests never touch the network. */
    private class FakeWeatherApi(
        private val response: WeatherResponse = WeatherResponse(null),
        private val error: Exception? = null
    ) : WeatherApi {
        var lastTemperatureUnit: String? = null

        override suspend fun getCurrentWeather(
            latitude: Double,
            longitude: Double,
            currentFields: String,
            temperatureUnit: String
        ): WeatherResponse {
            lastTemperatureUnit = temperatureUnit
            error?.let { throw it }
            return response
        }
    }

    private val somewhere = Coordinates(latitude = -27.47, longitude = 153.03)

    private fun responseOf(temperature: Double?, code: Int?, isDay: Int?) =
        WeatherResponse(CurrentWeatherDto(temperature, code, isDay))

    @Test
    fun mapsSuccessfulResponse() = runTest {
        val api = FakeWeatherApi(responseOf(temperature = 18.6, code = 63, isDay = 1))

        val result = NetworkWeatherRepository(api)
            .getCurrentWeather(somewhere, TemperatureUnit.Celsius)

        val weather = result.getOrThrow()
        assertEquals(19, weather.temperature)
        assertEquals(WeatherCondition.Rain, weather.condition)
        assertTrue(weather.isDay)
    }

    @Test
    fun nightIsReadFromIsDayFlag() = runTest {
        val api = FakeWeatherApi(responseOf(temperature = 10.0, code = 0, isDay = 0))

        val weather = NetworkWeatherRepository(api)
            .getCurrentWeather(somewhere, TemperatureUnit.Celsius)
            .getOrThrow()

        assertFalse(weather.isDay)
    }

    @Test
    fun sendsRequestedTemperatureUnit() = runTest {
        val api = FakeWeatherApi(responseOf(temperature = 70.0, code = 0, isDay = 1))

        NetworkWeatherRepository(api).getCurrentWeather(somewhere, TemperatureUnit.Fahrenheit)

        assertEquals("fahrenheit", api.lastTemperatureUnit)
    }

    @Test
    fun networkErrorBecomesFailure() = runTest {
        val api = FakeWeatherApi(error = IOException("no connection"))

        val result = NetworkWeatherRepository(api)
            .getCurrentWeather(somewhere, TemperatureUnit.Celsius)

        assertTrue(result.isFailure)
    }

    @Test
    fun responseMissingDataBecomesFailure() = runTest {
        val api = FakeWeatherApi(responseOf(temperature = null, code = 3, isDay = 1))

        val result = NetworkWeatherRepository(api)
            .getCurrentWeather(somewhere, TemperatureUnit.Celsius)

        assertTrue(result.isFailure)
    }
}
