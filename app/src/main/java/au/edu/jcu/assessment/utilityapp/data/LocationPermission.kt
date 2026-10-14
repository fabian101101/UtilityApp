package au.edu.jcu.assessment.utilityapp.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/** Both are requested together. The user may grant only approximate location, which is enough. */
val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)

/** True if the user has granted precise or approximate location. */
fun Context.hasLocationPermission(): Boolean =
    LOCATION_PERMISSIONS.any {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
