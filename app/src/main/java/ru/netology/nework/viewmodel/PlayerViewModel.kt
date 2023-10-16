package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nework.observer.MediaLifecycleObserver

class PlayerViewModel : ViewModel() {
//
//    val mediaObserver = MediaLifecycleObserver()
//
//    private val repository = TrackRepository()
//
//    private val _album = MutableLiveData<Album>()
//    val album: LiveData<Album>
//        get() = _album
//
//    private val _tracks = MutableLiveData<List<Track>>()
//    val tracks: LiveData<List<Track>>
//        get() = _tracks
//
//    private var lastPlayedId = -1L
//
//    init {
//        getAlbum()
//        mediaObserver.mediaPlayer?.setOnCompletionListener {
//            playNext()
//        }
//    }
//
//    private fun getAlbum() = viewModelScope.launch {
//        try {
//            _album.value = repository.getAlbum()
//            _tracks.value = _album.value?.tracks
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    fun play(track: Track) {
//        val needResume = lastPlayedId == track.id
//
//        _tracks.value = _tracks.value?.map {
//            if (it.id == track.id) {
//                it.copy(isPlaying = !track.isPlaying)
//            } else {
//                it.copy(isPlaying = false)
//            }
//        }
//
//        mediaObserver.apply {
//            when {
//                track.isPlaying -> mediaPlayer?.pause()
//                needResume -> mediaPlayer?.start()
//                else -> {
//                    mediaPlayer?.reset()
//                    mediaPlayer?.setDataSource(BASE_URL + track.file)
//                    play()
//                    lastPlayedId = track.id
//                }
//            }
//        }
//    }
//
//    private fun playNext() {
//        val tracksOrEmpty = tracks.value.orEmpty()
//
//        if (tracksOrEmpty.isEmpty()) return
//
//        val lastPlayedIndex = tracksOrEmpty.indexOfFirst { it.isPlaying }
//
//        val nextTrackIndex = if (lastPlayedIndex == tracksOrEmpty.lastIndex) 0 else lastPlayedIndex + 1
//
//        val nextTrack = tracksOrEmpty[nextTrackIndex]
//
//        play(nextTrack)
//    }
}
