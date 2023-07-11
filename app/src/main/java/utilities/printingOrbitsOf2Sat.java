package utilities;

public class printingOrbitsOf2Sat {
    public static String createKMLFile() {
        String kmlTemplate = "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
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
                "        \n" +
                "        <Placemark>\n" +
                "            <name>Satellite 1</name>\n" +
                "            <styleUrl>#satellite1</styleUrl>\n" +
                "            <LineString>\n" +
                "                <extrude>1</extrude>\n" +
                "                <tessellate>1</tessellate>\n" +
                "                <altitudeMode>relativeToGround</altitudeMode>\n" +
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
                "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                "                <coordinates>%satellite2Coordinates%</coordinates>\n" +
                "            </LineString>\n" +
                "        </Placemark>\n" +
                "        \n" +
                "    </Document>\n" +
                "</kml>";

        // Coordenadas del satélite 1 con altitud
        String satellite1Coordinates = "-74.0059,40.7128,1000\n" +
                "-77.0369,38.9072,1500\n" +
                "-118.2437,34.0522,2000\n" +
                "-95.3698,29.7604,2500\n" +
                "-122.4194,37.7749,3000\n" +
                "-87.6298,41.8781,3500\n" +
                "-79.3832,43.6532,4000\n" +
                "-71.0589,42.3601,4500\n" +
                "-77.0282,38.9189,5000\n" +
                "-81.6944,41.4993,5500";

        // Coordenadas del satélite 2 con altitud
        String satellite2Coordinates = "2.3522,48.8566,200\n" +
                "-0.1276,51.5074,400\n" +
                "12.4964,41.9028,600\n" +
                "4.8952,52.3702,800\n" +
                "16.3738,48.2082,1000\n" +
                "9.7416,52.3759,1200\n" +
                "17.0385,51.1079,1400\n" +
                "21.0122,52.2297,1600\n" +
                "4.3517,50.8503,1800\n" +
                "6.9603,50.9375,2000";

        String kmlContent = kmlTemplate.replace("%satellite1Coordinates%", satellite1Coordinates)
                .replace("%satellite2Coordinates%", satellite2Coordinates);

        return kmlContent;
    }
}
