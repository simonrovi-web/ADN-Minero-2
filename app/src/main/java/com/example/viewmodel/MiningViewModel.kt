package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MiningSampleData
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MiningUiState(
    val currentDestination: NavDestination = NavDestination.INICIO,
    val isModoFaena: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "all",
    val selectedMineDetail: MineDetail? = null,
    val isPlayingAudio: Boolean = false,
    val audioCurrentSeconds: Int = 64, // 01:04 default like the mockup
    val audioTotalSeconds: Int = 195, // 03:15 default like the mockup
    val audioSpeed: Float = 1.25f,
    val isHandsFree: Boolean = true,
    val isShiftAutoPlay: Boolean = true,
    val isOfflineDownloaded: Boolean = true,
    val activeChapterIndex: Int = 0,
    val followedFaenas: Set<String> = setOf("CEN-7049"),
    val favoritePanels: Set<String> = setOf("p-1", "p-2", "p-3", "p-4"),
    val showSosDialog: Boolean = false,
    val showCoffeeDialog: Boolean = false,
    val userNotification: String? = null,
    val panels: List<MiningPanel> = MiningSampleData.miningPanels,
    val marketQuotes: List<MarketQuote> = MiningSampleData.marketQuotes,
    val seismicRecords: List<SeismicRecord> = MiningSampleData.seismicEvents,
    val weatherSites: List<WeatherSite> = MiningSampleData.weatherSites,
    val audioChapters: List<AudioNewsChapter> = MiningSampleData.audioChapters,
    val shiftInfo: ShiftInfo = ShiftInfo()
)

class MiningViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MiningUiState())
    val uiState: StateFlow<MiningUiState> = _uiState.asStateFlow()

    private var audioJob: Job? = null

    fun setDestination(dest: NavDestination) {
        _uiState.update { it.copy(currentDestination = dest) }
    }

    fun toggleModoFaena() {
        _uiState.update { it.copy(isModoFaena = !it.isModoFaena) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun openMineDetail(mine: MineDetail = MiningSampleData.mineraCentinelaDetail) {
        _uiState.update { it.copy(selectedMineDetail = mine) }
    }

    fun closeMineDetail() {
        _uiState.update { it.copy(selectedMineDetail = null) }
    }

    fun toggleFollowFaena(id: String) {
        _uiState.update { state ->
            val updated = state.followedFaenas.toMutableSet()
            if (updated.contains(id)) updated.remove(id) else updated.add(id)
            state.copy(
                followedFaenas = updated,
                userNotification = if (updated.contains(id)) "Faena añadida a seguimiento prioritario" else "Faena removida de seguimiento"
            )
        }
    }

    fun toggleFavoritePanel(id: String) {
        _uiState.update { state ->
            val updated = state.favoritePanels.toMutableSet()
            if (updated.contains(id)) updated.remove(id) else updated.add(id)
            state.copy(
                favoritePanels = updated,
                userNotification = if (updated.contains(id)) "Panel guardado en favoritos" else "Panel eliminado de favoritos"
            )
        }
    }

    fun toggleAudioPlayback() {
        val willPlay = !_uiState.value.isPlayingAudio
        _uiState.update { it.copy(isPlayingAudio = willPlay) }
        if (willPlay) {
            startAudioTicker()
        } else {
            audioJob?.cancel()
        }
    }

    private fun startAudioTicker() {
        audioJob?.cancel()
        audioJob = viewModelScope.launch {
            while (_uiState.value.isPlayingAudio && _uiState.value.audioCurrentSeconds < _uiState.value.audioTotalSeconds) {
                delay((1000 / _uiState.value.audioSpeed).toLong())
                _uiState.update { state ->
                    val nextSec = state.audioCurrentSeconds + 1
                    if (nextSec >= state.audioTotalSeconds) {
                        state.copy(audioCurrentSeconds = state.audioTotalSeconds, isPlayingAudio = false)
                    } else {
                        // Check if active chapter changed
                        val newChapterIdx = state.audioChapters.indexOfLast { it.timeSeconds <= nextSec }.coerceAtLeast(0)
                        state.copy(audioCurrentSeconds = nextSec, activeChapterIndex = newChapterIdx)
                    }
                }
            }
        }
    }

    fun seekAudioTo(seconds: Int) {
        val clamped = seconds.coerceIn(0, _uiState.value.audioTotalSeconds)
        val chapterIdx = _uiState.value.audioChapters.indexOfLast { it.timeSeconds <= clamped }.coerceAtLeast(0)
        _uiState.update { it.copy(audioCurrentSeconds = clamped, activeChapterIndex = chapterIdx) }
    }

    fun rewind15() {
        seekAudioTo(_uiState.value.audioCurrentSeconds - 15)
    }

    fun forward30() {
        seekAudioTo(_uiState.value.audioCurrentSeconds + 30)
    }

    fun setAudioSpeed(speed: Float) {
        _uiState.update { it.copy(audioSpeed = speed) }
        if (_uiState.value.isPlayingAudio) {
            startAudioTicker()
        }
    }

    fun toggleHandsFree() {
        _uiState.update { it.copy(isHandsFree = !it.isHandsFree) }
    }

    fun toggleShiftAutoPlay() {
        _uiState.update { it.copy(isShiftAutoPlay = !it.isShiftAutoPlay) }
    }

    fun refreshOfflineDownload() {
        viewModelScope.launch {
            _uiState.update { it.copy(userNotification = "Sincronizando 4.8 MB para operación rajo...") }
            delay(1200)
            _uiState.update { it.copy(isOfflineDownloaded = true, userNotification = "Descarga local offline completada ✓") }
        }
    }

    fun selectChapter(index: Int) {
        val chapter = _uiState.value.audioChapters.getOrNull(index) ?: return
        _uiState.update {
            it.copy(
                activeChapterIndex = index,
                audioCurrentSeconds = chapter.timeSeconds
            )
        }
        if (!_uiState.value.isPlayingAudio) {
            toggleAudioPlayback()
        }
    }

    fun openSosDialog() {
        _uiState.update { it.copy(showSosDialog = true) }
    }

    fun closeSosDialog() {
        _uiState.update { it.copy(showSosDialog = false) }
    }

    fun openCoffeeDialog() {
        _uiState.update { it.copy(showCoffeeDialog = true) }
    }

    fun closeCoffeeDialog() {
        _uiState.update { it.copy(showCoffeeDialog = false) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }

    fun formatSeconds(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }
}
