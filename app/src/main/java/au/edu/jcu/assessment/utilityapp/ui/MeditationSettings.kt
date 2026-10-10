package au.edu.jcu.assessment.utilityapp.ui

import au.edu.jcu.assessment.utilityapp.data.model.Cities
import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit

/** Session lengths offered in Settings, in minutes. */
val SESSION_LENGTH_OPTIONS = listOf(5, 10, 15, 20, 30)

/** User choices from the settings screen. Each one changes the session screen. */
data class MeditationSettings(
    val city: City = Cities.default,
    val unit: TemperatureUnit = TemperatureUnit.Celsius,
    val sessionMinutes: Int = 10,
    /** When true, the session length comes from the weather instead of [sessionMinutes]. */
    val useWeatherSuggestion: Boolean = true
)
