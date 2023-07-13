package utilities

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class coordsOfSatellites {
    fun calculateOrbit(linea1TLE: String, linea2TLE: String): Orbit {
        val satelliteId = linea1TLE.substring(0, 6)
        val epoch = linea1TLE.substring(6, 14)
        val meanMotion = linea1TLE.substring(14, 24).toDouble()
        val eccentricity = linea1TLE.substring(24, 32).toDouble()
        val inclination = linea1TLE.substring(32, 40).toDouble()
        val rightAscensionOfAscendingNode = linea1TLE.substring(40, 50).toDouble()
        val argumentOfPerihelion = linea1TLE.substring(50, 60).toDouble()
        val meanAnomaly = linea1TLE.substring(60, 70).toDouble()

        return Orbit(
            satelliteId = satelliteId,
            epoch = epoch,
            meanMotion = meanMotion,
            eccentricity = eccentricity,
            inclination = inclination,
            rightAscensionOfAscendingNode = rightAscensionOfAscendingNode,
            argumentOfPerihelion = argumentOfPerihelion,
            meanAnomaly = meanAnomaly,
            coordinates = calculateCoordinates(
                meanMotion,
                eccentricity,
                inclination,
                rightAscensionOfAscendingNode,
                argumentOfPerihelion,
                meanAnomaly
            )
        )
    }

    fun calculateCoordinates(meanMotion: Double, eccentricity: Double, inclination: Double, rightAscensionOfAscendingNode: Double, argumentOfPerihelion: Double, meanAnomaly: Double): List<String> {
        val coordinates = mutableListOf<String>()
        val hours = listOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0)
        val hoursInSeconds = hours.map { it * 60.0 * 60.0 }
        for (time in hoursInSeconds){
            // Calculate the position of the satellite at the given time.
            val position = calculatePosition(meanMotion, eccentricity, inclination, rightAscensionOfAscendingNode, argumentOfPerihelion, meanAnomaly, time)

            // Convert the position to coordinates.
            val coordinatesStr = position.toCoordinates()

            coordinates.add(coordinatesStr)
        }

        return coordinates
    }

    fun calculatePosition(meanMotion: Double, eccentricity: Double, inclination: Double, rightAscensionOfAscendingNode: Double, argumentOfPerihelion: Double, meanAnomaly: Double, time: Double): Position {

        // Calculate the true anomaly.
        val trueAnomaly = meanAnomaly + eccentricity * sin(meanAnomaly)

        // Calculate the radius vector.
        val radiusVector = meanMotion * sqrt(1 - eccentricity * eccentricity) * sin(trueAnomaly)

        // Calculate the x-coordinate.
        val x = radiusVector * cos(trueAnomaly) * cos(rightAscensionOfAscendingNode) * cos(time) - radiusVector * sin(trueAnomaly) * sin(rightAscensionOfAscendingNode) * sin(time)

        // Calculate the y-coordinate.
        val y = radiusVector * cos(trueAnomaly) * sin(rightAscensionOfAscendingNode) * cos(time) + radiusVector * sin(trueAnomaly) * cos(rightAscensionOfAscendingNode) * sin(time)

        // Calculate the z-coordinate.
        val z = radiusVector * sin(trueAnomaly) * cos(inclination)

        println(x, y, z)
        // Return the position.
        return Position(x, y, z)
    }

    private fun println(x: Double, y: Double, z: Double) {
        println("x: $x, y: $y, z: $z")
    }

    fun Position.toCoordinates(): String {

        return listOf(x, y, z).joinToString(",")
    }
    data class Orbit(
        val satelliteId: String,
        val epoch: String,
        val meanMotion: Double,
        val eccentricity: Double,
        val inclination: Double,
        val rightAscensionOfAscendingNode: Double,
        val argumentOfPerihelion: Double,
        val meanAnomaly: Double,
        val coordinates: List<String>
    )

    data class Position(val x: Double, val y: Double, val z: Double)
}