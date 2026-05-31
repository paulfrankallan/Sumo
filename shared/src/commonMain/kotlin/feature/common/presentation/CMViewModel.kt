package feature.common.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.presentation.ViewModel

abstract class CMViewModel<V : ViewState, I : Intent> : ViewModel() {

    abstract fun initialViewState(): V
    abstract fun onIntent(intent: I)
    abstract fun onNavigationComplete(navigationEvent: NavigationEvent)
    abstract fun onEventComplete(eventId: String)

    @Suppress("PropertyName")
    protected val _state by lazy {
        MutableStateFlow(initialViewState())
    }

    val state by lazy {
        _state.asStateFlow()
    }
}
