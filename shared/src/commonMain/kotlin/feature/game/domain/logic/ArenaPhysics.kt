package feature.game.domain.logic

import androidx.compose.ui.geometry.Offset
import kotlin.math.pow
import kotlin.math.sqrt

object ArenaPhysics {
    fun isOutOfBounds(
        spotPosition: Offset,
        circleCenter: Offset,
        circleRadius: Float,
        spotRadiusPx: Float
    ): Boolean {
        // Calculate the center of the spot based on its top-left position and radius
        val spotCenter = Offset(spotPosition.x + spotRadiusPx, spotPosition.y + spotRadiusPx)
        // Calculate the distance between the center of the spot and the center of the arena
        val distance = sqrt(
            (spotCenter.x - circleCenter.x).pow(2) + (spotCenter.y - circleCenter.y).pow(2)
        )
        // Return true if the spot is touching or overlapping the arena circle
        val isOutOfBounds = distance + spotRadiusPx >= circleRadius
        return isOutOfBounds
    }

    fun doThumbSpotsOverlap(
        firstThumbCenter: Offset,
        secondThumbCenter: Offset,
        firstThumbRadiusPx: Float,
        secondThumbRadiusPx: Float
    ): Boolean {
        val distanceBetweenCenters = sqrt(
            (firstThumbCenter.x - secondThumbCenter.x).pow(2) +
                    (firstThumbCenter.y - secondThumbCenter.y).pow(2)
        )
        val sumOfRadii = firstThumbRadiusPx + secondThumbRadiusPx
        return distanceBetweenCenters <= sumOfRadii
    }

    fun calculatePushVector(
        movingSpotCenter: Offset,
        stationarySpotCenter: Offset,
        dragAmount: Offset,
    ): Offset {
        val direction = stationarySpotCenter - movingSpotCenter
        // Normalize the vector
        val normalizedDirection = direction / direction.getDistance()
        // Calculate the push vector without limiting the distance
        val magnitude = dragAmount.getDistance()
        return normalizedDirection * magnitude
    }
}