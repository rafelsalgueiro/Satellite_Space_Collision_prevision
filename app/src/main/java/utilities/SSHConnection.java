package utilities;

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
    private Session session;
    private String user;


    public SSHConnection(String ipAddress, String password, int numSlaves) {
        this.ipAddress = ipAddress;
        this.password = password;
        this.numSlaves = numSlaves;

    }

    public boolean isConnected() {
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
                String sentence = "chmod 777 /var/www/html/kml/" + logoSlaves + ".kml; echo '" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
                        "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
                        " xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n" +
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
                        " <size x=\"0.6\" y=\"0.65\" xunits=\"fraction\" yunits=\"fraction\"/> \n" +
                        "</ScreenOverlay> \n" +
                        " </Folder> \n" +
                        " </Document> \n" +
                        " </kml>\n' > /var/www/html/kml/" + logoSlaves + ".kml";
                executeCommand(sentence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void cleanKml() {
        Thread thread = new Thread(() -> {
            try {
                for (int i = 0; i <= numSlaves; i++) {
                    String slaves ="slave_" + i;
                    System.out.println(i);
                    String sentence = "chmod 777 /var/www/html/kml/" + slaves + ".kml; echo '" +
                            "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
                            "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
                            " xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n" +
                            " <Document id=\"" + slaves + "\">\n " +
                            " </Document> \n" +
                            " </kml>\n' > /var/www/html/kml/" + slaves + ".kml";
                    executeCommand(sentence);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }


    public void hideLogos() {
        Thread thread = new Thread(() -> {
            try {
                String sentence = "chmod 777 /var/www/html/kml/" + logoSlaves + ".kml; echo '" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
                        "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
                        " xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n" +
                        " <Document id=\"" + logoSlaves + "\">\n " +
                        " </Document> \n" +
                        " </kml>\n' > /var/www/html/kml/" + logoSlaves + ".kml";
                executeCommand(sentence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void rebootButton() {
        if (isConnected()) {
            Thread thread = new Thread(() -> {
                try {
                    String command = "/home/" + user + "/bin/lg-reboot > /home/" + user + "/log.txt;\n" +
                            "REBOOT_CMD=\"sudo lg-reboot\"\n" +
                            "sshpass -p " + password + " ssh -x -t lg@lg1 \"$REBOOT_CMD\"";
                    executeCommand(command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }

    public void poweroffButton() {
        if (isConnected()) {
            Thread thread = new Thread(() -> {
                try {
                    for (int i = 0; i <= numSlaves; i++) {
                        String command ="sshpass -p "+password+" ssh -t lg"+i+" echo "+password+" | sudo -S poweroff";

                        executeCommand(command);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
    }

    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
        session = null;
    }

    public void executeCommand(String command) throws JSchException {
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

}
