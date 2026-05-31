package feature.stroop

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import kotlin.test.Test

class StroopEngineTest {

    @Test
    fun `KeyType PHYSICAL - StimuliGroup CONGRUENT - only 1 keyColour == semanticTextColour`() {
        repeat(10000) {
            // Given
            var stroopTest: StroopTest
            do {
                stroopTest = StroopEngine.generate(KeyType.PHYSICAL)
            } while (stroopTest.stimuliGroup != StimuliGroup.CONGRUENT)

            // When
            val keyColour = stroopTest.keyColour
            val semanticTextColour = stroopTest.semanticTextColour
            val physicalTextColour = stroopTest.physicalTextColour

            val dummyKeyColour = stroopTest.dummyKeyColour
            val dummyPhysicalTextColour = stroopTest.dummyPhysicalTextColour
            val dummySemanticTextColour = stroopTest.dummySemanticTextColour

            val randomKeyColour = stroopTest.randomKeyColour
            val randomPhysicalTextColour = stroopTest.randomPhysicalTextColour
            val randomSemanticTextColour = stroopTest.randomSemanticTextColour

            // Then
            assertThat(keyColour).isEqualTo(semanticTextColour)
            assertThat(keyColour).isEqualTo(physicalTextColour)

            assertThat(dummyKeyColour).isNotEqualTo(dummyPhysicalTextColour)
            assertThat(dummyKeyColour).isEqualTo(dummySemanticTextColour)

            assertThat(randomKeyColour).isNotEqualTo(randomPhysicalTextColour)
            assertThat(randomKeyColour).isNotEqualTo(randomSemanticTextColour)
        }
    }

    @Test
    fun `KeyType SEMANTIC - StimuliGroup CONGRUENT - only 1 keyColour == semanticTextColour`() {
        repeat(10000) {
            // Given
            var stroopTest: StroopTest
            do {
                stroopTest = StroopEngine.generate(KeyType.SEMANTIC)
            } while (stroopTest.stimuliGroup != StimuliGroup.CONGRUENT)

            // When
            val keyColour = stroopTest.keyColour
            val semanticTextColour = stroopTest.semanticTextColour
            val physicalTextColour = stroopTest.physicalTextColour

            val dummyKeyColour = stroopTest.dummyKeyColour
            val dummyPhysicalTextColour = stroopTest.dummyPhysicalTextColour
            val dummySemanticTextColour = stroopTest.dummySemanticTextColour

            val randomKeyColour = stroopTest.randomKeyColour
            val randomPhysicalTextColour = stroopTest.randomPhysicalTextColour
            val randomSemanticTextColour = stroopTest.randomSemanticTextColour

            // Then
            assertThat(keyColour).isEqualTo(semanticTextColour)
            assertThat(keyColour).isEqualTo(physicalTextColour)

            assertThat(dummyKeyColour).isNotEqualTo(dummyPhysicalTextColour)
            assertThat(dummyKeyColour).isNotEqualTo(dummySemanticTextColour)

            assertThat(randomKeyColour).isNotEqualTo(randomPhysicalTextColour)
            assertThat(randomKeyColour).isNotEqualTo(randomSemanticTextColour)
        }
    }

    @Test
    fun `KeyType SEMANTIC - StimuliGroup INCONGRUENT - only 1 keyColour == semanticTextColour`() {
        repeat(10000) {
            // Given
            var stroopTest: StroopTest
            do {
                stroopTest = StroopEngine.generate(KeyType.SEMANTIC)
            } while (stroopTest.stimuliGroup != StimuliGroup.INCONGRUENT)

            // When
            val keyColour = stroopTest.keyColour
            val semanticTextColour = stroopTest.semanticTextColour
            val physicalTextColour = stroopTest.physicalTextColour

            val dummyKeyColour = stroopTest.dummyKeyColour
            val dummyPhysicalTextColour = stroopTest.dummyPhysicalTextColour
            val dummySemanticTextColour = stroopTest.dummySemanticTextColour

            val randomKeyColour = stroopTest.randomKeyColour
            val randomPhysicalTextColour = stroopTest.randomPhysicalTextColour
            val randomSemanticTextColour = stroopTest.randomSemanticTextColour

            // Then
            assertThat(keyColour).isEqualTo(semanticTextColour)
            assertThat(keyColour).isNotEqualTo(physicalTextColour)

            assertThat(dummyKeyColour).isNotEqualTo(dummyPhysicalTextColour)
            assertThat(dummyKeyColour).isNotEqualTo(dummySemanticTextColour)

            assertThat(randomKeyColour).isNotEqualTo(randomPhysicalTextColour)
            assertThat(randomKeyColour).isNotEqualTo(randomSemanticTextColour)
        }
    }

