package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.json.JSONArray;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class TopFiveHandler implements HttpHandler {

    private final TopFiveLocations topFiveLocations;

    public TopFiveHandler() {
        topFiveLocations = new TopFiveLocations();
    }

    /**
     * Handles the HTTP request by determining the request method and content type,
     * and calling the appropriate handler method accordingly.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     */
    @Override
    public synchronized void handle(HttpExchange exchange) {
        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                handleGetRequest(exchange);
            } else {
                sendResponse(exchange, 400, "Not supported");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the GET request by retrieving the top five locations from the server and sending the response.
     *
     * @param exchange the HttpExchange object representing the request.
     * @throws IOException if an I/O error occurs while handling the request.
     */
    private synchronized void handleGetRequest(HttpExchange exchange) throws IOException {
            try {
            JSONArray topFiveMessage = topFiveLocations.getMessages();
            if (topFiveMessage.isEmpty()) {
                sendResponse(exchange, 204, -1);
            } else {
                byte[] responseBytes = topFiveMessage.toString().getBytes(StandardCharsets.UTF_8);
                sendResponse(exchange, 200, new String(responseBytes));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Database error");
        };

    
     /**
     * Reads the request body from the HttpExchange object.
     *
     * @param exchange the HttpExchange object as request.
     * @return the request body as a string.
     * @throws IOException if an I/O error occurs while reading the request body.
     */
    }
    private synchronized String requestBody(HttpExchange exchange) throws IOException {
        InputStream stream = exchange.getRequestBody();
        String requestBody = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        stream.close();
        return requestBody;
    }

     /**
     * Sends a response to the client with the given status code and response body.
     *
     * @param exchange the HttpExchange object as request.
     * @param statusCode the status code to send in the response.
     * @param response the response body to send in the response.
     * @throws IOException if an I/O error occurs while sending the response.
     */
    private synchronized void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }

     /**
     * Sends a response to the client with the given status code and response length.
     * Overloaded method to send response without a response body.
     *
     * @param exchange the HttpExchange object as request.
     * @param statusCode the status code to send in the response.
     * @param length the response length to send in the response.
     * @throws IOException if an I/O error occurs while sending the response.
     */
    private synchronized void sendResponse(HttpExchange exchange, int statusCode, int length) throws IOException {
        exchange.sendResponseHeaders(statusCode, length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.flush();
        outputStream.close();
    }
}
