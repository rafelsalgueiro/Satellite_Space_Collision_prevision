package utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class callToServer extends Thread {
    private static AtomicReference<String> resultRef = new AtomicReference<>("False");

    public callToServer(AtomicReference<String> resultRef) {
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

            Thread thread = new callToServer(resultRef);
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
}