package au.edu.jcu.assessment.utilityapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import au.edu.jcu.assessment.utilityapp.data.model.WeatherCondition

/** Card colour and text colour for one kind of weather. */
data class WeatherTint(val container: Color, val content: Color)

private val LightInk = Color(0xFF14231F)
private val DarkInk = Color(0xFFF0F5F2)

/** The weather card takes a soft tint from the current weather, so the sky shows at a glance. */
@Composable
fun weatherTint(condition: WeatherCondition): WeatherTint {
    val dark = isSystemInDarkTheme()
    val container = when (condition) {
        WeatherCondition.Clear -> if (dark) Color(0xFF4A3B14) else Color(0xFFFBE7B5)
        WeatherCondition.Cloudy -> if (dark) Color(0xFF2B353A) else Color(0xFFDDE3E7)
        WeatherCondition.Fog -> if (dark) Color(0xFF30362F) else Color(0xFFE3E6DC)
        WeatherCondition.Rain -> if (dark) Color(0xFF1F3A4D) else Color(0xFFCFE3F2)
        WeatherCondition.Snow -> if (dark) Color(0xFF2C3E4A) else Color(0xFFE2EEF7)
        WeatherCondition.Storm -> if (dark) Color(0xFF3A3355) else Color(0xFFD9D3EC)
    }
    return WeatherTint(container, if (dark) DarkInk else LightInk)
}
