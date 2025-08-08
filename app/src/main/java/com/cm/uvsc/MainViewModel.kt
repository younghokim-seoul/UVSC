package com.cm.uvsc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cm.uvsc.ble.AchPacket
import com.cm.uvsc.ble.AchsPacket
import com.cm.uvsc.ble.AchtPacket
import com.cm.uvsc.ble.AcsPacket
import com.cm.uvsc.ble.AcsPacket.ModeChange.ModeType
import com.cm.uvsc.ble.BleRepository
import com.cm.uvsc.ble.ReceivePacket
import com.cm.uvsc.ble.SetChargeMode
import com.cm.uvsc.route.Navigator
import com.cm.uvsc.route.RouteHome
import com.cm.uvsc.route.RouteReceiveHistory
import com.cm.uvsc.route.RouteUvscHistory
import com.cm.uvsc.ui.UiEvent
import com.cm.uvsc.ui.history.UvscHistory
import com.cm.uvsc.ui.home.HomeUiState
import com.cm.uvsc.ui.home.UvscInfo
import com.cm.uvsc.ui.home.emptyCharging
import com.cm.uvsc.ui.home.toCharging
import com.cm.uvsc.ui.home.toUvscInProgress
import com.cm.uvsc.ui.receive.ReceiveData
import com.cm.uvsc.ui.receive.ReceiveScanResult
import com.cm.uvsc.util.achPackets
import com.cm.uvsc.util.achsPackets
import com.cm.uvsc.util.achtPackets
import com.cm.uvsc.util.acsPackets
import com.cm.uvsc.util.splitTrimmed
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val navigator: Navigator,
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _homeUiState = MutableStateFlow<HomeUiState>(emptyCharging)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _uvscHistoryList = MutableStateFlow<List<UvscHistory>>(emptyList())
    val uvscHistoryList: StateFlow<List<UvscHistory>> = _uvscHistoryList.asStateFlow()

    private val _receiveDataList = MutableStateFlow<List<ReceiveData>>(emptyList())
    val receiveDataList: StateFlow<List<ReceiveData>> = _receiveDataList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val scannedDevices: StateFlow<List<RxBleDevice>> = bleRepository.scannedDevices
        .map { it.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDevices = combine(
        searchQuery.debounce(300),
        scannedDevices
    ) { query, devices ->
        devices
            .takeIf { query.isNotBlank() }
            ?.filter { it.name?.contains(query, true) == true } ?: devices
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isConnected: StateFlow<Boolean> = bleRepository.connectionState
        .map { it == RxBleConnection.RxBleConnectionState.CONNECTED }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var modeType: ModeType = ModeType.Charging

    init {
        Timber.i("bleRepository => $bleRepository")
        observeConnectionState()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            _isConnected.collect {
                if (it) {
                    stopScan()
                    observeHomePacket()
                    observeHistoryPacket()
                    observeReceiveData()
                } else {
                    removeData()
                }
            }
        }
    }

    fun startScan() {
        bleRepository.startScan()
    }

    fun stopScan() {
        bleRepository.stopScan()
    }

    fun disconnect() {
        bleRepository.disconnect()
    }

    private fun observeHomePacket() {
        val homeFlow = bleRepository.latestPacketsMap

        homeFlow.acsPackets()
            .filterIsInstance<AcsPacket.ModeChange>()
            .onEach { updateUiStateFromAcs(it) }
            .launchIn(viewModelScope)

        homeFlow.acsPackets()
            .filterIsInstance<AcsPacket.Progress>()
            .onEach { updateUvscProgress(it) }
            .launchIn(viewModelScope)

        homeFlow
            .achtPackets()
            .onEach { updateUiStateFromAcht(it) }
            .launchIn(viewModelScope)

        homeFlow
            .achsPackets()
            .onEach { updateUiStateFromAchs(it) }
            .launchIn(viewModelScope)
    }

    private fun updateUvscProgress(packet: AcsPacket.Progress) {
        _homeUiState.update { state ->
            val info = state as? UvscInfo

            when (modeType) {
                ModeType.Charging -> {
                    state
                }

                ModeType.UvscInProgress -> {
                    val value = packet.valueAsString.toIntOrNull() ?: 1000
                    val minutes = value - 1000
                    info.toUvscInProgress(minutes)
                }
            }
        }
    }

    private fun updateUiStateFromAcht(packet: AchtPacket) {
        Timber.i("Received ACHT packet: $packet")
        val value = packet.valueAsString

        _homeUiState.update { state ->
            when (state) {
                is HomeUiState.Charging -> {
                    state.copy(recentUvscTime = value)
                }

                is HomeUiState.UvscInProgress -> {
                    state.copy(recentUvscTime = value)
                }
            }
        }
    }

    private fun updateUiStateFromAchs(packet: AchsPacket) {
        Timber.i("Received ACSH packet: $packet")
        val value = packet.valueAsString
        val (time, result, expected) = value.splitTrimmed().let {
            Triple(
                it.getOrNull(0) ?: "-",
                it.getOrNull(1) ?: "-",
                it.getOrNull(2) ?: "-"
            )
        }

        _homeUiState.update { state ->
            when (state) {
                is HomeUiState.Charging -> state.copy(
                    uvscTime = time,
                    uvscResult = result,
                    expectedTime = expected
                )

                is HomeUiState.UvscInProgress -> state.copy(
                    uvscTime = time,
                    uvscResult = result,
                    expectedTime = expected
                )
            }
        }
    }

    fun toggleCharging(isOff: Boolean) {
        viewModelScope.launch {
            retryUntilReceive(packet = SetChargeMode(isOff = isOff))
        }
    }

    private suspend fun retryUntilReceive(packet: SetChargeMode) {
        val isSuccess = bleRepository.sendToRetry(data = packet)
        _uiEvent.emit(UiEvent.ModeChangedResult(isSuccess = isSuccess))
        if (!isSuccess) {
            Timber.w("BLE response not received for $packet after retry")
        }
    }

    private fun updateUiStateFromAcs(packet: AcsPacket.ModeChange) {
        modeType = packet.modeType

        _homeUiState.update { state ->
            val info = state as? UvscInfo
            when (packet.modeType) {
                ModeType.Charging -> info.toCharging()
                ModeType.UvscInProgress -> info.toUvscInProgress()
            }
        }
    }

    private fun observeHistoryPacket() =
        bleRepository.latestPacketsMap
            .achPackets()
            .onEach { updateUvscHistoryFromPacket(it) }
            .launchIn(viewModelScope)

    private fun updateUvscHistoryFromPacket(packet: AchPacket) {
        Timber.i("Received ACH packet: $packet")
        val parts = packet.valueAsString.splitTrimmed()

        val index = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val date = runCatching { parts.getOrNull(1)?.normalizeDate() }.getOrNull()
        val time = parts.getOrNull(2)?.toIntOrNull()
        val result = parts.getOrNull(3)
        val note = parts.getOrNull(4)

        val entry = UvscHistory(
            index = index,
            date = date,
            time = time,
            result = result,
            note = note
        )

        _uvscHistoryList.update { current ->
            (listOf(entry) + current)
                .distinctBy { it.date to it.time }
                .sortedWith(compareByDescending<UvscHistory> { it.date }.thenByDescending { it.time })
                .take(10)
        }
    }

    private fun observeReceiveData() {
        bleRepository.latestPacketsMap
            .scan(initial = ReceiveScanResult(emptyMap(), null)) { acc, newMap ->
                val changedPacket = newMap.values.find { it != acc.currentMap[it.key] }
                ReceiveScanResult(newMap, changedPacket)
            }
            .mapNotNull { it.changedPacket }
            .filterNot { it is AcsPacket.ModeChange && (it.valueAsString == "100" || it.valueAsString == "200") }
            .onEach { updateReceiveData(it) }
            .launchIn(viewModelScope)
    }

    private fun updateReceiveData(packet: ReceivePacket) {
        _receiveDataList.update { currentList ->
            val existingIndex = currentList.indexOfFirst { it.key == packet.key }

            if (existingIndex != -1) {
                currentList.mapIndexed { index, data ->
                    if (index == existingIndex) {
                        data.copy(value = packet.valueAsString, isLatest = true)
                    } else {
                        if (data.isLatest) data.copy(isLatest = false) else data
                    }
                }
            } else {
                currentList.map { if (it.isLatest) it.copy(isLatest = false) else it } +
                        ReceiveData(
                            key = packet.key,
                            value = packet.valueAsString,
                            isLatest = true,
                            remarks = ""
                        )
            }.sortedByDescending { it.isChecked }
        }
    }

    fun toggleReceiveDataChecked(key: String) {
        _receiveDataList.update { currentList ->
            val toggledItem = currentList.find { it.key == key } ?: return@update currentList
            val isChecking = !toggledItem.isChecked

            currentList
                .map { it.copy(isChecked = (it.key == key) && isChecking) }
                .sortedByDescending { it.isChecked }
        }
    }

    fun navigateHome() = viewModelScope.launch {
        navigator.navigate(
            route = RouteHome,
            saveState = true,
            launchSingleTop = true,
        )
    }

    fun navigateUvscHistory() = viewModelScope.launch {
        navigator.navigate(
            route = RouteUvscHistory,
            saveState = true,
            launchSingleTop = true,
        )
    }

    fun navigateRouteReceiveHistory() = viewModelScope.launch {
        navigator.navigate(
            route = RouteReceiveHistory,
            saveState = true,
            launchSingleTop = true,
        )
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSendPacketClick(packet: String) = viewModelScope.launch {
        val isSuccess = bleRepository.sendDynamicPacket(packet)
        _uiEvent.emit(UiEvent.SendPacketResult(isSuccess))
    }

    fun connectDevice(device: RxBleDevice) {
        bleRepository.connect(device)
    }

    private fun removeData() {
        _homeUiState.value = emptyCharging
        _uvscHistoryList.value = emptyList()
        _receiveDataList.value = emptyList()
    }
}