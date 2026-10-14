package au.edu.jcu.assessment.utilityapp.ui

import au.edu.jcu.assessment.utilityapp.data.LocationPermissionException
import au.edu.jcu.assessment.utilityapp.data.LocationRepository
import au.edu.jcu.assessment.utilityapp.data.LocationUnavailableException
import au.edu.jcu.assessment.utilityapp.data.PlaceRepository
import au.edu.jcu.assessment.utilityapp.data.WeatherRepository
import au.edu.jcu.assessment.utilityapp.data.model.Coordinates
import au.edu.jcu.assessment.utilityapp.data.model.Place
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit
import au.edu.jcu.assessment.utilityapp.data.model.Weather
import au.edu.jcu.assessment.utilityapp.data.model.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MeditationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val phonePosition = Coordinates(latitude = -1.29, longitude = 36.82)
    private val brisbane = Place("Brisbane", "Queensland, Australia", -27.47, 153.03)

    /** Returns whatever result the test sets, and remembers where it was asked about. */
    private class FakeWeatherRepository(var result: Result<Weather>) : WeatherRepository {
        var lastCoordinates: Coordinates? = null

        override suspend fun getCurrentWeather(
            coordinates: Coordinates,
            unit: TemperatureUnit
        ): Result<Weather> {
            lastCoordinates = coordinates
            return result
        }
    }

    private class FakeLocationRepository(var result: Result<Coordinates>) : LocationRepository {
        override suspend fun getCurrentLocation(): Result<Coordinates> = result
    }

    private class FakePlaceRepository(var result: Result<List<Place>>) : PlaceRepository {
        var searchCount = 0

        override suspend fun searchPlaces(query: String): Result<List<Place>> {
            searchCount++
            return result
        }
    }

    private fun weatherOf(condition: WeatherCondition) =
        Result.success(Weather(temperature = 20, condition = condition, isDay = true))

    private var weatherRepository = FakeWeatherRepository(weatherOf(WeatherCondition.Clear))
    private var locationRepository = FakeLocationRepository(Result.success(phonePosition))
    private var placeRepository = FakePlaceRepository(Result.success(emptyList()))

    private fun createViewModel() =
        MeditationViewModel(weatherRepository, placeRepository, locationRepository)

    private fun TestScope.createViewModelAndLoad(): MeditationViewModel {
        val viewModel = createViewModel()
        advanceUntilIdle()
        return viewModel
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ----- Location and weather -----

    @Test
    fun usesPhonePositionByDefault() = runTest(dispatcher) {
        val viewModel = createViewModelAndLoad()

        assertTrue(viewModel.weather.value is WeatherUiState.Success)
        assertEquals(phonePosition, weatherRepository.lastCoordinates)
    }

    @Test
    fun deniedPermissionShowsNeedsPermission() = runTest(dispatcher) {
        locationRepository.result = Result.failure(LocationPermissionException())

        val viewModel = createViewModelAndLoad()

        assertEquals(WeatherUiState.NeedsPermission, viewModel.weather.value)
    }

    @Test
    fun grantingPermissionLoadsTheWeather() = runTest(dispatcher) {
        locationRepository.result = Result.failure(LocationPermissionException())
        val viewModel = createViewModelAndLoad()

        locationRepository.result = Result.success(phonePosition)
        viewModel.onPermissionResult()
        advanceUntilIdle()

        assertTrue(viewModel.weather.value is WeatherUiState.Success)
    }

    @Test
    fun unavailableLocationShowsLocationError() = runTest(dispatcher) {
        locationRepository.result = Result.failure(LocationUnavailableException())

        val viewModel = createViewModelAndLoad()

        assertEquals(WeatherUiState.Error(WeatherError.Location), viewModel.weather.value)
    }

    @Test
    fun failedWeatherRequestShowsNetworkError() = runTest(dispatcher) {
        weatherRepository.result = Result.failure(IOException())

        val viewModel = createViewModelAndLoad()

        assertEquals(WeatherUiState.Error(WeatherError.Network), viewModel.weather.value)
    }

    // ----- Place search -----

    @Test
    fun searchWaitsForTypingToPauseThenShowsResults() = runTest(dispatcher) {
        placeRepository.result = Result.success(listOf(brisbane))
        val viewModel = createViewModelAndLoad()

        viewModel.onSearchQueryChange("Bris")
        advanceTimeBy(100)
        runCurrent()
        assertEquals(PlaceSearchState.Idle, viewModel.searchState.value)

        advanceUntilIdle()
        assertEquals(PlaceSearchState.Results(listOf(brisbane)), viewModel.searchState.value)
    }

    @Test
    fun tooShortQueryDoesNotSearch() = runTest(dispatcher) {
        val viewModel = createViewModelAndLoad()

        viewModel.onSearchQueryChange("B")
        advanceUntilIdle()

        assertEquals(PlaceSearchState.Idle, viewModel.searchState.value)
        assertEquals(0, placeRepository.searchCount)
    }

    @Test
    fun noMatchesShowsEmptyState() = runTest(dispatcher) {
        val viewModel = createViewModelAndLoad()

        viewModel.onSearchQueryChange("zzzz")
        advanceUntilIdle()

        assertEquals(PlaceSearchState.Empty, viewModel.searchState.value)
    }

    @Test
    fun failedSearchShowsErrorState() = runTest(dispatcher) {
        placeRepository.result = Result.failure(IOException())
        val viewModel = createViewModelAndLoad()

        viewModel.onSearchQueryChange("Nairobi")
        advanceUntilIdle()

        assertEquals(PlaceSearchState.Error, viewModel.searchState.value)
    }

    @Test
    fun choosingAPlaceLoadsItsWeatherAndClearsTheSearch() = runTest(dispatcher) {
        placeRepository.result = Result.success(listOf(brisbane))
        val viewModel = createViewModelAndLoad()
        viewModel.onSearchQueryChange("Bris")
        advanceUntilIdle()

        viewModel.onPlaceSelected(brisbane)
        advanceUntilIdle()

        assertFalse(viewModel.settings.value.useCurrentLocation)
        assertEquals(brisbane, viewModel.settings.value.place)
        assertEquals(brisbane.coordinates, weatherRepository.lastCoordinates)
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(PlaceSearchState.Idle, viewModel.searchState.value)
    }

    @Test
    fun switchingBackToCurrentLocationUsesThePhonePosition() = runTest(dispatcher) {
        val viewModel = createViewModelAndLoad()
        viewModel.onPlaceSelected(brisbane)
        advanceUntilIdle()

        viewModel.onUseCurrentLocation()
        advanceUntilIdle()

        assertTrue(viewModel.settings.value.useCurrentLocation)
        assertEquals(phonePosition, weatherRepository.lastCoordinates)
    }

    // ----- Session length -----

    @Test
    fun weatherSuggestionSetsSessionLength() = runTest(dispatcher) {
        weatherRepository.result = weatherOf(WeatherCondition.Rain)

        val viewModel = createViewModelAndLoad()

        assertEquals(15 * 60, viewModel.timer.value.totalSeconds)
    }

    @Test
    fun turningSuggestionOffUsesChosenLength() = runTest(dispatcher) {
        weatherRepository.result = weatherOf(WeatherCondition.Rain)
        val viewModel = createViewModelAndLoad()

        viewModel.onUseWeatherSuggestionChange(false)
        viewModel.onSessionMinutesChange(20)

        assertEquals(20 * 60, viewModel.timer.value.totalSeconds)
    }

    // ----- Timer -----

    @Test
    fun timerCountsDownOneSecondAtATime() = runTest(dispatcher) {
        val viewModel = createViewModelAndLoad()
        val total = viewModel.timer.value.totalSeconds

        viewModel.onStartPause()
        advanceTimeBy(3_000)
        runCurrent()

        assertEquals(TimerStatus.Running, viewModel.timer.value.status)
        assertEquals(total - 3, viewModel.timer.value.remainingSeconds)
    }

    @Test
    fun pauseStopsTheCountdown() = runTest(dispatcher) {
        val viewModel = createViewModelAndLoad()

        viewModel.onStartPause()
        advanceTimeBy(2_000)
        runCurrent()
        viewModel.onStartPause()
        val remainingWhenPaused = viewModel.timer.value.remainingSeconds
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(TimerStatus.Paused, viewModel.timer.value.status)
        assertEquals(remainingWhenPaused, viewModel.timer.value.remainingSeconds)
    }

    @Test
    fun finishingASessionAddsToTodaysTotals() = runTest(dispatcher) {
        weatherRepository.result = weatherOf(WeatherCondition.Storm)
        val viewModel = createViewModelAndLoad()

        viewModel.onStartPause()
        advanceUntilIdle()

        assertEquals(TimerStatus.Finished, viewModel.timer.value.status)
        assertEquals(DayStats(minutes = 5, sessions = 1), viewModel.today.value)
    }

    @Test
    fun resetReturnsToReadyWithoutCountingTheSession() = runTest(dispatcher) {
        val viewModel = createViewModelAndLoad()

        viewModel.onStartPause()
        advanceTimeBy(4_000)
        runCurrent()
        viewModel.onReset()

        assertEquals(TimerStatus.Idle, viewModel.timer.value.status)
        assertEquals(viewModel.timer.value.totalSeconds, viewModel.timer.value.remainingSeconds)
        assertEquals(DayStats(), viewModel.today.value)
    }
}
