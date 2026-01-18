package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;


// CommunicationThread gestionează comunicarea cu un singur client.
public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    // Constructorul primește ca parametri serverul și socket-ul clientului.
    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            // Obține fluxurile de citire și scriere pentru socket.
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            // Citește orașul și tipul de informație de la client.
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type)!");
            String city = bufferedReader.readLine();
            String informationType = bufferedReader.readLine();
            if (city == null || city.isEmpty() || informationType == null || informationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type)!");
                return;
            }

            // Verifică dacă datele există deja în cache.
            HashMap<String, WeatherForecastInformation> data = serverThread.getData();
            WeatherForecastInformation weatherForecastInformation;
            if (data.containsKey(city)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                weatherForecastInformation = data.get(city);
            } else {
                // Dacă datele nu sunt în cache, le preia de pe serviciul web.
                Log.i(Constants.TAG, "[COMMunICATION THREAD] Getting the information from the webservice...");
                String pageSourceCode = "";

                // Construiește URL-ul pentru cererea GET.
                URL url = new URL(Constants.WEB_SERVICE_ADDRESS + "?q=" + city + "&appid=" + Constants.WEB_SERVICE_API_KEY);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    pageSourceCode = response.toString();
                }
                connection.disconnect();

                if (pageSourceCode == null || pageSourceCode.isEmpty()) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                }

                // Parsează răspunsul JSON.
                JSONObject content = new JSONObject(pageSourceCode);
                JSONObject main = content.getJSONObject("main");
                String temperature = main.getString("temp");
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");
                JSONObject wind = content.getJSONObject("wind");
                String windSpeed = wind.getString("speed");
                String condition = content.getJSONArray("weather").getJSONObject(0).getString("main");

                // Creează un nou obiect WeatherForecastInformation.
                weatherForecastInformation = new WeatherForecastInformation(temperature, windSpeed, condition, pressure, humidity);
                // Adaugă datele în cache.
                serverThread.setData(city, weatherForecastInformation);
            }

            if (weatherForecastInformation == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }

            // Trimite răspunsul la client în funcție de tipul de informație cerut.
            String result;
            switch (informationType) {
                case Constants.ALL:
                    result = weatherForecastInformation.toString();
                    break;
                case Constants.TEMPERATURE:
                    result = weatherForecastInformation.getTemperature();
                    break;
                case Constants.WIND_SPEED:
                    result = weatherForecastInformation.getWindSpeed();
                    break;
                case Constants.CONDITION:
                    result = weatherForecastInformation.getCondition();
                    break;
                case Constants.PRESSURE:
                    result = weatherForecastInformation.getPressure();
                    break;
                case Constants.HUMIDITY:
                    result = weatherForecastInformation.getHumidity();
                    break;
                default:
                    result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / pressure / humidity)! ";
            }
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException | JSONException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
        } finally {
            if (socket != null) {
                try {
                    // Închide socket-ul după ce comunicarea s-a încheiat.
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                }
            }
        }
    }
}
