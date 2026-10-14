package au.edu.jcu.assessment.utilityapp.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.utilityapp.R
import au.edu.jcu.assessment.utilityapp.data.LOCATION_PERMISSIONS
import au.edu.jcu.assessment.utilityapp.data.hasLocationPermission
import au.edu.jcu.assessment.utilityapp.data.model.Place
import au.edu.jcu.assessment.utilityapp.data.model.Weather
import au.edu.jcu.assessment.utilityapp.data.model.WeatherCondition
import au.edu.jcu.assessment.utilityapp.ui.theme.CalmSkiesTheme

/** The two bottom-bar destinations. */
private enum class UtilityTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    Session(R.string.tab_session, Icons.Default.Spa),
    Settings(R.string.tab_settings, Icons.Default.Settings)
}

/**
 * App entry point. Connects the ViewModel to the UI, asks for location permission,
 * and buzzes the phone when a session ends. Everything visual is in [UtilityAppContent].
 */
@Composable
fun UtilityApp(viewModel: MeditationViewModel = viewModel(factory = MeditationViewModel.Factory)) {
    val weather by viewModel.weather.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // The system permission dialog. Whatever the answer, reload the weather afterwards.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.onPermissionResult() }
    val requestLocation = { permissionLauncher.launch(LOCATION_PERMISSIONS) }

    // Ask once, automatically, the first time the app opens without permission.
    var askedOnLaunch by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(weather) {
        if (weather == WeatherUiState.NeedsPermission && !askedOnLaunch) {
            askedOnLaunch = true
            requestLocation()
        }
    }

    val haptics = LocalHapticFeedback.current
    LaunchedEffect(timer.status) {
        if (timer.status == TimerStatus.Finished) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    CalmSkiesTheme {
        UtilityAppContent(
            weatherState = weather,
            settings = settings,
            searchQuery = searchQuery,
            searchState = searchState,
            timer = timer,
            today = today,
            onRetryWeather = viewModel::refreshWeather,
            onAllowLocation = requestLocation,
            onStartPause = viewModel::onStartPause,
            onReset = viewModel::onReset,
            settingsActions = SettingsActions(
                onUseWeatherSuggestionChange = viewModel::onUseWeatherSuggestionChange,
                onSessionMinutesChange = viewModel::onSessionMinutesChange,
                onUnitChange = viewModel::onUnitChange,
                onUseCurrentLocation = {
                    viewModel.onUseCurrentLocation()
                    if (!context.hasLocationPermission()) requestLocation()
                },
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSearchSubmit = viewModel::onSearchSubmit,
                onPlaceSelected = viewModel::onPlaceSelected
            )
        )
    }
}

/** Scaffold with bottom navigation. It has no ViewModel, so it can be previewed with fake state. */
@Composable
fun UtilityAppContent(
    weatherState: WeatherUiState,
    settings: MeditationSettings,
    searchQuery: String,
    searchState: PlaceSearchState,
    timer: TimerUiState,
    today: DayStats,
    onRetryWeather: () -> Unit,
    onAllowLocation: () -> Unit,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    settingsActions: SettingsActions
) {
    // rememberSaveable keeps the selected tab when the screen rotates.
    var selectedTab by rememberSaveable { mutableStateOf(UtilityTab.Session) }

    val locationName = if (settings.useCurrentLocation) {
        stringResource(R.string.location_current)
    } else {
        settings.place?.name.orEmpty()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                UtilityTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            UtilityTab.Session -> UtilityScreen(
                weatherState = weatherState,
                locationName = locationName,
                unit = settings.unit,
                timer = timer,
                today = today,
                onRetryWeather = onRetryWeather,
                onAllowLocation = onAllowLocation,
                onSearchPlace = { selectedTab = UtilityTab.Settings },
                onStartPause = onStartPause,
                onReset = onReset,
                modifier = contentModifier
            )

            UtilityTab.Settings -> SettingsScreen(
                settings = settings,
                searchQuery = searchQuery,
                searchState = searchState,
                actions = settingsActions,
                modifier = contentModifier
            )
        }
    }
}

// ---------- Previews ----------

private val brisbane = Place("Brisbane", "Queensland, Australia", -27.47, 153.03)

@Composable
private fun PreviewApp(
    weatherState: WeatherUiState,
    timer: TimerUiState = TimerUiState.idle(15),
    settings: MeditationSettings = MeditationSettings(),
    searchState: PlaceSearchState = PlaceSearchState.Idle
) {
    CalmSkiesTheme {
        UtilityAppContent(
            weatherState = weatherState,
            settings = settings,
            searchQuery = "",
            searchState = searchState,
            timer = timer,
            today = DayStats(minutes = 25, sessions = 2),
            onRetryWeather = {},
            onAllowLocation = {},
            onStartPause = {},
            onReset = {},
            settingsActions = SettingsActions(
                onUseWeatherSuggestionChange = {},
                onSessionMinutesChange = {},
                onUnitChange = {},
                onUseCurrentLocation = {},
                onSearchQueryChange = {},
                onSearchSubmit = {},
                onPlaceSelected = {}
            )
        )
    }
}

private fun sampleWeather(condition: WeatherCondition) =
    WeatherUiState.Success(Weather(temperature = 19, condition = condition, isDay = true))

@Preview(name = "Rain, current location", showSystemUi = true)
@Composable
private fun RainPreview() = PreviewApp(sampleWeather(WeatherCondition.Rain))

@Preview(name = "Clear, searched place, running", showSystemUi = true)
@Composable
private fun ClearRunningPreview() = PreviewApp(
    weatherState = sampleWeather(WeatherCondition.Clear),
    timer = TimerUiState(totalSeconds = 600, remainingSeconds = 410, status = TimerStatus.Running),
    settings = MeditationSettings(useCurrentLocation = false, place = brisbane)
)

@Preview(name = "Storm, dark", showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun StormDarkPreview() = PreviewApp(sampleWeather(WeatherCondition.Storm))

@Preview(name = "Needs permission", showSystemUi = true)
@Composable
private fun PermissionPreview() = PreviewApp(WeatherUiState.NeedsPermission)

@Preview(name = "Location error", showSystemUi = true)
@Composable
private fun LocationErrorPreview() = PreviewApp(WeatherUiState.Error(WeatherError.Location))

@Preview(name = "Loading", showSystemUi = true)
@Composable
private fun LoadingPreview() = PreviewApp(WeatherUiState.Loading, TimerUiState.idle(10))
