package au.edu.jcu.assessment.utilityapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.utilityapp.R
import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit
import au.edu.jcu.assessment.utilityapp.ui.components.DayStatsRow
import au.edu.jcu.assessment.utilityapp.ui.components.TimerControls
import au.edu.jcu.assessment.utilityapp.ui.components.TimerRing
import au.edu.jcu.assessment.utilityapp.ui.components.WeatherCard

/**
 * Main screen: the weather and a suggested session, the countdown ring with its controls,
 * and today's totals. It only displays state and reports taps; the ViewModel decides what
 * happens next. The column scrolls so it still works on small screens.
 */
@Composable
fun UtilityScreen(
    weatherState: WeatherUiState,
    city: City,
    unit: TemperatureUnit,
    timer: TimerUiState,
    today: DayStats,
    onRetryWeather: () -> Unit,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        WeatherCard(
            state = weatherState,
            city = city,
            unit = unit,
            onRetry = onRetryWeather
        )
        TimerRing(
            progress = timer.progress,
            timeText = timer.timeText,
            statusText = stringResource(timer.status.labelRes())
        )
        TimerControls(
            status = timer.status,
            onStartPause = onStartPause,
            onReset = onReset
        )
        DayStatsRow(stats = today)
    }
}

private fun TimerStatus.labelRes(): Int = when (this) {
    TimerStatus.Idle -> R.string.status_ready
    TimerStatus.Running -> R.string.status_running
    TimerStatus.Paused -> R.string.status_paused
    TimerStatus.Finished -> R.string.status_finished
}
