package au.edu.jcu.assessment.utilityapp.di

import android.content.Context
import au.edu.jcu.assessment.utilityapp.data.FusedLocationRepository
import au.edu.jcu.assessment.utilityapp.data.LocationRepository
import au.edu.jcu.assessment.utilityapp.data.NetworkPlaceRepository
import au.edu.jcu.assessment.utilityapp.data.NetworkWeatherRepository
import au.edu.jcu.assessment.utilityapp.data.PlaceRepository
import au.edu.jcu.assessment.utilityapp.data.WeatherRepository
import au.edu.jcu.assessment.utilityapp.data.remote.GeocodingApi
import au.edu.jcu.assessment.utilityapp.data.remote.WeatherApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
private const val TIMEOUT_SECONDS = 10L

/** Manual dependency injection: one place that knows how to build the app's dependencies. */
interface AppContainer {
    val weatherRepository: WeatherRepository
    val placeRepository: PlaceRepository
    val locationRepository: LocationRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    /** The weather and place-search services live on different hosts, so each gets its own Retrofit. */
    private fun buildRetrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val weatherApi: WeatherApi by lazy {
        buildRetrofit(WEATHER_BASE_URL).create(WeatherApi::class.java)
    }

    private val geocodingApi: GeocodingApi by lazy {
        buildRetrofit(GEOCODING_BASE_URL).create(GeocodingApi::class.java)
    }

    override val weatherRepository: WeatherRepository by lazy { NetworkWeatherRepository(weatherApi) }
    override val placeRepository: PlaceRepository by lazy { NetworkPlaceRepository(geocodingApi) }
    override val locationRepository: LocationRepository by lazy { FusedLocationRepository(appContext) }
}
