package au.edu.jcu.assessment.utilityapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.utilityapp.R
import au.edu.jcu.assessment.utilityapp.data.model.Cities
import au.edu.jcu.assessment.utilityapp.data.model.City
import au.edu.jcu.assessment.utilityapp.data.model.TemperatureUnit
import au.edu.jcu.assessment.utilityapp.ui.components.ChoiceChips
import au.edu.jcu.assessment.utilityapp.ui.components.SettingsSection

/** Settings screen. Every control here changes something on the session screen straight away. */
@Composable
fun SettingsScreen(
    settings: MeditationSettings,
    onUseWeatherSuggestionChange: (Boolean) -> Unit,
    onSessionMinutesChange: (Int) -> Unit,
    onCityChange: (City) -> Unit,
    onUnitChange: (TemperatureUnit) -> Unit,
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

        SuggestionToggle(
            checked = settings.useWeatherSuggestion,
            onCheckedChange = onUseWeatherSuggestionChange
        )

        SettingsSection(
            title = stringResource(R.string.settings_length_title),
            description = stringResource(R.string.settings_length_desc)
        ) {
            ChoiceChips(
                options = SESSION_LENGTH_OPTIONS,
                selected = settings.sessionMinutes,
                label = { stringResource(R.string.length_option, it) },
                onSelect = onSessionMinutesChange
            )
        }

        SettingsSection(title = stringResource(R.string.settings_city_title)) {
            ChoiceChips(
                options = Cities.all,
                selected = settings.city,
                label = { it.name },
                onSelect = onCityChange
            )
        }

        SettingsSection(title = stringResource(R.string.settings_unit_title)) {
            ChoiceChips(
                options = TemperatureUnit.entries,
                selected = settings.unit,
                label = { stringResource(it.labelRes()) },
                onSelect = onUnitChange
            )
        }

        Text(
            text = stringResource(R.string.settings_attribution),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

private fun TemperatureUnit.labelRes(): Int = when (this) {
    TemperatureUnit.Celsius -> R.string.unit_celsius
    TemperatureUnit.Fahrenheit -> R.string.unit_fahrenheit
}
