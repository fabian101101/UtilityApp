package au.edu.jcu.assessment.utilityapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit description of the Open-Meteo geocoding API, which turns place names into coordinates. */
interface GeocodingApi {

    @GET("v1/search")
    suspend fun searchPlaces(
        @Query("name") name: String,
        @Query("count") count: Int,
        @Query("language") language: String,
        @Query("format") format: String
    ): GeocodingResponse
}
