package au.edu.jcu.assessment.utilityapp.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.utilityapp.R
import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit
import au.edu.jcu.assessment.utilityapp.data.model.Weather
import au.edu.jcu.assessment.utilityapp.data.model.WeatherCondition
import au.edu.jcu.assessment.utilityapp.ui.theme.CalmSkiesTheme

/** The two bottom-bar destinations. */
private enum class UtilityTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    Session(R.string.tab_session, Icons.Default.Spa),
    Settings(R.string.tab_settings, Icons.Default.Settings)
}

/**
 * App entry point. Connects the ViewModel to the UI and buzzes the phone when a session ends.
 * Everything visual is in [UtilityAppContent].
 */
@Composable
fun UtilityApp(viewModel: MeditationViewModel = viewModel(factory = MeditationViewModel.Factory)) {
    val weather by viewModel.weather.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val today by viewModel.today.collectAsStateWithLifecycle()

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
            timer = timer,
            today = today,
            onRetryWeather = viewModel::refreshWeather,
            onStartPause = viewModel::onStartPause,
            onReset = viewModel::onReset,
            onUseWeatherSuggestionChange = viewModel::onUseWeatherSuggestionChange,
            onSessionMinutesChange = viewModel::onSessionMinutesChange,
            onCityChange = viewModel::onCityChange,
            onUnitChange = viewModel::onUnitChange
        )
    }
}

/** Scaffold with bottom navigation. It has no ViewModel, so it can be previewed with fake state. */
@Composable
fun UtilityAppContent(
    weatherState: WeatherUiState,
    settings: MeditationSettings,
    timer: TimerUiState,
    today: DayStats,
    onRetryWeather: () -> Unit,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onUseWeatherSuggestionChange: (Boolean) -> Unit,
    onSessionMinutesChange: (Int) -> Unit,
    onCityChange: (City) -> Unit,
    onUnitChange: (TemperatureUnit) -> Unit
) {
    // rememberSaveable keeps the selected tab when the screen rotates.
    var selectedTab by rememberSaveable { mutableStateOf(UtilityTab.Session) }

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
                city = settings.city,
                unit = settings.unit,
                timer = timer,
                today = today,
                onRetryWeather = onRetryWeather,
                onStartPause = onStartPause,
                onReset = onReset,
                modifier = contentModifier
            )

            UtilityTab.Settings -> SettingsScreen(
                settings = settings,
                onUseWeatherSuggestionChange = onUseWeatherSuggestionChange,
                onSessionMinutesChange = onSessionMinutesChange,
                onCityChange = onCityChange,
                onUnitChange = onUnitChange,
                modifier = contentModifier
            )
        }
    }
}

// ---------- Previews ----------

private val previewSettings = MeditationSettings()

@Composable
private fun PreviewApp(weatherState: WeatherUiState, timer: TimerUiState = TimerUiState.idle(15)) {
    CalmSkiesTheme {
        UtilityAppContent(
            weatherState = weatherState,
            settings = previewSettings,
            timer = timer,
            today = DayStats(minutes = 25, sessions = 2),
            onRetryWeather = {},
            onStartPause = {},
            onReset = {},
            onUseWeatherSuggestionChange = {},
            onSessionMinutesChange = {},
            onCityChange = {},
            onUnitChange = {}
        )
    }
}

private fun sampleWeather(condition: WeatherCondition) =
    WeatherUiState.Success(Weather(temperature = 19, condition = condition, isDay = true))

@Preview(name = "Rain, ready", showSystemUi = true)
@Composable
private fun RainPreview() = PreviewApp(sampleWeather(WeatherCondition.Rain))

@Preview(name = "Clear, running", showSystemUi = true)
@Composable
private fun ClearRunningPreview() = PreviewApp(
    weatherState = sampleWeather(WeatherCondition.Clear),
    timer = TimerUiState(totalSeconds = 600, remainingSeconds = 410, status = TimerStatus.Running)
)

@Preview(name = "Storm, dark", showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun StormDarkPreview() = PreviewApp(sampleWeather(WeatherCondition.Storm))

@Preview(name = "Weather error", showSystemUi = true)
@Composable
private fun ErrorPreview() = PreviewApp(WeatherUiState.Error, TimerUiState.idle(10))

@Preview(name = "Loading", showSystemUi = true)
@Composable
private fun LoadingPreview() = PreviewApp(WeatherUiState.Loading, TimerUiState.idle(10))
