package ro.pub.cs.systems.eim.practicaltest02;

public interface Constants {

    String TAG = "PracticalTest02";

    // Flag pentru modul de debug
    boolean DEBUG = true;

    // Cheia API și adresa serviciului web OpenWeatherMap
    String WEB_SERVICE_API_KEY = "e03c3b32cfb5a6f7069f2ef29237d87e";
    String WEB_SERVICE_ADDRESS = "https://api.openweathermap.org/data/2.5/weather";

    // Constanta pentru șir gol
    String EMPTY_STRING = "";

    // Tipurile de informații pe care le poate cere clientul
    String ALL = "all";
    String TEMPERATURE = "temperature";
    String WIND_SPEED = "wind_speed";
    String CONDITION = "condition";
    String PRESSURE = "pressure";
    String HUMIDITY = "humidity";

}
