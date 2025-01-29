package com.server;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.*;

public class UserAuthenticator extends BasicAuthenticator {

    private final MessageDatabase myDatabase = MessageDatabase.getInstance();

    public UserAuthenticator() {
        super("info");
    }

    /**
     * Handles the authentication and registration of a user.
     * 
     * @param user the JSON object containing user information
     * @return true if the user is successfully authenticated or registered, false otherwise
     */

    public synchronized boolean handleUser(JSONObject user) {
        try {
            String username = user.getString("username");
            String password = user.getString("password");
            String email = user.getString("email");
            String userNickname = user.getString("userNickname");

            if (username.isEmpty() || password.isEmpty()) {
                return false;
            } 

            boolean result = addUser(username, password, email, userNickname);
            if (!result) {
                return false;
            }
            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Adds a new user to the database.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @param email the email address of the user
     * @param userNickname the nickname of the user
     * @return true if the user is successfully added, false otherwise
     */
    public synchronized boolean addUser(String username, String password, String email, String userNickname) {
        JSONObject userJson = new JSONObject();


        userJson.put("username", username);
        userJson.put("password", password);
        userJson.put("email", email);
        userJson.put("userNickname", userNickname);
    
        try {
            boolean result = myDatabase.setUser(userJson);
            if (!result) {
                return false;
            }
            return true;

        } catch (SQLException e) {
         e.getMessage();
            return false;
        }
    }

    /**
     * Checks the credentials of a user by authenticating the provided username and password.
     * 
     * @param username the username of the user
     * @param password the password of the user
     * @return true if the credentials are valid, false otherwise
     */
    public synchronized boolean checkCredentials(String username, String password) {

        try {
            boolean isValidUser = myDatabase.authenticateUser(username, password);
            if (!isValidUser) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


  }

