package au.edu.jcu.assessment.utilityapp.data.model

/** A point on the map. */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

/** A place found by searching, for example "Brisbane, Queensland, Australia". */
data class Place(
    val name: String,
    /** Region and country, for telling apart places with the same name. May be empty. */
    val region: String,
    val latitude: Double,
    val longitude: Double
) {
    val coordinates: Coordinates
        get() = Coordinates(latitude, longitude)

    /** Name plus region, for lists and the settings screen. */
    val displayName: String
        get() = if (region.isBlank()) name else "$name, $region"
}
