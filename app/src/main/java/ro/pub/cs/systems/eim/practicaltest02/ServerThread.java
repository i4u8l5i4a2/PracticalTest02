package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

// ServerThread este un fir de execuție care gestionează conexiunile clienților.
public class ServerThread extends Thread {

    // Portul pe care serverul va asculta conexiuni.
    private int port = 0;
    // Socket-ul serverului.
    private ServerSocket serverSocket = null;

    // Un HashMap pentru a stoca (cache) datele meteo pentru fiecare oraș.
    // Cheia este numele orașului, iar valoarea este un obiect WeatherForecastInformation.
    private HashMap<String, WeatherForecastInformation> data = null;

    // Constructorul clasei. Primește portul ca parametru.
    public ServerThread(int port) {
        this.port = port;
        try {
            // Inițializează ServerSocket-ul pe portul specificat.
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            // Înregistrează o eroare dacă ServerSocket-ul nu poate fi creat.
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
        }
        // Inițializează HashMap-ul pentru cache-ul de date.
        this.data = new HashMap<>();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    // Metoda run() este punctul de intrare pentru firul de execuție.
    @Override
    public void run() {
        try {
            // Buclă infinită pentru a accepta conexiuni de la clienți.
            while (!Thread.currentThread().isInterrupted()) {
                // Așteaptă și acceptă o nouă conexiune de la un client.
                Log.i(Constants.TAG, "[SERVER THREAD] Waiting for a client invocation...");
                Socket socket = serverSocket.accept();
                Log.i(Constants.TAG, "[SERVER THREAD] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());

                // Pentru fiecare client, creează un nou fir de execuție pentru comunicare.
                CommunicationThread communicationThread = new CommunicationThread(this, socket);
                communicationThread.start();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
        }
    }

    // Metodă sincronizată pentru a adăuga date în cache.
    public synchronized void setData(String city, WeatherForecastInformation weatherForecastInformation) {
        this.data.put(city, weatherForecastInformation);
    }

    // Metodă sincronizată pentru a obține date din cache.
    public synchronized HashMap<String, WeatherForecastInformation> getData() {
        return data;
    }


    // Metodă pentru a opri serverul.
    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try {
                // Închide ServerSocket-ul, ceea ce va debloca `accept()` și va termina bucla.
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[SERVER THREAD] An exception has occurred: " + ioException.getMessage());
            }
        }
    }
}
