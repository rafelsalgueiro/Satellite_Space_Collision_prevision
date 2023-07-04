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
    private Session session;


    public SSHConnection(String ipAddress, String password) {
        this.ipAddress = ipAddress;
        this.password = password;

    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    public boolean connect() {
        String user = "lg";
        int port = 22;
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


    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
        session = null;
    }

    public String executeCommand(String command) throws JSchException {
        if (session == null || !session.isConnected()) {
            return "No command connection";
        }

        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        channelssh.setOutputStream(baos);

        channelssh.setCommand(command);
        channelssh.connect();

        String exitStatus = String.valueOf(channelssh.getExitStatus());

        channelssh.disconnect();
        System.out.println(baos);

        return exitStatus;
    }

    /*private void powerOff() {
        try {
            String sentence = "/home/lg/bin/lg-poweroff > /home/lg/log.txt";
            showAlertAndExecution(sentence, "shut down");
        } catch (Exception e) {
            Toast.makeText(getActivity(), "error", Toast.LENGTH_LONG).show();
        }
    }*/

}
