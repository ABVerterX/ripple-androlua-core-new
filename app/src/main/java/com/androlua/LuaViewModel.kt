package com.androlua

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class LuaViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val data = mapOf<String, Any?>()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LuaViewModel(createSavedStateHandle())
            }
        }
    }
}