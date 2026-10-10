# Calm Skies

A utility app for **CP3406 / CP5307 Assessment 1**. It is a meditation timer that looks at the
weather outside and suggests how long to sit. Open it and see the current weather, a suggested
session, one big timer and today's totals. Nothing else gets in the way.

Built with Kotlin, Jetpack Compose, Material Design 3, a ViewModel, manual dependency injection,
the Repository pattern and Retrofit.

## Screenshots

_Add screenshots here: session screen (ready, running, finished), the weather error state, the
settings screen, and the dark theme._

## Core features

| Feature | What it does |
|---|---|
| Weather at a glance | City, temperature and conditions in a card tinted by the weather (sunny, rain, storm and so on). |
| Weather-based suggestion | The card suggests a session length that suits the weather, for example 15 minutes on a rainy day and 5 on a stormy one. |
| Countdown ring | A large ring empties as the session runs, with the time left in the middle. |
| One-button control | Start, pause and resume with one button, plus reset. The phone buzzes when a session ends. |
| Today's totals | Minutes meditated and sessions completed since the app was opened. |
| Light and dark themes | A calm moss-green theme that follows the phone's light or dark setting. |
| Works without internet | If the weather cannot load, the card says so with a retry button and the timer still works with the chosen session length. |

### Settings (all change the session screen immediately)

- **Use weather suggestion:** session length comes from the weather, or from the length you choose.
- **Session length:** 5, 10, 15, 20 or 30 minutes (used when the suggestion is off).
- **City:** Townsville, Cairns, Brisbane or Singapore. Choosing one reloads the weather.
- **Temperature:** Celsius or Fahrenheit.

Settings are not saved between launches, as the brief allows.

## How it is built

```
MainActivity
  └── UtilityApp()                 connects ViewModel to UI, buzzes at the end of a session
        └── UtilityAppContent()    Scaffold + bottom navigation
              ├── UtilityScreen()  weather card, timer ring, controls, totals
              └── SettingsScreen() switch and chips

MeditationViewModel ── WeatherRepository ── WeatherApi (Retrofit) ── api.open-meteo.com
   StateFlow<WeatherUiState>
   StateFlow<TimerUiState>
   StateFlow<MeditationSettings>
   StateFlow<DayStats>
```

| Folder | Role |
|---|---|
| `data/remote` | Retrofit `WeatherApi` and the JSON models |
| `data/model` | `Weather`, `WeatherCondition`, `City`, `TemperatureUnit` |
| `data` | `WeatherRepository` interface and `NetworkWeatherRepository` |
| `di` | `AppContainer`, which builds Retrofit and the repository |
| `ui` | Screens, `MeditationViewModel`, UI state, settings, weather wording and icons |
| `ui/components` | `WeatherCard`, `TimerRing`, `TimerControls`, `DayStatsRow`, `ChoiceChips`, `SettingsSection` |
| `ui/theme` | Colours, typography, shapes and the weather tints |

## Where each week's topic is used

| Week | Topic | Used in |
|---|---|---|
| 1 | Kotlin + Android Studio | Whole project, `MainActivity.kt` |
| 2 | Compose layouts | `UtilityScreen`, `SettingsScreen`, `WeatherCard`, and the `Canvas` drawing in `TimerRing` |
| 3 | Material Design 3 | `CalmSkiesTheme` (light and dark schemes, shapes, typography), `FilterChip`, `Switch`, `FilledIconButton`, `NavigationBar` |
| 4 | ViewModel, DI, Repository | `MeditationViewModel`, `AppContainer`, `WeatherRepository` |
| 5 | Retrofit | `WeatherApi`, `WeatherDto`, `DefaultAppContainer` |

### Implementation notes

- **State flows one way.** The ViewModel exposes `StateFlow`s, the UI collects them with
  `collectAsStateWithLifecycle`, and user actions go back up as lambdas.
- **One ViewModel for both screens**, so a setting change shows on the session screen, and the
  timer survives screen rotation.
- **Stale requests are cancelled.** Changing city quickly cancels the previous weather request.
- **Failures are values, not crashes.** The repository returns `Result<Weather>`; the ViewModel
  turns a failure into an Error state with a retry button.
- **A running session is never changed.** Settings and weather only change the length of an idle timer.
- **Weather codes** from the API (WMO codes) are mapped to six simple conditions in
  `WeatherCondition.fromCode`.

## Known limitations

- Needs internet for the weather. Without it, the timer works but no suggestion is shown.
- Settings and today's totals reset when the app is closed (the brief says settings need not persist).
- The timer runs inside the app. If Android closes the app in the background, the session stops.
- The city list is fixed (no GPS), so the app needs no location permission.

## Tests

```
./gradlew test
```

- `WeatherConditionTest`: weather code to condition mapping.
- `NetworkWeatherRepositoryTest`: success, night flag, temperature unit sent, network error, missing data.
- `MeditationViewModelTest`: error state, suggested length, chosen length, countdown, pause, finishing a session, reset.

## Running the app

1. Clone the repo and open it in Android Studio.
2. Run on an emulator or device (API 26 or higher) with internet access.

## Credits

- Weather data by [Open-Meteo.com](https://open-meteo.com) (free, no API key).
- Generative AI was used to help learn Kotlin and Compose. See the Declaration of AI-Generated
  Material submitted with the assignment.
