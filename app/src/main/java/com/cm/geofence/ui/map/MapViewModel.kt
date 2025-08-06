package com.cm.geofence.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cm.geofence.core.geofence.GeofenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val geofenceManager: GeofenceManager) : ViewModel() {
    init {
       viewModelScope.launch {

           val latitude = 37.558948
           val longitude = 127.035811
           geofenceManager.addGeofence(id = "geofence_1", lat = latitude, lon = longitude, radius = 2000f)
           geofenceManager.execute()
           Timber.i("Geofence added with ID: geofence_1 at ($latitude, $longitude) with radius 2000m")
       }
    }
}