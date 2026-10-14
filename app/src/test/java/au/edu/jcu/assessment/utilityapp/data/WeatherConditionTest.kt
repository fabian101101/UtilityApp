package au.edu.jcu.assessment.utilityapp.data

import au.edu.jcu.assessment.utilityapp.data.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun clearCodes() {
        assertEquals(WeatherCondition.Clear, WeatherCondition.fromCode(0))
        assertEquals(WeatherCondition.Clear, WeatherCondition.fromCode(1))
    }

    @Test
    fun cloudyCodes() {
        assertEquals(WeatherCondition.Cloudy, WeatherCondition.fromCode(2))
        assertEquals(WeatherCondition.Cloudy, WeatherCondition.fromCode(3))
    }

    @Test
    fun fogCodes() {
        assertEquals(WeatherCondition.Fog, WeatherCondition.fromCode(45))
        assertEquals(WeatherCondition.Fog, WeatherCondition.fromCode(48))
    }

    @Test
    fun drizzleAndRainCodesAreRain() {
        assertEquals(WeatherCondition.Rain, WeatherCondition.fromCode(53))
        assertEquals(WeatherCondition.Rain, WeatherCondition.fromCode(63))
        assertEquals(WeatherCondition.Rain, WeatherCondition.fromCode(81))
    }

    @Test
    fun snowCodes() {
        assertEquals(WeatherCondition.Snow, WeatherCondition.fromCode(73))
        assertEquals(WeatherCondition.Snow, WeatherCondition.fromCode(86))
    }

    @Test
    fun thunderstormCodes() {
        assertEquals(WeatherCondition.Storm, WeatherCondition.fromCode(95))
        assertEquals(WeatherCondition.Storm, WeatherCondition.fromCode(99))
    }

    @Test
    fun unknownCodeFallsBackToCloudy() {
        assertEquals(WeatherCondition.Cloudy, WeatherCondition.fromCode(1234))
    }
}
