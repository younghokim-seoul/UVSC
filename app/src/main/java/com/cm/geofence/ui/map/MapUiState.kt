package com.cm.geofence.ui.map

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MapUiState(
    val geofenceSearchResults: ImmutableList<String> = persistentListOf(),
)