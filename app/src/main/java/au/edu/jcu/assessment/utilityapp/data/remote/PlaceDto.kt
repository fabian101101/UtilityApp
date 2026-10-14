package au.edu.jcu.assessment.utilityapp.data.remote

import au.edu.jcu.assessment.utilityapp.data.model.Place
import com.google.gson.annotations.SerializedName

/** Top level of the Open-Meteo geocoding response. `results` is missing when nothing matches. */
data class GeocodingResponse(
    @SerializedName("results") val results: List<PlaceDto>?
)

/** One search result. Fields are nullable because Gson does not enforce Kotlin null-safety. */
data class PlaceDto(
    @SerializedName("name") val name: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("admin1") val region: String?,
    @SerializedName("country") val country: String?
)

/** Converts a search result to a [Place], or null if the name or position is missing. */
fun PlaceDto.toPlace(): Place? {
    val placeName = name?.trim().orEmpty()
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    if (placeName.isEmpty()) return null

    val regionText = listOfNotNull(region, country)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(", ")
    return Place(name = placeName, region = regionText, latitude = lat, longitude = lon)
}
