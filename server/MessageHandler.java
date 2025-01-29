package com.server;

import com.sun.net.httpserver.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class MessageHandler implements HttpHandler {

    private final UserMessage userMessage;

    public MessageHandler() {
        userMessage = new UserMessage();
    }

    /**
     * Handles the HTTP request by determining the request method and content type,
     * and calling the appropriate handler method accordingly.
     *
     * @param exchange the HttpExchange object representing the HTTP request and response
     */
    @Override
    public synchronized void handle(HttpExchange exchange) {

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
            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                handleGetRequest(exchange);
            } else {
                sendResponse(exchange, 400, "Not supported");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a POST request received by the server.
     * Sends JSON data to the UserMessage class to be handled.
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
            String userName = exchange.getPrincipal().getUsername();
            Boolean messageHandled = userMessage.handleMessage(json, userName);
            if (messageHandled) {
                sendResponse(exchange, 200, "OK");
            } else {
                sendResponse(exchange, 405, "Failed to handle message");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            sendResponse(exchange, 400, "Invalid JSON!");
        }
    }

    /**
     * Handles a GET request by retrieving user messages and sending a response.
     * Sends JSON Array to the client when GET is requested.
     * If there is error sends appropriate error code.
     *
     * @param  exchange the HttpExchange object as request.
     * @throws IOException if an I/O error occurs while handling the request
     */
    private synchronized void handleGetRequest(HttpExchange exchange) throws IOException {
        try {
            JSONArray userMessages = userMessage.getMessages();
            if (userMessages.isEmpty()) {
                sendResponse(exchange, 204, -1);
            } else {
                byte[] responseBytes = userMessages.toString().getBytes(StandardCharsets.UTF_8);
                sendResponse(exchange, 200, new String(responseBytes));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Database error");
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