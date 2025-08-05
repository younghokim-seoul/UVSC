package com.cm.geofence.core.geofence

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.gms.location.Geofence
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.util.UUID

@HiltWorker
class GeofenceWorker @AssistedInject constructor(
    @Assisted applicationContext: Context,
    @Assisted workerParams: WorkerParameters,
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
                when (transitionType) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Timber.d("ENTER event for $id")
                        // TODO: 상태를 'ENTERING'으로 변경하거나, 가벼운 진입 로그 전송
                    }

                    Geofence.GEOFENCE_TRANSITION_DWELL -> {
                        Timber.d("DWELL event for $id")
                        // TODO: '골프장 입장'으로 최종 확정. 환영 알림 표시 및 핵심 기능 활성화
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        Timber.d("EXIT event for $id")
                        // TODO: '골프장 퇴장' 처리. 관련 기능 비활성화
                    }

                    else -> {
                        Timber.w("Unknown transition type: $transitionType")
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount > 3) {
                Result.success()
            } else {
                Result.retry()
            }
        } finally {

        }
    }

}