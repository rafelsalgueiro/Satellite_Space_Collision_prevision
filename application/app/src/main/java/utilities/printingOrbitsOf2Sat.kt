package utilities

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.*

private var primaryCoordinates: String = ""

@RequiresApi(Build.VERSION_CODES.O)
fun createKMLFile1(sat1: String, sat2: String): String {
    val kmlTemplate = "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
            "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
            " xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n" +
            "    <Document>\n" +
            "        <name>Satellite Trajectories</name>\n" +
            "        \n" +
            " <open>1</open>\n" +
            "        <Style id=\"satellite1\">\n" +
            "            <LineStyle>\n" +
            "                <color>ff0000ff</color>\n" +
            "                <width>2</width>\n" +
            "            </LineStyle>\n" +
            "        </Style>\n" +
            "        \n" +
            "        <Style id=\"satellite2\">\n" +
            "            <LineStyle>\n" +
            "                <color>ff00ff00</color>\n" +
            "                <width>2</width>\n" +
            "            </LineStyle>\n" +
            "        </Style>\n" +
            "        <Placemark>\n" +
            "            <name>Satellite 1</name>\n" +
            "            <styleUrl>#satellite1</styleUrl>\n" +
            "            <LineString>\n" +
            "                <extrude>1</extrude>\n" +
            "                <tessellate>1</tessellate>\n" +
            "                <altitudeMode>absolute</altitudeMode>\n" +
            "                <coordinates>%satellite1Coordinates%</coordinates>\n" +
            "            </LineString>\n" +
            "        </Placemark>\n" +
            "        \n" +
            "        <Placemark>\n" +
            "            <name>Satellite 2</name>\n" +
            "            <styleUrl>#satellite2</styleUrl>\n" +
            "            <LineString>\n" +
            "                <extrude>1</extrude>\n" +
            "                <tessellate>1</tessellate>\n" +
            "                <altitudeMode>absolute</altitudeMode>\n" +
            "                <coordinates>%satellite2Coordinates%</coordinates>\n" +
            "            </LineString>\n" +
            "        </Placemark>\n" +
            "    </Document>\n" +
            "</kml>"

    val satellite1Coordinates = callToServer.getCoordinates(sat1)
    primaryCoordinates = satellite1Coordinates
    val satellite2Coordinates = callToServer.getCoordinates(sat2)


    val sat1 = satellite1Coordinates.replace("\",\"", "\n").split("\n")
    val sat2 = satellite2Coordinates.replace("\",\"", "\n").split("\n")
    var closestDistance = Double.MAX_VALUE
    var midpointX = 0.0
    var midpointY = 0.0
    var finalX = Double.MAX_VALUE
    var finalY = Double.MAX_VALUE
    var finalZ = Double.MAX_VALUE
    for (pos1 in sat1) {
        for (pos2 in sat2) {
            val positions1 = pos1.split(",")
            val positions2 = pos2.split(",")


            val x1 = positions1[0].toDouble()
            val y1 = positions1[1].toDouble()
            val x2 = positions2[0].toDouble()
            val y2 = positions2[1].toDouble()

            val distance = sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))

            midpointX = (x1 + x2) / 2
            midpointY = (y1 + y2) / 2

            if (distance < closestDistance) {
                closestDistance = distance
                finalX = midpointX
                finalY = midpointY
                finalZ = positions1[2].toDouble()
            }
        }
    }

    SSHConnection.flyto("${finalX},${finalY},0")
    val circleCoordinates = generateCircleCoordinates(finalX, finalY, finalZ)

    SSHConnection.printCollision(circleCoordinates)

    getTour(satellite1Coordinates)

    return kmlTemplate.replace(
        "%satellite1Coordinates%",
        satellite1Coordinates.replace("\",\"", "\n")
    )
        .replace("%satellite2Coordinates%", satellite2Coordinates.replace("\",\"", "\n"))
}

fun backButton() {
    val coords = primaryCoordinates.split("\n")
    SSHConnection.flyto(coords[0])
}

fun nextButton() {
    val coords = primaryCoordinates.split("\n")
    SSHConnection.flyto(coords[coords.size - 1])
}

