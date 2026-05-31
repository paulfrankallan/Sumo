package platform.presentation

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual abstract class ViewModel {
    actual val scope = MainScope()

    protected actual open fun onCleared() {}

    fun clear() {
        onCleared()
        scope.cancel()
    }
}