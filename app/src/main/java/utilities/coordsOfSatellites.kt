package utilities

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

        val orbit = Orbit(
            satelliteId = satelliteId,
            epoch = epoch,
            meanMotion = meanMotion,
            eccentricity = eccentricity,
            inclination = inclination,
            rightAscensionOfAscendingNode = rightAscensionOfAscendingNode,
            argumentOfPerihelion = argumentOfPerihelion,
            meanAnomaly = meanAnomaly,
            coordinates = calculateCoordinates(meanMotion, eccentricity, inclination, rightAscensionOfAscendingNode, argumentOfPerihelion, meanAnomaly)
        )

        return orbit
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
        /*REVISAR*/
        // Calculate the true anomaly.
        val trueAnomaly = meanAnomaly + eccentricity * Math.sin(meanAnomaly)

        // Calculate the radius vector.
        val radiusVector = meanMotion * Math.sqrt(1 - eccentricity * eccentricity) * Math.sin(trueAnomaly)

        // Calculate the x-coordinate.
        val x = radiusVector * Math.cos(trueAnomaly) * Math.cos(rightAscensionOfAscendingNode)

        // Calculate the y-coordinate.
        val y = radiusVector * Math.cos(trueAnomaly) * Math.sin(rightAscensionOfAscendingNode)

        // Calculate the z-coordinate.
        val z = radiusVector * Math.sin(trueAnomaly) * Math.cos(inclination)

        // Return the position.
        val position = Position(x, y, z)

        return position
    }

    fun Position.toCoordinates(): String {
        val coordinates = listOf(x, y, z).joinToString(",")

        return coordinates
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