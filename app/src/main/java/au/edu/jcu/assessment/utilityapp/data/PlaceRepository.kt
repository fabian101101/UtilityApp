package au.edu.jcu.assessment.utilityapp.data

import au.edu.jcu.assessment.utilityapp.data.model.Place
import au.edu.jcu.assessment.utilityapp.data.remote.GeocodingApi
import au.edu.jcu.assessment.utilityapp.data.remote.toPlace
import kotlin.coroutines.cancellation.CancellationException

private const val MAX_RESULTS = 6
private const val LANGUAGE = "en"
private const val FORMAT = "json"

/** Looks up places by name. */
interface PlaceRepository {

    /** Returns matching places (possibly none), or a failure if the search could not be made. */
    suspend fun searchPlaces(query: String): Result<List<Place>>
}

class NetworkPlaceRepository(private val api: GeocodingApi) : PlaceRepository {

    override suspend fun searchPlaces(query: String): Result<List<Place>> =
        try {
            val response = api.searchPlaces(
                name = query,
                count = MAX_RESULTS,
                language = LANGUAGE,
                format = FORMAT
            )
            Result.success(response.results.orEmpty().mapNotNull { it.toPlace() })
        } catch (e: CancellationException) {
            // Never swallow cancellation, or a newer search cannot replace an older one.
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
