package sscp.satellite_space_collision_prevision

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal const val SPLASHSCREEN_SHOW_TIME = 4000L
class SplashScreen : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SPLASHSCREEN_SHOW_TIME)
            _isLoading.value = false
        }
    }
}