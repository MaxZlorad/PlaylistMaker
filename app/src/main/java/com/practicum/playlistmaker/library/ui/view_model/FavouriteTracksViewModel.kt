package com.practicum.playlistmaker.library.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.FavoriteTracksInteractor
import com.practicum.playlistmaker.library.domain.models.FavoriteTracksState
import kotlinx.coroutines.launch

class FavouriteTracksViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor // ✅ Интерактор для работы с БД
) : ViewModel() {

    // ✅ LiveData для состояния экрана (пусто или есть треки)
    private val _state = MutableLiveData<FavoriteTracksState>()
    val state: LiveData<FavoriteTracksState> = _state

    // ✅ В init загружаем треки сразу при создании ViewModel
    init {
        loadFavoriteTracks()
    }

    // ✅ Метод загрузки избранных треков из БД
    fun loadFavoriteTracks() {
        // Запускаем корутину в viewModelScope
        viewModelScope.launch {
            // Подписываемся на Flow из интерактора
            // Flow автоматически обновляется при изменениях в БД
            favoriteTracksInteractor.getFavoriteTracks()
                .collect { tracks ->
                    // Когда получили треки, обновляем состояние
                    if (tracks.isEmpty()) {
                        // Если список пуст — показываем заглушку
                        _state.value = FavoriteTracksState.Empty
                    } else {
                        // Если есть треки — показываем список
                        _state.value = FavoriteTracksState.Content(tracks)
                    }
                }
        }
    }
}
