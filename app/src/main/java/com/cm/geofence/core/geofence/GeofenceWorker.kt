package com.cm.geofence.core.geofence

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cm.geofence.core.notification.NotificationHelper
import com.google.android.gms.location.Geofence
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltWorker
class GeofenceWorker @AssistedInject constructor(
    @Assisted applicationContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(applicationContext, workerParams) {

    companion object {
        private const val GEOFENCE_WORK_NAME = "geofence_event_work"
        const val KEY_TRANSITION_TYPE = "key_transition_type"
        const val KEY_GEOFENCE_IDS = "key_geofence_ids"

        fun run(context: Context, data: Data): UUID {

            val workerRequest =
                OneTimeWorkRequestBuilder<GeofenceWorker>().setInputData(data).build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    GEOFENCE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workerRequest
                )

            return workerRequest.id
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result = coroutineScope {
        Timber.i("[GeofenceWorker] doWork() called")
        try {
            val transitionType = inputData.getInt(KEY_TRANSITION_TYPE, -1)
            val geofenceIds = inputData.getStringArray(KEY_GEOFENCE_IDS)

            if (transitionType == -1 || geofenceIds.isNullOrEmpty()) {
                Timber.e("Invalid input data.")
                return@coroutineScope Result.failure()
            }

            geofenceIds.forEach { id ->
                val title = "Geofence Event"
                val message = when (transitionType) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Timber.d("ENTER event for $id")
                        "You have entered geofence $id"
                    }

                    Geofence.GEOFENCE_TRANSITION_DWELL -> {
                        Timber.d("DWELL event for $id")
                        "You are dwelling in geofence $id"
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        Timber.d("EXIT event for $id")
                        "You have exited geofence $id"
                    }

                    else -> {
                        Timber.w("Unknown transition type: $transitionType")
                        "Unknown geofence event"
                    }
                }
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Timber.e("POST_NOTIFICATIONS permission granted." + notificationHelper)
                    notificationHelper.show(
                        notificationId = id.hashCode(),
                        title = title,
                        message = message,
                        smallIcon = android.R.drawable.ic_dialog_info
                    )
                } else {
                    Timber.e("POST_NOTIFICATIONS permission not granted.")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error processing geofence event " + e)
            if (runAttemptCount > 3) {
                Result.success()
            } else {
                Result.retry()
            }
        } finally {

        }
    }

}