package utilities;

import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SSHConnection {
    private final String ipAddress;
    private final String password;
    private Session session;
    private ChannelExec channel;

    public SSHConnection(String ipAddress, String password) {
        this.ipAddress = ipAddress;
        this.password = password;
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    public boolean connect() {
        try {
            Thread connectionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSch jsch = new JSch();
                        Session sshSession = jsch.getSession("lg", ipAddress, 22); // Rename the variable here
                        sshSession.setPassword(password);

                        Properties prop = new Properties();
                        prop.put("StrictHostKeyChecking", "no");
                        sshSession.setConfig(prop);

                        sshSession.connect();

                        session = sshSession; // Assign the value to the class-level variable

                        channel = (ChannelExec) session.openChannel("exec");
                        channel.setCommand("lg-poweroff");
                        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                        channel.setOutputStream(responseStream);
                        channel.connect();

                        while (channel.isConnected()) {
                            Thread.sleep(100);
                        }
                        // Log the command output
                        String commandOutput = responseStream.toString("UTF-8");
                        Log.d("Command Output", commandOutput);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        session = null;
    }

    public String executeCommand(String command) {
        StringBuilder output = new StringBuilder();

        try {
            if (session != null && session.isConnected()) {
                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(command);
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                channel.setOutputStream(responseStream);
                channel.connect();
                while (channel.isConnected()) {
                    Thread.sleep(100);
                }
                output.append(responseStream.toString("UTF-8"));

                // Log the command output
                String commandOutput = responseStream.toString("UTF-8");
                Log.d("Command Output", commandOutput);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }

        return output.toString();
    }


    public Future<String> executeCommandAsync(final String command) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> task = () -> executeCommand(command);
        Future<String> future = executor.submit(task);
        executor.shutdown();
        return future;
    }
}
