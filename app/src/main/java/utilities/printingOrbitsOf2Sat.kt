package utilities

import com.neosensory.tlepredictionengine.TlePredictionEngine
import java.util.Date


fun createKMLFile(): String {
    val tle1 = "1 56446U 23063A   23172.32771504  .00000141  00000+0  58996-5 0  9990"
    val tle2 = "2 56446  41.4746 130.7235 0004813  98.5802 261.5581 15.59339219  6520"
    val initialDate = Date()

    val intervalSeconds = 10 * 60
    val numIntervals = 144

    val allCoordinates = mutableListOf<String>()

    for (i in 0 until numIntervals) {
        val currentDate = Date(initialDate.time + (i * intervalSeconds * 1000))
        val lla_coords = TlePredictionEngine.getSatellitePosition(tle1, tle2, true, currentDate)
        val latitude = lla_coords[0]
        val longitude = lla_coords[1]
        val altitude = lla_coords[2]
        val coordinates = "$latitude,$longitude,$altitude"
        allCoordinates.add(coordinates)
    }

    val coordinates = allCoordinates.joinToString(" ")

//    /* Inserts the orbit part as a tour */
//    String orbit = ActionBuildCommandUtility.buildCommandInsertOrbit (lla_coords, 100000);

    val kmlTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
    "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
            "    <Document id=\"1\">\n" +
            "        <Style id=\"4\">\n" +
            "            <LineStyle id=\"5\">\n" +
            "                <color>ff0000ff</color>\n" +
            "                <colorMode>normal</colorMode>\n" +
            "            </LineStyle>\n" +
            "        </Style>\n" +
            "        <Placemark id=\"3\">\n" +
            "            <name>Satellite 1</name>\n" +
            "            <styleUrl>#4</styleUrl>\n" +
            "            <LineString id=\"8\"> \n" +
            "                <coordinates>$coordinates</coordinates>\n" +
            "        <extrude>0</extrude>\n" +
            "        <altitudeMode>relativeToGround</altitudeMode>" +

    "</LineString>" +
            "        </Placemark>\n" +
            "    </Document>\n" +
            "</kml>"

//    val kmlTemplate = "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
//            "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
//            " xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n" +
//            "    <Document>\n" +
//            "        <name>Satellite Trajectories</name>\n" +
//            "        \n" +
//            " <open>1</open>\n" +
//            "        <Style id=\"satellite1\">\n" +
//            "            <LineStyle>\n" +
//            "                <color>ff0000ff</color>\n" +
//            "                <width>2</width>\n" +
//            "            </LineStyle>\n" +
//            "        </Style>\n" +
//            "        \n" +
//            "        <Style id=\"satellite2\">\n" +
//            "            <LineStyle>\n" +
//            "                <color>ff00ff00</color>\n" +
//            "                <width>2</width>\n" +
//            "            </LineStyle>\n" +
//            "        </Style>\n" +
//            "        \n" +
//            "        <Placemark>\n" +
//            "            <name>Satellite 1</name>\n" +
//            "            <styleUrl>#satellite1</styleUrl>\n" +
//            "            <LineString>\n" +
//            "                <extrude>1</extrude>\n" +
//            "                <tessellate>1</tessellate>\n" +
//            "                <altitudeMode>relativeToGround</altitudeMode>\n" +
//            "                <coordinates>%satellite1Coordinates%</coordinates>\n" +
//            "            </LineString>\n" +
//            "        </Placemark>\n" +
//            "        \n" +
//            "        <Placemark>\n" +
//            "            <name>Satellite 2</name>\n" +
//            "            <styleUrl>#satellite2</styleUrl>\n" +
//            "            <LineString>\n" +
//            "                <extrude>1</extrude>\n" +
//            "                <tessellate>1</tessellate>\n" +
//            "                <altitudeMode>relativeToGround</altitudeMode>\n" +
//            "                <coordinates>%satellite2Coordinates%</coordinates>\n" +
//            "            </LineString>\n" +
//            "        </Placemark>\n" +
//            "        \n" +
//            "    </Document>\n" +
//            "</kml>"
//
//    val satellite1Coordinates = "2.3522,48.8566,200\n" +
//            "-0.1276,51.5074,400\n" +
//            "12.4964,41.9028,600\n" +
//            "4.8952,52.3702,800\n" +
//            "16.3738,48.2082,1000\n" +
//            "9.7416,52.3759,1200\n" +
//            "17.0385,51.1079,1400\n" +
//            "21.0122,52.2297,1600\n" +
//            "4.3517,50.8503,1800\n" +
//            "6.9603,50.9375,2000"
//
//    val satellite2Coordinates = "2.3522,48.8566,200\n" +
//            "-0.1276,51.5074,400\n" +
//            "12.4964,41.9028,600\n" +
//            "4.8952,52.3702,800\n" +
//            "16.3738,48.2082,1000\n" +
//            "9.7416,52.3759,1200\n" +
//            "17.0385,51.1079,1400\n" +
//            "21.0122,52.2297,1600\n" +
//            "4.3517,50.8503,1800\n" +
//            "6.9603,50.9375,2000"

    return kmlTemplate
}
