package au.edu.jcu.assessment.utilityapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit description of the Open-Meteo forecast API (https://open-meteo.com). No API key needed. */
interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") currentFields: String,
        @Query("temperature_unit") temperatureUnit: String
    ): WeatherResponse
}