fun getTour(coords: String) {
    val coords = coords.split("\n")
    val coord = coords[0].split(",")
    val alt = coord[2].toDouble() + 500000
    var kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
            "  <Document>\n" +
            "    <name>Tour</name>\n" +
            "    <open>1</open>\n" +
            "    <Folder>\n" +
            "    <gx:Tour>\n" +
            "      <name>Satellitetour</name>\n" +
            "      <gx:Playlist>\n" +
            "<gx:FlyTo>\n" +
            "    <gx:duration>2</gx:duration>\n" +
            "    <gx:flyToMode>smooth</gx:flyToMode>\n" +
            "     <LookAt>\n" +
            "      <longitude>${coord[0]}</longitude>\n" +
            "      <latitude>${coord[1]}</latitude>\n" +
            "      <altitude>${alt}</altitude>\n" +
            "      <heading>0</heading>\n" +
            "      <tilt>30</tilt>\n" +
            "      <range>10000000</range>\n" +
            "      <gx:altitudeMode>absolute</gx:altitudeMode>\n" +
            "      </LookAt>\n" +
            "    </gx:FlyTo>\n"


    for (coord in coords) {
        val coord = coord.split(",")
        val alt = coord[2].toDouble() + 500000

        kml = kml + "<gx:FlyTo>\n" +
                "    <gx:duration>0.5</gx:duration>\n" +
                "    <gx:flyToMode>smooth</gx:flyToMode>\n" +
                "     <LookAt>\n" +
                "      <longitude>${coord[0]}</longitude>\n" +
                "      <latitude>${coord[1]}</latitude>\n" +
                "      <altitude>${alt}</altitude>\n" +
                "      <heading>0</heading>\n" +
                "      <tilt>30</tilt>\n" +
                "      <range>10000000</range>\n" +
                "      <gx:altitudeMode>absolute</gx:altitudeMode>\n" +
                "      </LookAt>\n" +
                "    </gx:FlyTo>\n"
    }

    kml = kml + " </gx:Playlist>\n" +
            "    </gx:Tour>\n" +
            " </Folder>\n" +
            "  </Document>\n" +
            "</kml>"

    val command = "chmod 777 /var/www/html/tourSat.kml; echo '${kml}' > /var/www/html/tourSat.kml"
    SSHConnection.executeCommand(command)
    val command2 = "chmod 777 /var/www/html/kmls.txt; echo '" +
            "http://lg1:81/tourSat.kml" +
            "' >> /var/www/html/kmls.txt"
    SSHConnection.executeCommand(command2)
}

fun generateCircleCoordinates(
    centerX: Double,
    centerY: Double,
    centerZ: Double
): String {
    val coordinates = StringBuilder()
    val radius1 = 1.0
    val radius2 = 0.75

    val points = (0..180).step(10).flatMap { i ->
        (0..360).step(10).map { j ->
            val x = centerX + radius1 * cos(Math.toRadians(i.toDouble())) * sin(
                Math.toRadians(
                    j.toDouble()
                )
            )
            val y = centerY + radius2 * sin(Math.toRadians(i.toDouble())) * sin(
                Math.toRadians(
                    j.toDouble()
                )
            )
            val z = centerZ + radius1 * 100000 * cos(Math.toRadians(j.toDouble()))
            coordinates.append("$x,$y,$z\n")
        }
    }
    return coordinates.toString()
}


suspend fun orbitsOfSomeSat(satellites: Array<String>, context: Context) {

    var kml = "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
            "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
            " xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n" +
            "    <Document>\n" +
            "        <name>Satellite Trajectories</name>\n" +
            "        \n" +
            " <open>1</open>\n" +
            "        <Style id=\"satellite1\">\n" +
            "            <LineStyle>\n" +
            "                <color>ff0000ff</color>\n" +
            "                <width>2</width>\n" +
            "            </LineStyle>\n" +
            "        </Style>\n" +
            "        \n"

    for (sat in satellites) {
        val satellite1Coordinates = callToServer.getCoordinates(sat)
        kml += "        <Placemark>\n" +
                "            <name>Satellite 1</name>\n" +
                "            <styleUrl>#satellite1</styleUrl>\n" +
                "            <LineString>\n" +
                "                <extrude>1</extrude>\n" +
                "                <tessellate>1</tessellate>\n" +
                "                <altitudeMode>absolute</altitudeMode>\n" +
                "                <coordinates>%satellite1Coordinates%</coordinates>\n" +
                "            </LineString>\n" +
                "        </Placemark>\n" +
                "        \n"

        kml = kml.replace("%satellite1Coordinates%", satellite1Coordinates)
    }
    kml += "    </Document>\n" +
            "</kml>"

    val internalFilesDir = context.filesDir
    val file = File(internalFilesDir, "trajectories.kml")

    withContext(Dispatchers.IO) {
        val outputStream = FileOutputStream(file)
        outputStream.write(kml.toByteArray())
        outputStream.close()

        print(file)


        SSHConnection.sendFile(file, "/var/www/html/trajectories.kml")

        val command2 = "chmod 777 /var/www/html/kmls.txt; echo '" +
                "http://lg1:81/trajectories.kml" +
                "' > /var/www/html/kmls.txt"

        SSHConnection.executeCommand(command2)
    }



}

