import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.support.expected
import assertk.assertions.support.show
import feature.game.domain.usecase.GetThumbPrintActionPanelState
import feature.game.presentation.model.ThumbPrintActionPanelState
import kotlin.test.Test

class GetThumbPrintActionPanelStateTest {

    @Test
    fun `Should return thumb print action panel state`() {
        // Given
        val getThumbPrintActionPanelState = GetThumbPrintActionPanelState()

        // When
        val actual = getThumbPrintActionPanelState()

        // Then
        assertThat(actual.thumbPrintStates.size).isEqualTo(3)

        assertThat(actual).hasThreeThumbPrintStateValues()
    }
}

fun Assert<ThumbPrintActionPanelState>.hasThreeThumbPrintStateValues() = given { actual ->
    if (actual.thumbPrintStates.size == 3) return
    expected("to be 3 but was:${show(actual)}")
}

// Rules
// If the StimuliGroup is CONGRUENT then the keyColour should be
// different from the physicalTextColour and semanticTextColour
// If the StimuliGroup is INCONGRUENT then the keyColour should be
// different from the physicalTextColour and the same as semanticTextColour
// If the StimuliGroup is CONGRUENT then the semanticTextColour should be different
// from the original semanticTextColour and physicalTextColour
// If the StimuliGroup is INCONGRUENT then the physicalColour should be different
// The physicalColour must always be different from the original physicalColour