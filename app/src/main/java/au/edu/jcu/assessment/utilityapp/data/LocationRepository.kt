package au.edu.jcu.assessment.utilityapp.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import au.edu.jcu.assessment.utilityapp.data.model.Coordinates
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** The user has not allowed location access. */
class LocationPermissionException : Exception("Location permission not granted")

/** Permission is granted but the phone could not work out where it is (for example, location is off). */
class LocationUnavailableException : Exception("Device location is unavailable")

/** Finds where the device is right now. */
interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Coordinates>
}

/** Uses Google Play services' fused location provider. */
class FusedLocationRepository(context: Context) : LocationRepository {

    private val appContext = context.applicationContext
    private val client by lazy { LocationServices.getFusedLocationProviderClient(appContext) }

    override suspend fun getCurrentLocation(): Result<Coordinates> {
        if (!appContext.hasLocationPermission()) {
            return Result.failure(LocationPermissionException())
        }
        return try {
            val location = requestLocation()
            if (location == null) {
                Result.failure(LocationUnavailableException())
            } else {
                Result.success(Coordinates(location.latitude, location.longitude))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Wraps the callback-style Task in a suspend function that can be cancelled. */
    @SuppressLint("MissingPermission") // Checked in getCurrentLocation() just before this runs.
    private suspend fun requestLocation(): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellation = CancellationTokenSource()
            continuation.invokeOnCancellation { cancellation.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
}
