package com.example.busdriverpanel.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.busdriverpanel.data.repository.BusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val busId: String = "",
    val driverName: String = "",
    val errorMessage: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = BusRepository(application)
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        if (repository.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(
                isLoggedIn = true,
                busId = repository.getBusId() ?: "",
                driverName = repository.getDriverName() ?: ""
            )
        }
    }
    
    fun login(busId: String, driverName: String) {
        if (busId.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Bus ID is required")
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            repository.login(busId.trim(), driverName.trim())
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        busId = busId.trim(),
                        driverName = driverName.trim()
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed"
                    )
                }
        }
    }
    
    fun logout() {
        repository.logout()
        _uiState.value = LoginUiState()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun updateBusId(busId: String) {
        _uiState.value = _uiState.value.copy(busId = busId)
    }
    
    fun updateDriverName(driverName: String) {
        _uiState.value = _uiState.value.copy(driverName = driverName)
    }
}
