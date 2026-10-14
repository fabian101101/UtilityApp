package au.edu.jcu.assessment.utilityapp.ui

import au.edu.jcu.assessment.utilityapp.data.model.Weather

/** Why the weather could not be shown. Decides which message the card displays. */
enum class WeatherError {
    /** The weather request failed (no internet, server problem). */
    Network,

    /** The phone could not work out where it is. */
    Location
}

/** What the weather card should show. */
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: Weather) : WeatherUiState

    /** "Use my location" is on, but the user has not allowed location access. */
    data object NeedsPermission : WeatherUiState

    data class Error(val reason: WeatherError) : WeatherUiState
}
