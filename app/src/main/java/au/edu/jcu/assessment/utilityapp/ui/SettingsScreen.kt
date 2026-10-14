package au.edu.jcu.assessment.utilityapp.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.utilityapp.R
import au.edu.jcu.assessment.utilityapp.data.model.Place
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit
import au.edu.jcu.assessment.utilityapp.ui.components.ChoiceChips
import au.edu.jcu.assessment.utilityapp.ui.components.SettingsSection

/** Everything the settings screen can ask the app to do, bundled to keep the parameter list short. */
data class SettingsActions(
    val onUseWeatherSuggestionChange: (Boolean) -> Unit,
    val onSessionMinutesChange: (Int) -> Unit,
    val onUnitChange: (TemperatureUnit) -> Unit,
    val onUseCurrentLocation: () -> Unit,
    val onSearchQueryChange: (String) -> Unit,
    val onSearchSubmit: () -> Unit,
    val onPlaceSelected: (Place) -> Unit
)

/** Settings screen. Every control here changes something on the session screen straight away. */
@Composable
fun SettingsScreen(
    settings: MeditationSettings,
    searchQuery: String,
    searchState: PlaceSearchState,
    actions: SettingsActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LocationSection(
            settings = settings,
            query = searchQuery,
            searchState = searchState,
            actions = actions
        )

        SuggestionToggle(
            checked = settings.useWeatherSuggestion,
            onCheckedChange = actions.onUseWeatherSuggestionChange
        )

        SettingsSection(
            title = stringResource(R.string.settings_length_title),
            description = stringResource(R.string.settings_length_desc)
        ) {
            ChoiceChips(
                options = SESSION_LENGTH_OPTIONS,
                selected = settings.sessionMinutes,
                label = { stringResource(R.string.length_option, it) },
                onSelect = actions.onSessionMinutesChange
            )
        }

        SettingsSection(title = stringResource(R.string.settings_unit_title)) {
            ChoiceChips(
                options = TemperatureUnit.entries,
                selected = settings.unit,
                label = { stringResource(it.labelRes()) },
                onSelect = actions.onUnitChange
            )
        }

        Text(
            text = stringResource(R.string.settings_attribution),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Where the weather comes from: the phone's position, or any place found by searching.
 * There is no fixed list of cities.
 */
@Composable
private fun LocationSection(
    settings: MeditationSettings,
    query: String,
    searchState: PlaceSearchState,
    actions: SettingsActions
) {
    val activeName = if (settings.useCurrentLocation) {
        stringResource(R.string.settings_location_current_name)
    } else {
        settings.place?.displayName.orEmpty()
    }

    SettingsSection(
        title = stringResource(R.string.settings_location_title),
        description = stringResource(R.string.settings_location_showing, activeName)
    ) {
        FilledTonalButton(onClick = actions.onUseCurrentLocation) {
            Icon(
                imageVector = if (settings.useCurrentLocation) Icons.Default.Check else Icons.Default.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.settings_use_current))
        }

        OutlinedTextField(
            value = query,
            onValueChange = actions.onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { actions.onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.search_clear)
                        )
                    }
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { actions.onSearchSubmit() })
        )

        SearchResults(state = searchState, onPlaceSelected = actions.onPlaceSelected)
    }
}

@Composable
private fun SearchResults(state: PlaceSearchState, onPlaceSelected: (Place) -> Unit) {
    when (state) {
        PlaceSearchState.Idle -> Unit

        PlaceSearchState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.search_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PlaceSearchState.Empty -> SearchMessage(R.string.search_empty)
        PlaceSearchState.Error -> SearchMessage(R.string.search_error)

        is PlaceSearchState.Results -> Column {
            state.places.forEach { place ->
                PlaceRow(place = place, onClick = { onPlaceSelected(place) })
            }
        }
    }
}

@Composable
private fun SearchMessage(@StringRes messageRes: Int) {
    Text(
        text = stringResource(messageRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** One search result. The whole row is tappable. */
@Composable
private fun PlaceRow(place: Place, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (place.region.isNotBlank()) {
                Text(
                    text = place.region,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** The whole row is the touch target, so it is easy to hit and works with screen readers. */
@Composable
private fun SuggestionToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_suggestion_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.settings_suggestion_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@StringRes
private fun TemperatureUnit.labelRes(): Int = when (this) {
    TemperatureUnit.Celsius -> R.string.unit_celsius
    TemperatureUnit.Fahrenheit -> R.string.unit_fahrenheit
}
