package platform.presentation

import kotlinx.coroutines.CoroutineScope

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect abstract class ViewModel() {
    val scope: CoroutineScope
    protected open fun onCleared()
}