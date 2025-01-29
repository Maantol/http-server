package com.server;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserMessage {

    private MessageDatabase myDatabase = MessageDatabase.getInstance();

    public UserMessage() {
    }

    /**
     * Handles the user message and performs the necessary actions based on the provided JSON data.
     * Based on JSON data, the method will either add a new message, update an existing message, or add a visit to a location.
     * 
     * @param json The JSON object containing the message data.
     * @param userName The username of the user sending the message.
     * @return {@code true} if the message was successfully handled, {@code false} otherwise.
     */
    public synchronized Boolean handleMessage(JSONObject json, String userName) {
        try {
            if (json.has("locationID") && json.has("locationVisitor")) {
                int locationID = json.getInt("locationID");
                String locationVisitor = json.getString("locationVisitor");
    
                return addVisit(locationID, locationVisitor);

            } else if (json.has("locationID") && json.has("updatereason")) {
                int locationID = json.getInt("locationID");
                String updateReason = json.getString("updatereason");
    
                String locationName = json.getString("locationName");
                String locationDescription = json.getString("locationDescription");
                String locationCity = json.getString("locationCity");
                String locationCountry = json.getString("locationCountry");
                String locationStreetAddress = json.getString("locationStreetAddress");
                String originalPostingTime = json.getString("originalPostingTime");
                Double latitude = json.optDouble("latitude", 0.0);
                Double longitude = json.optDouble("longitude", 0.0);
    
                if (locationName.isEmpty() || locationDescription.isEmpty() || locationCity.isEmpty()
                        || locationCountry.isEmpty() || locationStreetAddress.isEmpty() || originalPostingTime.isEmpty()
                        || !TimeTools.isGivenTimeValid(originalPostingTime)) {
                    return false;
                }
    
                return updateMessage(locationID, locationName, locationDescription, locationCity,
                        locationCountry, locationStreetAddress, originalPostingTime, latitude, longitude, userName,
                        updateReason);

            } else {
                String locationName = json.getString("locationName");
                String locationDescription = json.getString("locationDescription");
                String locationCity = json.getString("locationCity");
                String locationCountry = json.getString("locationCountry");
                String locationStreetAddress = json.getString("locationStreetAddress");
                String originalPostingTime = json.getString("originalPostingTime");
                Double latitude = json.optDouble("latitude", 0.0);
                Double longitude = json.optDouble("longitude", 0.0);
                int weather = 0;

                if (json.has("weather")) {
                    weather = 1;
                }
                else {
                    weather = 0;
                }
    
                if (locationName.isEmpty() || locationDescription.isEmpty() || locationCity.isEmpty()
                        || locationCountry.isEmpty() || locationStreetAddress.isEmpty() || originalPostingTime.isEmpty()
                        || !TimeTools.isGivenTimeValid(originalPostingTime)) {
                    return false;
                }
    
                return addMessage(locationName, locationDescription, locationCity, locationCountry,
                        locationStreetAddress, originalPostingTime, latitude, longitude, userName, weather);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a message to the database with the specified details.
     * 
     * @param locationName           the name of the location
     * @param locationDescription    the description of the location
     * @param locationCity           the city of the location
     * @param locationCountry        the country of the location
     * @param locationStreetAddress  the street address of the location
     * @param originalPostingTime    the original posting time of the message
     * @param latitude               the latitude of the location
     * @param longitude              the longitude of the location
     * @param userName               the username of the user
     * @param weather                the weather status request if true (1) or false (0)
     * @return                       true if the message is successfully added, false otherwise
     */
    public synchronized Boolean addMessage(String locationName, String locationDescription, String locationCity,
            String locationCountry, String locationStreetAddress, String originalPostingTime, double latitude,
            double longitude, String userName, int weather) {
        JSONObject message = new JSONObject();

        long epochTimestamp = TimeTools.convertZoneTimeToEpoch(originalPostingTime);
        String userNickName = null;

        try {
            userNickName = myDatabase.getUserNickname(userName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        message.put("locationName", locationName);
        message.put("locationDescription", locationDescription);
        message.put("originalPostingTime", epochTimestamp);
        message.put("locationCity", locationCity);
        message.put("locationCountry", locationCountry);
        message.put("locationStreetAddress", locationStreetAddress);
        message.put("latitude", latitude);
        message.put("longitude", longitude);
        message.put("userNickname", userNickName);
        message.put("weather", weather);

        try {
            boolean result = myDatabase.setMessage(message);
            if (!result) {
                return false;
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the message with the specified details.
     *
     * @param locationID the ID of the location
     * @param locationName the name of the location
     * @param locationDescription the description of the location
     * @param locationCity the city of the location
     * @param locationCountry the country of the location
     * @param locationStreetAddress the street address of the location
     * @param originalPostingTime the original posting time of the message
     * @param latitude the latitude of the location
     * @param longitude the longitude of the location
     * @param userName the username of the user
     * @param updateReason the reason for the update
     * @return true if the message was successfully updated, false otherwise
     */
    public synchronized Boolean updateMessage(int locationID, String locationName, String locationDescription, String locationCity,
            String locationCountry, String locationStreetAddress, String originalPostingTime, double latitude,
            double longitude, String userName, String updateReason) {
        JSONObject updateMessage = new JSONObject();

        long updateTimeNowEpoch = TimeTools.currentTimeinEpoch();
        String userNickName = null;

        try {
            userNickName = myDatabase.getUserNickname(userName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        updateMessage.put("locationID", locationID); 
        updateMessage.put("locationName", locationName);
        updateMessage.put("locationDescription", locationDescription);
        updateMessage.put("locationCity", locationCity);
        updateMessage.put("locationCountry", locationCountry);
        updateMessage.put("locationStreetAddress", locationStreetAddress);
        updateMessage.put("latitude", latitude);
        updateMessage.put("longitude", longitude);
        updateMessage.put("userNickname", userNickName);
        updateMessage.put("updatereason", updateReason);
        updateMessage.put("modified", updateTimeNowEpoch);


        try {
            boolean updateMessageResult = myDatabase.updateMessage(updateMessage);
            if (!updateMessageResult) {
                return false;
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }
    }

    /**
     * Adds a visit to the specified location based on locationID.
     *
     * @param locationID the ID of the location to add the visit to
     * @param locationVisitor the name of the visitor
     * @return true if the visit was successfully added, false otherwise
     */
    public synchronized Boolean addVisit (int locationID, String locationVisitor) {

        JSONObject visitMessage = new JSONObject();

        visitMessage.put("locationID", locationID);
        visitMessage.put("locationVisitor", locationVisitor);

        try {
            boolean result = myDatabase.updateVisitation(visitMessage);
            if (!result) {
                return false;
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


   public synchronized JSONArray getMessages() throws JSONException, SQLException, IOException {
       return myDatabase.getMessages();
   }

}
