package utilities

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class SGP4Satellite(tleLine1: String, tleLine2: String) {
    // Constants
    private val ae: Double = 1.0
    private val ck2: Double = 5.413079E-4
    private val e6a: Double = 1.0E-6
    private val s: Double = 1.012229
    private val xj3: Double = -2.53881E-6

    // TLE Constants
    private val epochYear: Int
    private val epochDay: Double
    private val bstar: Double
    private val inclination: Double
    private val raan: Double
    private val eccentricity: Double
    private val argPerigee: Double
    private val meanAnomaly: Double
    private val meanMotion: Double

    init {
        // Parse TLE lines
        val tle = TLEParser.parse(tleLine1, tleLine2)
        epochYear = tle.epochYear
        epochDay = tle.epochDay
        bstar = tle.bstar
        inclination = tle.inclination
        raan = tle.raan
        eccentricity = tle.eccentricity
        argPerigee = tle.argPerigee
        meanAnomaly = tle.meanAnomaly
        meanMotion = tle.meanMotion
    }

    private fun calculateMeanMotion(n0: Double): Double {
        val a1 = (xke / n0).pow(x2o3)
        val d1 = 0.75 * ck2 * (3 * cos(inclination).pow(2) - 1) / (a1 * a1 * a1 * a1)

        val d2 = 1.5 * ck2 * (5 * cos(inclination).pow(2) - 1) / (a1 * a1 * a1 * a1 * a1 * a1)

        val d3 = 1.875 * ck2 * (cos(inclination).pow(2) - 1) / (a1 * a1 * a1 * a1 * a1 * a1 * a1 * a1)
        return n0 + d1 * meanMotion + d2 * meanMotion * meanMotion + d3 * meanMotion * meanMotion * meanMotion
    }

    private fun calculateMeanAnomaly(n: Double, tSinceEpoch: Double): Double {
        return meanAnomaly + n * tSinceEpoch
    }

    private fun calculateEccentricAnomaly(m: Double): Double {
        var e0 = m
        var e1 = Double.MAX_VALUE
        while (abs(e0 - e1) > e6a) {
            e1 = e0
            e0 = m + eccentricity * sin(e1)
        }
        return e0
    }

    private fun calculateTrueAnomaly(e: Double): Double {
        return 2 * atan2(sqrt(1 + eccentricity) * sin(e / 2), sqrt(1 - eccentricity) * cos(e / 2))
    }

    private fun calculateRadius(a: Double, e: Double, v: Double): Double {
        return a * (1 - e * cos(v))
    }

    private fun calculateInclinationCorrection(): Double {
        return 3 * xj3 * ae * ae * meanMotion / (2 * (ae * meanMotion).pow(2.0))
    }

    fun calculatePositionAndVelocity(tSinceEpoch: Double): String {
        // Constants
        val x2o3 = 2.0 / 3.0
        val xke = 7.43669161E-2

        // Time conversions
        val tsince = tSinceEpoch / 60.0

        // Constants from TLE elements
        val a1 = (xke / meanMotion).pow(x2o3)
        val cosio = cos(inclination)
        val theta2 = cosio * cosio
        val x3thm1 = 3 * theta2 - 1.0
        val eosq = eccentricity * eccentricity
        val betao2 = 1 - eosq
        val betao2_3 = betao2 * betao2 * betao2
        val del1 = 1.5 * ck2 * x3thm1 / betao2_3 * s * s
        val ao = a1 * (1 - del1 * 0.5 - del1 * del1 * 3.0 / 8.0 - del1 * del1 * del1 * 27.0 / 128.0)

        // Propagate mean motion and mean anomaly
        val n = calculateMeanMotion(meanMotion)
        val m = calculateMeanAnomaly(n, tsince)

        // Propagate eccentric anomaly
        val e = calculateEccentricAnomaly(m)

        // Propagate true anomaly
        val v = calculateTrueAnomaly(e)

        // Calculate radius and speed
        val r = calculateRadius(ao, eccentricity, v)
        val cosv = cos(v)
        val rfdot = xke * sqrt(ao / (1 - eccentricity * eccentricity)) * cosv
        val xinck = inclination + calculateInclinationCorrection()

        // Calculate position and velocity in TEME (True Equator Mean Equinox) frame
        val xnode = raan / (2.0 * PI / 1440.0) + xnodeo
        val xinc = xinck + xinclo

        val cosu = cos(xnode)
        val sinu = sin(xnode)
        val sini = sin(xinc)
        val cosr = cos(v + argPerigee)
        val x = r * cosu * cosr - rfdot * sinu * sini
        val y = r * sinu * cosr + rfdot * cosu * sini
        val z = rfdot * sini

        // Calculate position and velocity in ECI (Earth-Centered Inertial) frame
        val xmx = x * cosra - y * sinra
        val xmy = x * sinra + y * cosra
        val ux = xmx * cosgst - z * singst
        val uy = xmx * singst + z * cosgst
        val uz = xmy

        return "${"%.7f".format(ux)} ${"%.7f".format(uy)} ${"%.7f".format(uz)}"
    }

    companion object {
        // Constants
        private const val PI = 3.14159265358979323846

        // TLE Constants
        private const val xke = 7.43669161E-2
        private const val x2o3 = 2.0 / 3.0

        // Additional TLE elements (you may need to obtain these values from the TLE data)
        private const val xnodeo = 0.0 // Right Ascension of Ascending Node (RAD)
        private const val xinclo = 0.0 // Inclination (RAD)
        private const val cosra = 1.0 // Cosine of right ascension
        private const val sinra = 0.0 // Sine of right ascension
        private const val cosgst = 1.0 // Cosine of Greenwich Sidereal Time
        private const val singst = 0.0 // Sine of Greenwich Sidereal Time

        // Simple TLE parser to extract the necessary elements
        private object TLEParser {
            fun parse(line1: String, line2: String): TLE {
                val epochYear = line1.substring(18, 20).trim().toInt()
                val epochDay = line1.substring(20, 32).trim().toDouble()
                val bstar = line1.substring(53, 61).trim().toDouble()
                val inclination = line2.substring(8, 16).trim().toDouble()
                val raan = line2.substring(17, 25).trim().toDouble()
                val eccentricity = "0.${line2.substring(26, 33).trim()}".toDouble()
                val argPerigee = line2.substring(34, 42).trim().toDouble()
                val meanAnomaly = line2.substring(43, 51).trim().toDouble()
                val meanMotion = line2.substring(52, 63).trim().toDouble()
                println("Epoch Year: $epochYear" + "\n" +
                        "Epoch Day: $epochDay" + "\n" +
                        "B*: $bstar" + "\n" +
                        "Inclination: $inclination" + "\n" +
                        "RAAN: $raan" + "\n" +
                        "Eccentricity: $eccentricity" + "\n" +
                        "Arg Perigee: $argPerigee" + "\n" +
                        "Mean Anomaly: $meanAnomaly" + "\n" +
                        "Mean Motion: $meanMotion")
                return TLE(epochYear, epochDay, bstar, inclination, raan, eccentricity, argPerigee, meanAnomaly, meanMotion)
            }
        }

        // Simple data class to hold TLE elements
        private data class TLE(
            val epochYear: Int,
            val epochDay: Double,
            val bstar: Double,
            val inclination: Double,
            val raan: Double,
            val eccentricity: Double,
            val argPerigee: Double,
            val meanAnomaly: Double,
            val meanMotion: Double
        )

        // Simple data classes to hold position and velocity components

    }
}