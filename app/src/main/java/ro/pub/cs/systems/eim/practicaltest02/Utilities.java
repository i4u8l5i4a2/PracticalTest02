package ro.pub.cs.systems.eim.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Clasa Utilities oferă metode ajutătoare pentru lucrul cu socket-uri.
public class Utilities {

    // Metodă statică pentru a obține un BufferedReader dintr-un socket.
    public static BufferedReader getReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Metodă statică pentru a obține un PrintWriter dintr-un socket.
    public static PrintWriter getWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }

}
