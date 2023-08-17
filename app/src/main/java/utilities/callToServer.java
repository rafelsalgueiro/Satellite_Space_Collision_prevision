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
    private static AtomicReference<String> resultRef = new AtomicReference<>("False");
    private static Context context;

    public callToServer(Context context, AtomicReference<String> resultRef) {
        callToServer.context = context;
        callToServer.resultRef = resultRef;
    }

    @Override
    public void run() {
        try {
            URL obj = new URL("http://192.168.1.115:8080/predict");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes("{\"features\":[[66.8199, 72.1786, 79.1120, 13.868643, 0.007818, 0.0]]}");
            wr.flush();
            wr.close();

            con.connect();


            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String response = in.readLine();
            in.close();

            JSONObject jsonResponse = new JSONObject(response);


            String predictionValue = jsonResponse.getString("prediction");
            if (predictionValue.equals("true")){
                predictionValue = "False";
            } else {
                predictionValue = "True";
            }

            resultRef.set(predictionValue);
            con.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sendPostRequest() {
        try {
            AtomicReference<String> resultRef = new AtomicReference<>();

            Thread thread = new callToServer(context, resultRef);
            thread.start();
            thread.join();
            if (resultRef.get().equals("null")) {
                resultRef.set("False");
            }

            return resultRef.get();
        } catch (Exception e) {
            e.printStackTrace();
            resultRef.set("False");
            return resultRef.get();
      }
    }

    public static String getCoordinates(String sat) {
        AtomicReference<String> coordinates = new AtomicReference<>();
                Thread thread = new Thread(() -> {
                    try {
                String[] lines = getTLEData(sat);
                String line1 = lines[1];
                String line2 = lines[2].replace("]", "");

                URL obj = new URL("http://192.168.1.115:8081/calculate_coords");
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                System.out.println("{\"line1\":" + line1 + ",\"line2\":" + line2 + "}");
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
        return coordinates.get();
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