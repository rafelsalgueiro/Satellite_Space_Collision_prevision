package utilities;

import static utilities.PrintingOrbitsOf2SatKt.createKMLFile1;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class SSHConnection {
    private final String ipAddress;
    private final String password;
    private final int numSlaves;
    private String logoSlaves;

    private static String infoSlave;
    private static Session session;
    private String user;


    public SSHConnection(String ipAddress, String password, int numSlaves) {
        this.ipAddress = ipAddress;
        this.password = password;
        this.numSlaves = numSlaves;

    }

    public static boolean isConnected() {
        return session != null && session.isConnected();
    }

    public int leftScreen() {

        if (numSlaves == 1) {
            return 1;
        }

        return (numSlaves / 2) + 2;
    }

    public int rightScreen() {

        if (numSlaves == 1) {
            return 1;
        }

        return (numSlaves / 2) + 1;
    }

    public boolean connect() {
        user = "lg";
        int port = 22;
        logoSlaves = "slave_" + leftScreen();
        infoSlave = "slave_" + rightScreen();
        JSch jsch = new JSch();
        try {
            Thread connectionThread = new Thread(() -> {
                try {
                    if (session == null || !session.isConnected()) {
                        session = jsch.getSession(user, ipAddress, port); // Rename the variable here
                        session.setPassword(password);

                        Properties prop = new Properties();
                        prop.put("StrictHostKeyChecking", "no");
                        session.setConfig(prop);

                        session.connect(Integer.MAX_VALUE);
                        cleanKml();
                        Thread.sleep(300);
                        displayLogos();
                    } else {
                        session.sendKeepAliveMsg();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            connectionThread.start();
            connectionThread.join();

            return session != null && session.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void displayLogos() {
        Thread thread = new Thread(() -> {
            try {
                String command = "chmod 777 /var/www/html/kml/" + logoSlaves + ".kml; echo '" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
                        "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
                        " xmlns:gx=\"http://www.google.com/kml/ext/2.2\" \n" +
                        "xmlns:kml=\"http://www.opengis.net/kml/2.2\"> \n" +
                        " <Document>\n " +
                        " <Folder> \n" +
                        "<name>Logos</name> \n" +
                        "<ScreenOverlay>\n" +
                        "<name>Logo</name> \n" +
                        " <Icon> \n" +
                        "<href>https://raw.githubusercontent.com/rafelsalgueiro/Satellite_Space_Collision_prevision/master/app/src/main/res/drawable/all_logos.png</href> \n" +
                        " </Icon> \n" +
                        " <overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/> \n" +
                        " <screenXY x=\"0.02\" y=\"0.95\" xunits=\"fraction\" yunits=\"fraction\"/> \n" +
                        " <rotationXY x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/> \n" +
                        " <size x=\"600\" y=\"650\" xunits=\"pixels\" yunits=\"pixels\"/> \n" +
                        "</ScreenOverlay> \n" +
                        " </Folder> \n" +
                        " </Document> \n" +
                        " </kml>\n' > /var/www/html/kml/" + logoSlaves + ".kml";
                executeCommand(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void cleanKml() {
        Thread thread = new Thread(() -> {
            try {
                for (int i = 1; i <= numSlaves; i++) {
                    String slaves = "slave_" + i;
                    String command = "chmod 777 /var/www/html/kml/" + slaves + ".kml; echo '" +
                            "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
                            "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
                            " xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n" +
                            " <Document id=\"" + slaves + "\">\n " +
                            " </Document> \n" +
                            " </kml>\n' > /var/www/html/kml/" + slaves + ".kml";
                    executeCommand(command);

                    String command2 = "chmod 777 /var/www/html/kmls.txt; echo '' > /var/www/html/kmls.txt";
                    executeCommand(command2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void flyto(String poi) {
        Thread thread = new Thread(() -> {
            try {
                String[] pos = poi.split(",");
                String command = "echo 'flytoview=<gx:duration>5</gx:duration><LookAt><longitude>"+pos[0]+"</longitude><latitude>"+pos[1]+"</latitude><altitude>25000000</altitude><heading>0</heading><tilt>0</tilt><range>1492.66</range><gx:altitudeMode>relativeToGround</gx:altitudeMode></LookAt>' > /tmp/query.txt";
                executeCommand(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }


    public void hideLogos() {
        Thread thread = new Thread(() -> {
            try {
                String command = "chmod 777 /var/www/html/kml/" + logoSlaves + ".kml; echo '" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
                        "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
                        " xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n" +
                        " <Document id=\"" + logoSlaves + "\">\n " +
                        " </Document> \n" +
                        " </kml>\n' > /var/www/html/kml/" + logoSlaves + ".kml";
                executeCommand(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void rebootButton() {
        final String[] command = {null};
        Thread thread = new Thread(() -> {
            try {
                for (int i = numSlaves; i >= 1; i--) {
                    if (i == numSlaves) {
                        command[0] = "sshpass -p " + password + " ssh -t lg" + i + " \"echo " + password + " | sudo -S reboot\"";
                    } else {
                        command[0] += "; sshpass -p " + password + " ssh -t lg" + i + " \"echo " + password + " | sudo -S reboot\"";
                    }
                    executeCommand(command[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void poweroffButton() {
        final String[] command = {null};
        Thread thread = new Thread(() -> {
            try {
                for (int i = numSlaves; i >= 1; i--) {
                    if (i == numSlaves) {
                        command[0] = "sshpass -p " + password + " ssh -t lg" + i + " \"echo " + password + " | sudo -S poweroff\"";
                    } else {
                        command[0] += "; sshpass -p " + password + " ssh -t lg" + i + " \"echo " + password + " | sudo -S poweroff\"";
                    }
                    executeCommand(command[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
        session = null;
    }

    public static void executeCommand(String command) throws JSchException {
        if (session == null || !session.isConnected()) {
            return;
        }

        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        channelssh.setOutputStream(baos);

        channelssh.setCommand(command);
        channelssh.connect();

        channelssh.disconnect();

    }

    public static void tour() {
        Thread thread = new Thread(() -> {
            try {
                String command = "echo \"playtour=Satellitetour\" > /tmp/query.txt";
                executeCommand(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void testingPrintingSats() {
        Thread thread = new Thread(() -> {
            try {
                String command = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    command = "chmod 777 /var/www/html/satellites.kml; echo '" +
                            createKMLFile1("TIANZHOU-6", "CALSPHERE 1") +
                            "' > /var/www/html/satellites.kml";
                }
                executeCommand(command);
                String command2 = "chmod 777 /var/www/html/kmls.txt; echo '" +
                        "http://lg1:81/satellites.kml" +
                        "' >> /var/www/html/kmls.txt";
                executeCommand(command2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void printSatInfo(String data, String collision) {
        Thread thread = new Thread(() -> {
            try {
                String[] dataSection = data.split(",");
                String sat1 = dataSection[0].replace("?", "").replace("[", "").replace("]", "");
                String tle11 = dataSection[1].replace("?", "").replace("[", "").replace("]", "");
                String tle12 = dataSection[2].replace("?", "").replace("[", "").replace("]", "");
                String sat2 = dataSection[3].replace("?", "").replace("[", "").replace("]", "");
                String tle21 = dataSection[4].replace("?", "").replace("[", "").replace("]", "");
                String tle22 = dataSection[5].replace("?", "").replace("[", "").replace("]", "");
                String command = "chmod 777 /var/www/html/kml/" + infoSlave + ".kml; echo '" +
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                        "<Document>\n" +
                        " <name>historic.kml</name> \n" +
                        " <Style id=\"purple_paddle\">\n" +
                        "   <BalloonStyle>\n" +
                        "     <text>$[description]</text>\n" +
                        "     <bgColor>ffffffff</bgColor>\n" +
                        "   </BalloonStyle>\n" +
                        " </Style>\n" +
                        " <Placemark id=\"0A7ACC68BF23CB81B354\">\n" +
                        "   <name>Historic Track Map</name>\n" +
                        "   <Snippet maxLines=\"0\"></Snippet>\n" +
                        "   <description><![CDATA[<!-- BalloonStyle background color:\n" +
                        "                ffffffff\n" +
                        "     -->" +
                        " <tr>\n" +
                        "   <td colspan=\"2\" align=\"center\">\n" +
                        "     <h2>%sat1%</font></h2>\n" +
                        "     <h3>TLE 1: %tle11%</font></h3>\n" +
                        "     <h3>TLE 1: %tle12%</font></h3>\n" +
                        "   </td>\n" +
                        " </tr>\n" +
                        " <tr>\n" +
                        "   <td colspan=\"2\" align=\"center\">\n" +
                        "     <h2>%sat2%</font></h2>\n" +
                        "     <h3>TLE 1: %tle21%</font></h3>\n" +
                        "     <h3>TLE 1: %tle22%</font></h3>\n" +
                        "   </td>\n" +
                        " </tr>\n" +
                        " <tr>\n" +
                        "   <td colspan=\"2\" align=\"center\">\n" +
                        "     <h2>Collision: %collision% </font></h2>\n" +
                        "</table>]]></description>\n" +
                        "   <LookAt>\n" +
                        "     <longitude>-17.841486</longitude>\n" +
                        "     <latitude>28.638478</latitude>\n" +
                        "     <altitude>0</altitude>\n" +
                        "     <heading>0</heading>\n" +
                        "     <tilt>0</tilt>\n" +
                        "     <range>24000</range>\n" +
                        "  </LookAt>\n" +
                        "   <styleUrl>#purple_paddle</styleUrl>\n" +
                        "   <gx:balloonVisibility>1</gx:balloonVisibility>\n" +
                        "   <Point>\n" +
                        "     <coordinates>-17.841486,28.638478,0</coordinates>\n" +
                        "   </Point>\n" +
                        " </Placemark>\n" +
                        "</Document>\n" +
                        "</kml>\n' > /var/www/html/kml/" + infoSlave + ".kml";

                String kmlContent = command.replace("%sat1%", "sat1").replace("%tle11%", "tle11").replace("%tle12%", "tle12").replace("%sat2%", "sat2").replace("%tle21%", "tle21").replace("%tle22%", "tle22").replace("%collision%", collision);
                executeCommand(kmlContent);


            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void printCollision(String circleCoordinates) {
        Thread thread = new Thread(() -> {
            try {
                String command = "chmod 777 /var/www/html/collision.kml; echo '" +
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
                        "    <Document>\n" +
                        "        <Style id=\"style_dfym\">\n" +
                        "            <LineStyle>\n" +
                        "                <color>FFB57EDC</color>\n" +
                        "                <width>2</width>\n" +
                        "            </LineStyle>\n" +
                        "\n" +
                        "            <PolyStyle>\n" +
                        "                <color>640000ff</color>\n" +
                        "                <altitudeMode>relativeToGround</altitudeMode>\n" +
                        "                <colorMode>normal</colorMode>\n" +
                        "                <fill>1</fill>\n" +
                        "                <outline>1</outline>\n" +
                        "            </PolyStyle>\n" +
                        "        </Style>\n" +
                        "        \n" +
                        "<Placemark>\n" +
                        "      <name>Collision</name>\n" +
                        "      <styleUrl>style_dfym</styleUrl>\n" +
                        "            <Polygon id=\"Path\">\n" +
                        "        <extrude>0</extrude>\n" +
                        "        <outerBoundaryIs>\n" +
                        "          <LinearRing>\n" +
                        "            <coordinates>\n" +
                        "              ${circleCoordinates}\n" +
                        "            </coordinates>\n" +
                        "          </LinearRing>\n" +
                        "        </outerBoundaryIs>\n" +
                        "      </Polygon>\n" +
                        "   </Placemark>" +
                        "    </Document>\n" +
                        "</kml>' > /var/www/html/collision.kml";
                command = command.replace("${circleCoordinates}", circleCoordinates);
                executeCommand(command);
                String command2 = "chmod 777 /var/www/html/kmls.txt; echo '" +
                        "http://lg1:81/collision.kml" +
                        "' > /var/www/html/kmls.txt";
                executeCommand(command2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static void stopTour() {
        Thread thread = new Thread(() -> {
            try {
                String command = "echo \"exittour=true\" > /tmp/query.txt";
                executeCommand(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

    }
}
