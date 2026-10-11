package au.edu.jcu.assessment.utilityapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.utilityapp.R
import au.edu.jcu.assessment.utilityapp.ui.TimerStatus

/**
 * Reset on the left, one big start/pause button in the middle. The empty spacer on the right
 * keeps the main button centred.
 */
@Composable
fun TimerControls(
    status: TimerStatus,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = status == TimerStatus.Running
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedIconButton(
            onClick = onReset,
            enabled = status != TimerStatus.Idle,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Replay,
                contentDescription = stringResource(R.string.action_reset)
            )
        }
        FilledIconButton(
            onClick = onStartPause,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(
                    if (isRunning) R.string.action_pause else R.string.action_start
                ),
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.size(56.dp))
    }
}