    @Test
    fun `KeyType SEMANTIC - StimuliGroup INCONGRUENT - keyColour == semanticTextColour`() {
        repeat(10000) {
            // Given
            var stroopTest: StroopTest
            do {
                stroopTest = StroopEngine.generate(KeyType.SEMANTIC)
            } while (stroopTest.stimuliGroup != StimuliGroup.INCONGRUENT)

            // When
            val keyColour = stroopTest.keyColour
            val semanticTextColour = stroopTest.semanticTextColour
            val physicalTextColour = stroopTest.physicalTextColour

            // Then
            assertThat(keyColour).isEqualTo(semanticTextColour)
            assertThat(keyColour).isNotEqualTo(physicalTextColour)
        }
    }

    @Test
    fun `KeyType PHYSICAL - StimuliGroup INCONGRUENT - keyColour == physicalTextColour`() {
        repeat(10000) {
            // Given
            var stroopTest: StroopTest
            do {
                stroopTest = StroopEngine.generate(KeyType.PHYSICAL)
            } while (stroopTest.stimuliGroup != StimuliGroup.INCONGRUENT)

            // When
            val keyColour = stroopTest.keyColour
            val semanticTextColour = stroopTest.semanticTextColour
            val physicalTextColour = stroopTest.physicalTextColour

            // Then
            assertThat(keyColour).isNotEqualTo(semanticTextColour)
            assertThat(keyColour).isEqualTo(physicalTextColour)
        }
    }

    @Test
    fun `StimuliGroup CONGRUENT - Test random Stroop values`() {
        repeat(10000) {
            // Given
            var stroopTest: StroopTest
            do {
                stroopTest = StroopEngine.generate()
            } while (stroopTest.stimuliGroup != StimuliGroup.CONGRUENT)

            // When
            val randomPhysicalTextColour = stroopTest.randomPhysicalTextColour
            val randomKeyColour = stroopTest.randomKeyColour

            // Then
            assertThat(randomKeyColour).isNotEqualTo(randomPhysicalTextColour)
        }
    }

    @Test
    fun `StimuliGroup INCONGRUENT - Test random Stroop values`() {
        repeat(10000) {
            // Given
            var stroopTest: StroopTest
            do {
                stroopTest = StroopEngine.generate()
            } while (stroopTest.stimuliGroup != StimuliGroup.INCONGRUENT)

            // When
            val randomPhysicalTextColour = stroopTest.randomPhysicalTextColour
            val randomKeyColour = stroopTest.randomKeyColour

            // Then
            assertThat(randomKeyColour).isNotEqualTo(randomPhysicalTextColour)
        }
    }

    //--

    @Test
    fun `StimuliGroup CONGRUENT - Test dummy Stroop values`() {
        repeat(10000) {
            // Given
            var stroopTest: StroopTest
            do {
                stroopTest = StroopEngine.generate()
            } while (stroopTest.stimuliGroup != StimuliGroup.CONGRUENT)

            // When
            val dummyPhysicalTextColour = stroopTest.dummyPhysicalTextColour
            val dummySemanticTextColour = stroopTest.dummySemanticTextColour
            val dummyKeyColour = stroopTest.dummyKeyColour

            val keyColour = stroopTest.keyColour
            val physicalTextColour = stroopTest.physicalTextColour

            // Then
            assertThat(dummyKeyColour).isNotEqualTo(dummyPhysicalTextColour)
            assertThat(dummyKeyColour).isEqualTo(dummySemanticTextColour)
            assertThat(dummyPhysicalTextColour).isNotEqualTo(physicalTextColour)
            assertThat(dummyKeyColour).isEqualTo(keyColour)
            assertThat(dummySemanticTextColour).isEqualTo(physicalTextColour)
        }
    }

