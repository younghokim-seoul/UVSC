package com.cm.geofence.core.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber
import androidx.work.workDataOf

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val nonNullContext = context ?: return
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) } ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Timber.e("Geofence error: $errorMessage")
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        val alertString = "Geofence Alert : Trigger $transitionType Transition $triggeringGeofences"

        Timber.d(alertString)

        val geofenceIds = triggeringGeofences?.map { it.requestId }?.toTypedArray()


        val data = workDataOf(GeofenceWorker.KEY_TRANSITION_TYPE to transitionType ,GeofenceWorker.KEY_GEOFENCE_IDS to geofenceIds)

        Timber.d("geofenceIds %s", geofenceIds)


        GeofenceWorker.run(nonNullContext,data)


    }
}