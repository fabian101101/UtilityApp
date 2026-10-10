package au.edu.jcu.assessment.utilityapp.ui

enum class TimerStatus { Idle, Running, Paused, Finished }

/** State of the meditation countdown. */
data class TimerUiState(
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val status: TimerStatus
) {
    /** 1.0 when the session has not started, falling to 0.0 when it is complete. */
    val progress: Float
        get() = if (totalSeconds == 0) 0f else remainingSeconds.toFloat() / totalSeconds

    /** Remaining time as `MM:SS`. */
    val timeText: String
        get() = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60)

    companion object {
        /** A timer that is ready to start a session of [minutes]. */
        fun idle(minutes: Int): TimerUiState {
            val seconds = minutes * 60
            return TimerUiState(seconds, seconds, TimerStatus.Idle)
        }
    }
}

/** Meditation completed since the app was opened. Not saved between launches. */
data class DayStats(
    val minutes: Int = 0,
    val sessions: Int = 0
)
