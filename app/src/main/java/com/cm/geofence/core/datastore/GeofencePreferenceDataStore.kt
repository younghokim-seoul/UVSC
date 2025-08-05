package com.cm.geofence.core.datastore

import kotlinx.coroutines.flow.Flow

interface GeofencePreferenceDataStore {
    val geofenceSession: Flow<Set<String>>
    suspend fun updateGeofenceSession(geofenceSession: Set<String>)
}