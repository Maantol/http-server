package com.server;

import java.net.HttpURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class WeatherClient {
    
    public WeatherClient() {
    }

    /**
     * Retrieves the weather information for a given latitude and longitude.
     * 
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The temperature of the weather as integer.
     * @throws IOException If an I/O error occurs while making the HTTP request.
     * @throws JSONException If the response from the server is not in the expected JSON format.
     */

    public int getWeather(double latitude, double longitude) throws IOException, JSONException {
        URL url = new URL("http://localhost:4001/weather");
        
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(20000);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);

        urlConnection.setRequestProperty("Content-Type", "application/xml");

        String xmlMessage = "<coordinates>\n" +
                    "    <latitude>" + latitude + "</latitude>\n" +
                    "    <longitude>" + longitude + "</longitude>\n" +
                    "</coordinates>";

        try (OutputStream outputStream = urlConnection.getOutputStream()) {
            outputStream.write(xmlMessage.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
        }

        JSONObject xmlResponse = XML.toJSONObject(responseBuilder.toString());
        int weather = xmlResponse.getJSONObject("weather").getInt("temperature");
        
        return weather;
    }
}