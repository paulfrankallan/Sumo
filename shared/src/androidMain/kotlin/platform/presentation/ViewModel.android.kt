package platform.presentation

import kotlinx.coroutines.CoroutineScope
import androidx.lifecycle.ViewModel as AndroidXViewModel
import androidx.lifecycle.viewModelScope as androidXViewModelScope

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual abstract class ViewModel actual constructor() : AndroidXViewModel() {
    actual val scope: CoroutineScope = androidXViewModelScope

    actual override fun onCleared() {
        super.onCleared()
    }
}
