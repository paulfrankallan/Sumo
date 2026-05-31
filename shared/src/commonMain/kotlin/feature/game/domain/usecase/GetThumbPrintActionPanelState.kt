package feature.game.domain.usecase

import feature.game.presentation.model.ThumbPrintActionPanelState
import feature.game.presentation.model.ThumbPrintState
import feature.stroop.KeyType
import feature.stroop.StroopEngine
import feature.stroop.StroopTest

class GetThumbPrintActionPanelState {
    operator fun invoke(): ThumbPrintActionPanelState {
        val stroopTest = if ((0..1).random() == 0) {
            StroopEngine.generate()
        } else {
            StroopEngine.generate(KeyType.SEMANTIC)
        }
        val thumbSates = listOf(
            getThumbPrintState(
                stroopTest = stroopTest,
                isKey = true,
            ),
            getThumbPrintState(
                stroopTest = stroopTest,
                isDummy = true,
            ),
            getThumbPrintState(
                stroopTest = stroopTest,
            )
        ).shuffled()
        return ThumbPrintActionPanelState(
            listOf(
                thumbSates[0],
                thumbSates[1],
                thumbSates[2]
            )
        )
    }

    private fun getThumbPrintState(
        stroopTest: StroopTest,
        isKey: Boolean = false,
        isDummy: Boolean = false
    ): ThumbPrintState {
        return when {
            isKey -> {
                ThumbPrintState(
                    isKey = isKey,
                    stroopTest = stroopTest,
                    physicalColour = stroopTest.physicalTextColour,
                    semanticColour = stroopTest.semanticTextColour,
                    keyColour = stroopTest.keyColour,
                    fontWeight = stroopTest.fontWeight,
                    textCase = stroopTest.textCase
                )
            }

            isDummy -> {
                ThumbPrintState(
                    isKey = isKey,
                    stroopTest = stroopTest,
                    keyColour = stroopTest.dummyKeyColour,
                    physicalColour = stroopTest.dummyPhysicalTextColour,
                    semanticColour = stroopTest.dummySemanticTextColour,
                    fontWeight = stroopTest.dummyFontWeight,
                    textCase = stroopTest.dummyTextCase
                )
            }

            else -> {
                ThumbPrintState(
                    isKey = isKey,
                    stroopTest = stroopTest,
                    keyColour = stroopTest.randomKeyColour,
                    physicalColour = stroopTest.randomPhysicalTextColour,
                    semanticColour = stroopTest.randomSemanticTextColour,
                    fontWeight = stroopTest.randomFontWeight,
                    textCase = stroopTest.randomTextCase
                )
            }
        }
    }
}