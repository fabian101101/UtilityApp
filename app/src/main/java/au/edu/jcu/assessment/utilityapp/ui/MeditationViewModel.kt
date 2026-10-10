package au.edu.jcu.assessment.utilityapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import au.edu.jcu.assessment.utilityapp.CalmSkiesApplication
import au.edu.jcu.assessment.utilityapp.data.WeatherRepository
import au.edu.jcu.assessment.utilityapp.data.model.City
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

/**
 * Holds the weather, the meditation timer, today's totals and the settings.
 * Both screens share this one ViewModel so a settings change shows on the session screen,
 * and the timer keeps running when the screen rotates.
 */
class MeditationViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    private val _settings = MutableStateFlow(MeditationSettings())
    val settings: StateFlow<MeditationSettings> = _settings.asStateFlow()

    private val _weather = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weather: StateFlow<WeatherUiState> = _weather.asStateFlow()

    private val _timer = MutableStateFlow(TimerUiState.idle(_settings.value.sessionMinutes))
    val timer: StateFlow<TimerUiState> = _timer.asStateFlow()

    private val _today = MutableStateFlow(DayStats())
    val today: StateFlow<DayStats> = _today.asStateFlow()

    private var weatherJob: Job? = null
    private var tickerJob: Job? = null

    init {
        refreshWeather()
    }

    // ----- Weather -----

    /** Loads the weather for the current city and unit. Also used by the retry button. */
    fun refreshWeather() {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            _weather.value = WeatherUiState.Loading
            val settings = _settings.value
            weatherRepository.getCurrentWeather(settings.city, settings.unit)
                .onSuccess { _weather.value = WeatherUiState.Success(it) }
                .onFailure { _weather.value = WeatherUiState.Error }
            syncIdleDuration()
        }
    }

    // ----- Settings -----

    fun onCityChange(city: City) {
        _settings.update { it.copy(city = city) }
        refreshWeather()
    }

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
        /** Builds the ViewModel with the repository from the application's container. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CalmSkiesApplication
                MeditationViewModel(app.container.weatherRepository)
            }
        }
    }
}
