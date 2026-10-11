package au.edu.jcu.assessment.utilityapp.data.model

/** A place the weather can be fetched for. */
data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

/** The cities offered in Settings. A fixed list keeps the app free of location permissions. */
object Cities {
    val all: List<City> = listOf(
        City("Nairobi", -1.2921, 36.8219),
        City("Townsville", -19.2590, 146.8169),
        City("Cairns", -16.9186, 145.7781),
        City("Brisbane", -27.4698, 153.0251),
        City("Singapore", 1.3521, 103.8198)
    )

    val default: City = all.first()
}
