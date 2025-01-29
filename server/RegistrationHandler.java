package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONException;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {

    private final UserAuthenticator userAuthenticator;

    public RegistrationHandler(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }


    /**
     * Handles the HTTP request by determining the request method and content type,
     * and calling the appropriate handler method accordingly.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     */
    @Override
    public synchronized void handle(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getRequestHeaders();
        String contentType = "";

        try {
            contentType = headers.get("Content-Type").get(0);

            if (!contentType.equalsIgnoreCase("application/json")) {
                sendResponse(exchange, 415, "Unsupported content type");
                return;
            }
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                handlePostRequest(exchange);
            } else {
                sendResponse(exchange, 400, "Not supported");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a POST request received by the server.
     * Sends JSON data to the UserAuthenticator class to be handled.
     * Handles all situations where the server receives a POST request.
     *
     * @param exchange the HttpExchange object as request.
     * @throws IOException if an I/O error occurs while handling the request.
     */
    private synchronized void handlePostRequest(HttpExchange exchange) throws IOException {
        String requestBody = requestBody(exchange);
    
        if (requestBody == null || requestBody.isEmpty()) {
            sendResponse(exchange, 400, "POST Empty");
            return;
        }
    
        try {
            JSONObject json = new JSONObject(requestBody);

            Boolean userAuthenticate = userAuthenticator.handleUser(json);
    
            if (!userAuthenticate) {
                sendResponse(exchange, 405, "Registration failed!");
            } else {
                sendResponse(exchange, 200, "User registered");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            sendResponse(exchange, 400, "Invalid JSON!");
        } catch (IOException e) {
            sendResponse(exchange, 500, "Internal server error");
            e.printStackTrace();
        }
    }

     /**
     * Reads the request body from the HttpExchange object.
     *
     * @param exchange the HttpExchange object as request.
     * @return the request body as a string.
     * @throws IOException if an I/O error occurs while reading the request body.
     */
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
}