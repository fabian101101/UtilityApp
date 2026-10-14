package au.edu.jcu.assessment.utilityapp.data

import au.edu.jcu.assessment.utilityapp.data.remote.GeocodingApi
import au.edu.jcu.assessment.utilityapp.data.remote.GeocodingResponse
import au.edu.jcu.assessment.utilityapp.data.remote.PlaceDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NetworkPlaceRepositoryTest {

    /** Stand-in for the web API so the tests never touch the network. */
    private class FakeGeocodingApi(
        private val response: GeocodingResponse = GeocodingResponse(null),
        private val error: Exception? = null
    ) : GeocodingApi {
        var lastQuery: String? = null

        override suspend fun searchPlaces(
            name: String,
            count: Int,
            language: String,
            format: String
        ): GeocodingResponse {
            lastQuery = name
            error?.let { throw it }
            return response
        }
    }

    private fun dto(name: String?, lat: Double? = 1.0, lon: Double? = 2.0, region: String? = null, country: String? = null) =
        PlaceDto(name = name, latitude = lat, longitude = lon, region = region, country = country)

    @Test
    fun mapsResultsAndJoinsRegionWithCountry() = runTest {
        val api = FakeGeocodingApi(
            GeocodingResponse(listOf(dto("Brisbane", -27.47, 153.03, "Queensland", "Australia")))
        )

        val places = NetworkPlaceRepository(api).searchPlaces("Bris").getOrThrow()

        assertEquals(1, places.size)
        assertEquals("Brisbane", places[0].name)
        assertEquals("Queensland, Australia", places[0].region)
        assertEquals("Brisbane, Queensland, Australia", places[0].displayName)
        assertEquals(-27.47, places[0].coordinates.latitude, 0.0)
    }

    @Test
    fun sendsTheQuery() = runTest {
        val api = FakeGeocodingApi()

        NetworkPlaceRepository(api).searchPlaces("Nairobi")

        assertEquals("Nairobi", api.lastQuery)
    }

    @Test
    fun noResultsKeyGivesEmptyList() = runTest {
        val places = NetworkPlaceRepository(FakeGeocodingApi(GeocodingResponse(null)))
            .searchPlaces("zzzz")
            .getOrThrow()

        assertTrue(places.isEmpty())
    }

    @Test
    fun skipsResultsWithMissingNameOrPosition() = runTest {
        val api = FakeGeocodingApi(
            GeocodingResponse(listOf(dto(name = null), dto("No position", lat = null), dto("Valid")))
        )

        val places = NetworkPlaceRepository(api).searchPlaces("x").getOrThrow()

        assertEquals(listOf("Valid"), places.map { it.name })
    }

    @Test
    fun networkErrorBecomesFailure() = runTest {
        val api = FakeGeocodingApi(error = IOException("no connection"))

        val result = NetworkPlaceRepository(api).searchPlaces("Nairobi")

        assertTrue(result.isFailure)
    }
}
