package au.edu.jcu.assessment.utilityapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.utilityapp.R
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit
import au.edu.jcu.assessment.utilityapp.data.model.Weather
import au.edu.jcu.assessment.utilityapp.ui.WeatherError
import au.edu.jcu.assessment.utilityapp.ui.WeatherUiState
import au.edu.jcu.assessment.utilityapp.ui.icon
import au.edu.jcu.assessment.utilityapp.ui.labelRes
import au.edu.jcu.assessment.utilityapp.ui.suggestionRes
import au.edu.jcu.assessment.utilityapp.ui.theme.weatherTint

/**
 * Card at the top of the session screen. It shows the weather and, using it, a suggested
 * session length. Every state keeps the same card shape so the layout stays still.
 */
@Composable
fun WeatherCard(
    state: WeatherUiState,
    locationName: String,
    unit: TemperatureUnit,
    onRetry: () -> Unit,
    onAllowLocation: () -> Unit,
    onSearchPlace: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        WeatherUiState.Loading -> CardMessage(modifier) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.weather_loading),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        WeatherUiState.NeedsPermission -> PermissionCard(
            onAllowLocation = onAllowLocation,
            onSearchPlace = onSearchPlace,
            modifier = modifier
        )

        is WeatherUiState.Error -> CardMessage(modifier) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (state.reason == WeatherError.Location) {
                            R.string.location_error_title
                        } else {
                            R.string.weather_error_title
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(
                        if (state.reason == WeatherError.Location) {
                            R.string.location_error_body
                        } else {
                            R.string.weather_error_body
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
        }

        is WeatherUiState.Success -> WeatherDetails(state.weather, locationName, unit, modifier)
    }
}

@Composable
private fun WeatherDetails(
    weather: Weather,
    locationName: String,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    val tint = weatherTint(weather.condition)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = tint.container,
        contentColor = tint.content
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "${weather.temperature}${unit.symbol}",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = stringResource(weather.condition.labelRes(weather.isDay)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(
                    imageVector = weather.condition.icon(weather.isDay),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(
                    weather.condition.suggestionRes(),
                    weather.condition.suggestedMinutes
                ),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/** Shown when "use my location" is on but permission has not been given. */
@Composable
private fun PermissionCard(
    onAllowLocation: () -> Unit,
    onSearchPlace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.permission_title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.permission_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAllowLocation) {
                    Text(stringResource(R.string.action_allow_location))
                }
                TextButton(onClick = onSearchPlace) {
                    Text(stringResource(R.string.action_search_place))
                }
            }
        }
    }
}

@Composable
private fun CardMessage(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}