    @Test
    fun `StimuliGroup INCONGRUENT - Test dummy Stroop values`() {
        repeat(10000) {
            // Given
            var stroopTest: StroopTest
            do {
                stroopTest = StroopEngine.generate()
            } while (stroopTest.stimuliGroup != StimuliGroup.INCONGRUENT)

            // When
            val dummyPhysicalTextColour = stroopTest.dummyPhysicalTextColour
            val dummySemanticTextColour = stroopTest.dummySemanticTextColour
            val dummyKeyColour = stroopTest.dummyKeyColour

            val keyColour = stroopTest.keyColour
            val physicalTextColour = stroopTest.physicalTextColour

            // Then
            assertThat(dummyKeyColour).isNotEqualTo(dummyPhysicalTextColour)
            assertThat(dummyKeyColour).isEqualTo(dummySemanticTextColour)
            assertThat(dummyPhysicalTextColour).isNotEqualTo(physicalTextColour)
            assertThat(dummyKeyColour).isEqualTo(keyColour)
            assertThat(dummySemanticTextColour).isEqualTo(physicalTextColour)
        }
    }

    @Test
    fun `StimuliGroup INCONGRUENT - keyColour != semanticTextColour AND keyColour == physicalTextColour`() {

        // Given
        var stroopTest: StroopTest
        do {
            stroopTest = StroopEngine.generate()
        } while (stroopTest.stimuliGroup != StimuliGroup.INCONGRUENT)

        // When
        val keyColour = stroopTest.keyColour
        val physicalTextColour = stroopTest.physicalTextColour
        val semanticTextColour = stroopTest.semanticTextColour

        // Then
        assertThat(physicalTextColour).isNotEqualTo(semanticTextColour)
        assertThat(keyColour).isNotEqualTo(semanticTextColour)
        assertThat(keyColour).isEqualTo(physicalTextColour)
    }

    @Test
    fun `StimuliGroup CONGRUENT should have the same keyColour and semanticTextColour`() {
        // Given
        var stroopTest: StroopTest
        do {
            stroopTest = StroopEngine.generate()
        } while (stroopTest.stimuliGroup != StimuliGroup.CONGRUENT)

        // When
        val keyColour = stroopTest.keyColour
        val physicalTextColour = stroopTest.physicalTextColour
        val semanticTextColour = stroopTest.semanticTextColour

        // Then
        assertThat(physicalTextColour).isEqualTo(semanticTextColour)
        assertThat(keyColour).isEqualTo(physicalTextColour)
        assertThat(keyColour).isEqualTo(semanticTextColour)
    }

    @Test
    fun `StimuliGroup CONGRUENT should have the same physicalTextColour and semanticTextColour`() {

        // Given
        var stroopTest: StroopTest
        do {
            stroopTest = StroopEngine.generate()
        } while (stroopTest.stimuliGroup != StimuliGroup.CONGRUENT)

        // When
        val physicalTextColour = stroopTest.physicalTextColour
        val semanticTextColour = stroopTest.semanticTextColour

        // Then
        assertThat(physicalTextColour).isEqualTo(semanticTextColour)
    }

    @Test
    fun `StimuliGroup INCONGRUENT should have different physicalTextColour and semanticTextColour`() {

        // Given
        var stroopTest: StroopTest
        do {
            stroopTest = StroopEngine.generate()
        } while (stroopTest.stimuliGroup != StimuliGroup.INCONGRUENT)

        // When
        val physicalTextColour = stroopTest.physicalTextColour
        val semanticTextColour = stroopTest.semanticTextColour

        // Then
        assertThat(physicalTextColour).isNotEqualTo(semanticTextColour)
    }
}