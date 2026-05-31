package feature.stroop

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

class StroopEngine {
    companion object {
        private val colors: List<StimuliColour> = StimuliColour.entries
        private val fontWeights: List<FontWeight> = listOf(FontWeight.Normal, FontWeight.Bold)
        private val textCases: List<TextCase> = listOf(TextCase.UPPER, TextCase.LOWER)

        fun generate(keyType: KeyType = KeyType.PHYSICAL): StroopTest {
            // 1. Pick a random StimuliGroup with an 80% bias towards INCONGRUENT
            val stimuliGroup = if ((1..100).random() <= 80) {
                StimuliGroup.INCONGRUENT
            } else {
                StimuliGroup.CONGRUENT
            }

            // 2. Pick a random physical label colour
            val physicalTextColour = colors.random()

            // 3. If StimuliGroup is CONGRUENT, pick the same semanticTextColour as the physicalTextColour
            // 4. If StimuliGroup is INCONGRUENT, pick a different semanticTextColour from the physicalTextColour
            val semanticTextColour = if (stimuliGroup == StimuliGroup.CONGRUENT) {
                physicalTextColour
            } else {
                colors.filterNot { it == physicalTextColour }.random()
            }

            // 5. Pick a random background colour that is different from the physicalTextColour
            val backgroundColor =
                Color.Black//colors.filterNot { it == physicalTextColour }.random()

            // Randomly select mutually exclusive icon colors
            val unusedColours = colors.filterNot {
                it == physicalTextColour || it == semanticTextColour
            }.shuffled()

            val dummyPhysicalTextColour = unusedColours.filterNot { it == physicalTextColour }.random()

            val dummyKeyColour = if (keyType == KeyType.SEMANTIC) {
                unusedColours.filterNot { it == dummyPhysicalTextColour }.random()
            } else {
                physicalTextColour
            }

            val dummySemanticTextColour = if (keyType == KeyType.SEMANTIC) {
                unusedColours.filterNot { it == dummyKeyColour }.random()
            } else {
                physicalTextColour
            }

            val randomPhysicalTextColour = getRandomPhysicalTextColour(
                dummyPhysicalTextColour = dummyPhysicalTextColour,
                dummySemanticTextColour = dummySemanticTextColour,
                unusedColours
            )

            val randomSemanticTextColour = getRandomSemanticTextColour(
                dummyPhysicalTextColour = dummyPhysicalTextColour,
                dummySemanticTextColour = dummySemanticTextColour,
                unusedColours
            )

            val randomKeyColour = if (
                randomPhysicalTextColour == randomSemanticTextColour || keyType == KeyType.SEMANTIC
            ) {
                unusedColours.filterNot {
                    it == randomPhysicalTextColour ||
                            it == randomSemanticTextColour
                }.random()
            } else {
                randomSemanticTextColour
            }

            val keyColour = if (keyType == KeyType.PHYSICAL) {
                physicalTextColour
            } else {
                semanticTextColour
            }

            return StroopTest(
                keyColour = keyColour,
                semanticTextColour = semanticTextColour,
                physicalTextColour = physicalTextColour,
                backgroundColor = backgroundColor,
                fontWeight = fontWeights.random(),
                textCase = textCases.random(),
                stimuliGroup = stimuliGroup,
                dummyKeyColour = dummyKeyColour,
                dummySemanticTextColour = dummySemanticTextColour,
                dummyPhysicalTextColour = dummyPhysicalTextColour,
                dummyFontWeight = fontWeights.random(),
                dummyTextCase = textCases.random(),
                randomKeyColour = randomKeyColour,
                randomSemanticTextColour = randomSemanticTextColour,
                randomPhysicalTextColour = randomPhysicalTextColour,
                randomFontWeight = fontWeights.random(),
                randomTextCase = textCases.random(),
            )
        }
    }
}

fun getRandomPhysicalTextColour(
    dummyPhysicalTextColour: StimuliColour,
    dummySemanticTextColour: StimuliColour,
    unusedColours: List<StimuliColour>
): StimuliColour {
    return unusedColours.filterNot {
        it == dummyPhysicalTextColour ||
                it == dummySemanticTextColour
    }.random()
}

fun getRandomSemanticTextColour(
    dummyPhysicalTextColour: StimuliColour,
    dummySemanticTextColour: StimuliColour,
    unusedColours: List<StimuliColour>
): StimuliColour {
    return unusedColours.filterNot {
        it == dummyPhysicalTextColour ||
                it == dummySemanticTextColour
    }.random()
}

enum class KeyType {
    PHYSICAL,
    SEMANTIC,
}

enum class StimuliGroup {
    CONGRUENT,
    INCONGRUENT,
}

enum class StimuliColour(val label: String, val colour: Color) {
    RED("Red", Color.Red),
    GREEN("Green", Color.Green),
    BLUE("Blue", Color.Blue),
    YELLOW("Yellow", Color.Yellow),
    PURPLE("Purple", Color(0xFF800080)),
    ORANGE("Orange", Color(0xFFFFA500)),
    WHITE("White", Color.White),
    BROWN("Brown", Color(0xFF8B4513)),
    PINK("Pink", Color(0xFFFF1694)),
}

data class StroopTest(
    val keyColour: StimuliColour,
    val semanticTextColour: StimuliColour,
    val physicalTextColour: StimuliColour,
    val backgroundColor: Color,
    val stimuliGroup: StimuliGroup,
    val fontWeight: FontWeight,
    val textCase: TextCase,
    val dummyKeyColour: StimuliColour,
    val dummySemanticTextColour: StimuliColour,
    val dummyPhysicalTextColour: StimuliColour,
    val dummyFontWeight: FontWeight,
    val dummyTextCase: TextCase,
    val randomKeyColour: StimuliColour,
    val randomSemanticTextColour: StimuliColour,
    val randomPhysicalTextColour: StimuliColour,
    val randomFontWeight: FontWeight,
    val randomTextCase: TextCase,
)

enum class TextCase {
    UPPER,
    LOWER
}