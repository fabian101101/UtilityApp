package au.edu.jcu.assessment.utilityapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import au.edu.jcu.assessment.utilityapp.CalmSkiesApplication
import au.edu.jcu.assessment.utilityapp.data.LocationPermissionException
import au.edu.jcu.assessment.utilityapp.data.LocationRepository
import au.edu.jcu.assessment.utilityapp.data.LocationUnavailableException
import au.edu.jcu.assessment.utilityapp.data.PlaceRepository
import au.edu.jcu.assessment.utilityapp.data.WeatherRepository
import au.edu.jcu.assessment.utilityapp.data.model.Coordinates
import au.edu.jcu.assessment.utilityapp.data.model.Place
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TICK_MILLIS = 1_000L
private const val SECONDS_PER_MINUTE = 60
private const val SEARCH_DEBOUNCE_MILLIS = 400L
private const val MIN_SEARCH_LENGTH = 2

/**
 * Holds the weather, the place search, the meditation timer, today's totals and the settings.
 * Both screens share this one ViewModel so a settings change shows on the session screen,
 * and the timer keeps running when the screen rotates.
 */
class MeditationViewModel(
    private val weatherRepository: WeatherRepository,
    private val placeRepository: PlaceRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(MeditationSettings())
    val settings: StateFlow<MeditationSettings> = _settings.asStateFlow()

    private val _weather = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weather: StateFlow<WeatherUiState> = _weather.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<PlaceSearchState>(PlaceSearchState.Idle)
    val searchState: StateFlow<PlaceSearchState> = _searchState.asStateFlow()

    private val _timer = MutableStateFlow(TimerUiState.idle(_settings.value.sessionMinutes))
    val timer: StateFlow<TimerUiState> = _timer.asStateFlow()

    private val _today = MutableStateFlow(DayStats())
    val today: StateFlow<DayStats> = _today.asStateFlow()

    private var weatherJob: Job? = null
    private var searchJob: Job? = null
    private var tickerJob: Job? = null

    init {
        refreshWeather()
    }

    // ----- Weather -----

    /**
     * Loads the weather for the chosen location: the phone's position, or the searched place.
     * Also used by the retry button and after the permission dialog closes.
     */
    fun refreshWeather() {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            _weather.value = WeatherUiState.Loading
            val settings = _settings.value
            _weather.value = resolveCoordinates(settings).fold(
                onSuccess = { coordinates ->
                    weatherRepository.getCurrentWeather(coordinates, settings.unit).fold(
                        onSuccess = { WeatherUiState.Success(it) },
                        onFailure = { WeatherUiState.Error(WeatherError.Network) }
                    )
                },
                onFailure = { error ->
                    if (error is LocationPermissionException) {
                        WeatherUiState.NeedsPermission
                    } else {
                        WeatherUiState.Error(WeatherError.Location)
                    }
                }
            )
            syncIdleDuration()
        }
    }

    private suspend fun resolveCoordinates(settings: MeditationSettings): Result<Coordinates> =
        when {
            settings.useCurrentLocation -> locationRepository.getCurrentLocation()
            settings.place != null -> Result.success(settings.place.coordinates)
            else -> Result.failure(LocationUnavailableException())
        }

    // ----- Location choice and place search -----

    /** Switches to the phone's position. The UI asks for permission if it is missing. */
    fun onUseCurrentLocation() {
        _settings.update { it.copy(useCurrentLocation = true) }
        refreshWeather()
    }

    /** Called when the permission dialog closes, whatever the answer was. */
    fun onPermissionResult() {
        if (_settings.value.useCurrentLocation) refreshWeather()
    }

    fun onPlaceSelected(place: Place) {
        _settings.update { it.copy(useCurrentLocation = false, place = place) }
        clearSearch()
        refreshWeather()
    }

    /** Searches as the user types, waiting a moment so it does not fire on every keystroke. */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        search(query.trim(), debounce = true)
    }

    /** Searches straight away, for the keyboard's search button. */
    fun onSearchSubmit() = search(_searchQuery.value.trim(), debounce = false)

    private fun search(query: String, debounce: Boolean) {
        searchJob?.cancel()
        if (query.length < MIN_SEARCH_LENGTH) {
            _searchState.value = PlaceSearchState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            if (debounce) delay(SEARCH_DEBOUNCE_MILLIS)
            _searchState.value = PlaceSearchState.Loading
            _searchState.value = placeRepository.searchPlaces(query).fold(
                onSuccess = { places ->
                    if (places.isEmpty()) PlaceSearchState.Empty else PlaceSearchState.Results(places)
                },
                onFailure = { PlaceSearchState.Error }
            )
        }
    }

    private fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _searchState.value = PlaceSearchState.Idle
    }

    // ----- Settings -----

    fun onUnitChange(unit: TemperatureUnit) {
        _settings.update { it.copy(unit = unit) }
        refreshWeather()
    }

    fun onSessionMinutesChange(minutes: Int) {
        _settings.update { it.copy(sessionMinutes = minutes) }
        syncIdleDuration()
    }

    fun onUseWeatherSuggestionChange(enabled: Boolean) {
        _settings.update { it.copy(useWeatherSuggestion = enabled) }
        syncIdleDuration()
    }

    // ----- Timer -----

    /** One button does start, pause, resume and "start again" depending on the timer status. */
    fun onStartPause() {
        when (_timer.value.status) {
            TimerStatus.Running -> pause()
            TimerStatus.Finished -> {
                onReset()
                start()
            }
            TimerStatus.Idle, TimerStatus.Paused -> start()
        }
    }

    fun onReset() {
        tickerJob?.cancel()
        _timer.value = TimerUiState.idle(effectiveMinutes())
    }

    private fun start() {
        _timer.update { it.copy(status = TimerStatus.Running) }
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (_timer.value.remainingSeconds > 0) {
                delay(TICK_MILLIS)
                _timer.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            completeSession()
        }
    }

    private fun pause() {
        tickerJob?.cancel()
        _timer.update { it.copy(status = TimerStatus.Paused) }
    }

    private fun completeSession() {
        val minutes = _timer.value.totalSeconds / SECONDS_PER_MINUTE
        _today.update { it.copy(minutes = it.minutes + minutes, sessions = it.sessions + 1) }
        _timer.update { it.copy(status = TimerStatus.Finished) }
    }

    /** Session length in minutes: the weather's suggestion if enabled and available, else the setting. */
    private fun effectiveMinutes(): Int {
        val settings = _settings.value
        val weather = _weather.value
        return if (settings.useWeatherSuggestion && weather is WeatherUiState.Success) {
            weather.weather.condition.suggestedMinutes
        } else {
            settings.sessionMinutes
        }
    }

    /** Only an idle timer follows setting or weather changes. A running session is never altered. */
    private fun syncIdleDuration() {
        if (_timer.value.status == TimerStatus.Idle) {
            _timer.value = TimerUiState.idle(effectiveMinutes())
        }
    }

    companion object {
        /** Builds the ViewModel with the repositories from the application's container. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CalmSkiesApplication
                MeditationViewModel(
                    weatherRepository = app.container.weatherRepository,
                    placeRepository = app.container.placeRepository,
                    locationRepository = app.container.locationRepository
                )
            }
        }
    }
}
