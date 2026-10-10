package au.edu.jcu.assessment.utilityapp.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import au.edu.jcu.assessment.utilityapp.R
import au.edu.jcu.assessment.utilityapp.data.model.WeatherCondition

/*
 * UI wording and icons for weather. They live here, not in the data layer,
 * so the data layer stays free of Android resources.
 */

fun WeatherCondition.icon(isDay: Boolean): ImageVector = when (this) {
    WeatherCondition.Clear -> if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
    WeatherCondition.Cloudy -> Icons.Default.Cloud
    WeatherCondition.Fog -> Icons.Default.Dehaze
    WeatherCondition.Rain -> Icons.Default.Opacity
    WeatherCondition.Snow -> Icons.Default.AcUnit
    WeatherCondition.Storm -> Icons.Default.FlashOn
}

@StringRes
fun WeatherCondition.labelRes(isDay: Boolean): Int = when (this) {
    WeatherCondition.Clear -> if (isDay) R.string.condition_clear_day else R.string.condition_clear_night
    WeatherCondition.Cloudy -> R.string.condition_cloudy
    WeatherCondition.Fog -> R.string.condition_fog
    WeatherCondition.Rain -> R.string.condition_rain
    WeatherCondition.Snow -> R.string.condition_snow
    WeatherCondition.Storm -> R.string.condition_storm
}

@StringRes
fun WeatherCondition.suggestionRes(): Int = when (this) {
    WeatherCondition.Clear -> R.string.suggestion_clear
    WeatherCondition.Cloudy -> R.string.suggestion_cloudy
    WeatherCondition.Fog -> R.string.suggestion_fog
    WeatherCondition.Rain -> R.string.suggestion_rain
    WeatherCondition.Snow -> R.string.suggestion_snow
    WeatherCondition.Storm -> R.string.suggestion_storm
}
