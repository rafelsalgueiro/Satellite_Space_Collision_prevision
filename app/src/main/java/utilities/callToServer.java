package utilities;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class callToServer extends Thread {
    private static AtomicReference<String> resultRef = new AtomicReference<>("No");
    private static Context context;

    public static String serverIP;
    public static String predictionPort;
    public static String coordinatesPort;

    public callToServer(Context context, AtomicReference<String> resultRef) {
        callToServer.context = context;
        callToServer.resultRef = resultRef;
    }

    public static void setter(String serverIP, String predictionPort, String coordinatesPort) {
        callToServer.serverIP = serverIP;
        callToServer.predictionPort = predictionPort;
        callToServer.coordinatesPort = coordinatesPort;
    }
    public static boolean connection(){
        if (serverIP == null|| serverIP == "" || predictionPort == null || predictionPort == "" || coordinatesPort == null || coordinatesPort == ""){
            return false;
        }
        else{
            return true;
        }
    }

    public static String sendPostRequest(String data) {
        try {
            AtomicReference<String> resultRef = new AtomicReference<>();
            Thread thread = new Thread(() -> {
                try {
                    try {
                        System.out.println(",a,aguebo: " + data);
                        String inclination = data.split(" ")[0];
                        String eccentricity = ("0" +data.split(" ")[1]);

                        String bstar = ("0"+data.split(" ")[2]);
                        String meanMotion = data.split(" ")[9];
                        String meanAnomaly = data.split(" ")[10];
                        String rightAscension = data.split(" ")[11];
                        URL obj = new URL("http://" + serverIP + ":" + predictionPort + "/predict");
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type", "application/json");
                        con.setDoOutput(true);

                        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                        wr.writeBytes("{\"features\":[["+inclination+", "+meanAnomaly+", "+rightAscension+", "+meanMotion+", "+eccentricity+", "+bstar+"]]}");
                        wr.flush();
                        wr.close();

                        con.connect();


                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String response = in.readLine();
                        in.close();

                        JSONObject jsonResponse = new JSONObject(response);


                        String predictionValue = jsonResponse.getString("prediction");
                        if (predictionValue.equals("true")) {
                            predictionValue = "No";
                        } else {
                            predictionValue = "Yes";
                        }

                        resultRef.set(predictionValue);
                        con.disconnect();

                    } catch (IOException e) {
                        e.printStackTrace();
                        resultRef.set("No");
                    } catch (JSONException e) {
                        resultRef.set("No");
                        throw new RuntimeException(e);

                    }
                    if (resultRef.get().equals("null")) {
                        resultRef.set("No");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            thread.join();
            return resultRef.get();
        } catch (
                Exception e) {
            e.printStackTrace();
            resultRef.set("No");
            return resultRef.get();
        }

    }

    public static String getCoordinates(String sat) throws InterruptedException {
        AtomicReference<String> coordinates = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                String[] lines = getTLEData(sat);
                String line1 = lines[1].substring(1);
                String line2 = lines[2].replace("]", "").substring(1);

                URL obj = new URL("http://" + serverIP + ":" + coordinatesPort + "/calculate_coords");
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes("{\"line1\":\"" + line1 + "\",\"line2\":\"" + line2 + "\"}");
                wr.flush();
                wr.close();

                con.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String response1 = in.readLine();
                in.close();
                coordinates.set(response1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        return coordinates.toString().replace("{\"positions\":[\"", "").replace("\",\"", "\n").replace("\"]}", "");
    }

    public static String[] getTLEData(String satelliteName) throws IOException {
        FileInputStream fis = context.openFileInput("tleSat.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;
        List<String> lines = new ArrayList<>();

        while ((line = br.readLine()) != null) {
            lines.add(line);
        }

        br.close();
        fis.close();

        int indexOfSatellite = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(satelliteName)) {
                indexOfSatellite = i;
                break;
            }
        }

        if (indexOfSatellite == -1) {
            return new String[0];
        }
        return lines.subList(indexOfSatellite, indexOfSatellite + 3).toString().split(",");
    }
}