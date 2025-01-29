package com.server;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.security.SecureRandom;
import org.apache.commons.codec.digest.Crypt;

public class MessageDatabase {

    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;
    private String dbName = "YourDB.db";

    private String preparedCreateUserTableString = "CREATE TABLE users (username VARCHAR(50) PRIMARY KEY, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, userNickname VARCHAR(50) NOT NULL, FOREIGN KEY (userNickname) REFERENCES users(userNickname))";
    private String preparedCreateMessageTableString = "CREATE TABLE messages (locationID INTEGER PRIMARY KEY, locationName VARCHAR(50) NOT NULL, locationDescription VARCHAR(50) NOT NULL, locationCity VARCHAR(50) NOT NULL, locationCountry VARCHAR(50) NOT NULL, locationStreetAddress VARCHAR(50) NOT NULL, originalPostingTime INT NOT NULL, latitude DOUBLE, longitude DOUBLE, userNickname VARCHAR(50) NOT NULL, updatereason VARCHAR(50), modified INT, timesVisited INT DEFAULT 0, weather INT DEFAULT 0, FOREIGN KEY (userNickname) REFERENCES users(userNickname))";
    private String preparedSetUsersString = "INSERT INTO users (username, password, email, userNickname) VALUES (?, ?, ?, ?)";
    private String preparedSetMessageString = "INSERT INTO messages (locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPostingTime, latitude, longitude, userNickname, weather) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private String preparedUpdateMessageString = "UPDATE messages SET locationName = ?, locationDescription = ?, locationCity = ?, locationCountry = ?, locationStreetAddress = ?, latitude = ?, longitude = ?, updatereason = ?, modified = ? WHERE locationID = ?";
    private String preparedUpdateVisitationString = "UPDATE messages SET timesVisited = timesVisited + 1 WHERE locationID = ?";
    private String preparedCheckUserString = "SELECT username from users where username = ?";
    private String preparedGetUsersString = "SELECT locationID, locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPostingTime, latitude, longitude, userNickname, updatereason, modified, weather FROM messages";
    private String preparedAuthenticateString = "SELECT password from users where username = ?";
    private String preparedGetUserNickNameString = "SELECT userNickname from users where username = ?";
    private String preparedGetMessageStringByID = "SELECT locationID, locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPostingTime, latitude, longitude, userNickname FROM messages WHERE locationID = ?";
    private String preparedGetTopFiveLocations = "SELECT locationID, locationName, timesVisited FROM messages ORDER BY timesVisited DESC LIMIT 5";

    public static synchronized MessageDatabase getInstance() {
        if (null == dbInstance) {
            dbInstance = new MessageDatabase();
        }
        return dbInstance;
    }

    public MessageDatabase() {
        try {
            File dbFile = new File(dbName);
            if (dbFile.exists() && !dbFile.isDirectory()) {
                String database = "jdbc:sqlite:" + dbName;
                dbConnection = DriverManager.getConnection(database);
            } else {
                init();
            }
        } catch (SQLException e) {
            e.getMessage();    
        }
    }

    /**
     * Initializes the MessageDatabase by establishing a connection to the database.
     * Creates predefined tables if the database is empty.
     * @return true if the initialization is successful, false otherwise.
     * @throws SQLException if there is an error in establishing the database connection.
     */
    private boolean init() throws SQLException {
        String database = "jdbc:sqlite:" + dbName;
        try {
            dbConnection = DriverManager.getConnection(database);
            if (null != dbConnection) {
                createUserTable();
                createMessageTable();
                return true;
            }
        } catch (SQLException e) {
           e.getMessage();
        }
        return false;
    }

    /**
     * Closes the database connection.
     */
    private void close() {
        try {
            if (null != dbConnection) {
                dbConnection.close();
            }
        } catch (SQLException e) {
          e.getMessage();
        }
    }

    private void createUserTable() throws SQLException {

        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedCreateUserTableString);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private void createMessageTable() throws SQLException {

        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedCreateMessageTableString);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }


    public synchronized boolean setUser(JSONObject user) throws SQLException {

        if (checkIfUserExists(user.getString("username"))) {
            return false;
        }

        String password = user.getString("password");
        String encryptedPassword = encryptPassword(password);

        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedSetUsersString);
        preparedStatement.setString(1, user.getString("username"));
        preparedStatement.setString(2, encryptedPassword);
        preparedStatement.setString(3, user.getString("email"));
        preparedStatement.setString(4, user.getString("userNickname"));

        preparedStatement.executeUpdate();
        preparedStatement.close();

        return true;
    }

    public synchronized boolean setMessage(JSONObject message) throws SQLException {

        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedSetMessageString);

        preparedStatement.setString(1, message.getString("locationName"));
        preparedStatement.setString(2, message.getString("locationDescription"));
        preparedStatement.setString(3, message.getString("locationCity"));
        preparedStatement.setString(4, message.getString("locationCountry"));
        preparedStatement.setString(5, message.getString("locationStreetAddress"));
        preparedStatement.setLong(6, message.getLong("originalPostingTime"));
        preparedStatement.setDouble(7, message.getDouble("latitude"));
        preparedStatement.setDouble(8, message.getDouble("longitude"));
        preparedStatement.setString(9, message.getString("userNickname"));
        preparedStatement.setInt(10, message.getInt("weather"));

        preparedStatement.executeUpdate();
        preparedStatement.close();
        return true;
    }

    public synchronized boolean updateMessage(JSONObject updateMessage) throws SQLException {

        int locationID = updateMessage.getInt("locationID");

        if (!checkIfMessageExists(locationID)) {
            return false;
        }

        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedUpdateMessageString);
        preparedStatement.setString(1, updateMessage.getString("locationName"));
        preparedStatement.setString(2, updateMessage.getString("locationDescription"));
        preparedStatement.setString(3, updateMessage.getString("locationCity"));
        preparedStatement.setString(4, updateMessage.getString("locationCountry"));
        preparedStatement.setString(5, updateMessage.getString("locationStreetAddress"));
        preparedStatement.setDouble(6, updateMessage.getDouble("latitude"));
        preparedStatement.setDouble(7, updateMessage.getDouble("longitude"));
        preparedStatement.setString(8, updateMessage.getString("updatereason"));
        preparedStatement.setLong(9, updateMessage.getLong("modified"));
        preparedStatement.setInt(10, locationID);

        preparedStatement.executeUpdate();
        preparedStatement.close();
        return true;
    }

    public synchronized boolean updateVisitation(JSONObject visitMessage) throws SQLException {

        int locationID = visitMessage.getInt("locationID");

        if (!checkIfMessageExists(locationID)) {
            return false;
        }

        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedUpdateVisitationString);
        preparedStatement.setInt(1, locationID);

        preparedStatement.executeUpdate();
        preparedStatement.close();
        return true;
    }

    public synchronized JSONArray getMessages() throws SQLException, JSONException, IOException {
        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedGetUsersString);
        ResultSet rs = preparedStatement.executeQuery();
        JSONArray messageArray = new JSONArray();

        while (rs.next()) {
            JSONObject message = new JSONObject();

            long epochTime = rs.getLong("originalPostingTime");
            String zonedTime = TimeTools.convertEpochToZoneTime(epochTime);

            message.put("locationID", rs.getInt("locationID"));
            message.put("locationName", rs.getString("locationName"));
            message.put("locationDescription", rs.getString("locationDescription"));
            message.put("locationCity", rs.getString("locationCity"));
            message.put("locationCountry", rs.getString("locationCountry"));
            message.put("locationStreetAddress", rs.getString("locationStreetAddress"));
            message.put("originalPoster", rs.getString("userNickname"));
            message.put("originalPostingTime", zonedTime);

            if (rs.getDouble("latitude") != 0.0 && rs.getDouble("longitude") != 0.0) {
                Double latitude = rs.getDouble("latitude");
                Double longitude = rs.getDouble("longitude");

                message.put("latitude", latitude);
                message.put("longitude", longitude);

                if (rs.getInt("weather") != 0) {
                    WeatherClient weatherClient = new WeatherClient();
                    int weather = weatherClient.getWeather(latitude, longitude);
                    message.put("weather", weather);
                }   

            }

            if (rs.getString("updatereason") != null) {
                message.put("updatereason", rs.getString("updatereason"));
                long modifiedTime = rs.getLong("modified");
                String zonedModifiedTime = TimeTools.convertEpochToZoneTime(modifiedTime);
                message.put("modified", zonedModifiedTime);
            }

            messageArray.put(message);
        }

        rs.close();
        preparedStatement.close();
        return messageArray;
    }

    public synchronized JSONArray getTopFiveLocations() throws SQLException, JSONException, IOException {
        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedGetTopFiveLocations);
        ResultSet rs = preparedStatement.executeQuery();
        JSONArray messageArray = new JSONArray();

        while (rs.next()) {
            JSONObject message = new JSONObject();

            message.put("locationID", rs.getInt("locationID"));
            message.put("locationName", rs.getString("locationName"));
            message.put("timesVisited", rs.getInt("timesVisited"));

            messageArray.put(message);
        }

        rs.close();
        preparedStatement.close();
        return messageArray;
    }

    public synchronized boolean checkIfUserExists(String givenUserName) throws SQLException {

        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedCheckUserString);
        preparedStatement.setString(1, givenUserName);

        ResultSet rs = preparedStatement.executeQuery();
        boolean userExists = rs.next();
        rs.close();
        preparedStatement.close();
        return userExists;
    }

    public synchronized boolean checkIfMessageExists(int locationID) throws SQLException {

        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedGetMessageStringByID);
        preparedStatement.setInt(1, locationID);

        ResultSet rs = preparedStatement.executeQuery();
        boolean messageExists = rs.next();
        rs.close();
        preparedStatement.close();
        return messageExists;
    }

    public synchronized boolean authenticateUser(String giveUserName, String givenPlainPassword) throws SQLException {

        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedAuthenticateString);
        preparedStatement.setString(1, giveUserName);

        ResultSet rs = preparedStatement.executeQuery();
        boolean userExists = rs.next();
        if (!userExists) {
            rs.close();
            preparedStatement.close();
            return false;
        }
        String cryptedPasswordDB = rs.getString("password");
        rs.close();
        preparedStatement.close();
        return decryptPassword(givenPlainPassword, cryptedPasswordDB);
    }

    public synchronized String getUserNickname(String username) throws SQLException {
        PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedGetUserNickNameString);
        preparedStatement.setString(1, username);

        String userNickname = "";

        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            userNickname = rs.getString("userNickname");
        }
        rs.close();
        preparedStatement.close();
        return userNickname;
    }

    /**
     * Encrypts the given plain password using a randomly generated salt and the Crypt.crypt() method.
     * 
     * @param givenPlainPassword the plain password to be encrypted
     * @return the encrypted password
     */
    private String encryptPassword(String givenPlainPassword) {
        SecureRandom secureRandom = new SecureRandom();

        byte bytes[] = new byte[13];
        secureRandom.nextBytes(bytes);
        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes;

        String encryptedPassword = Crypt.crypt(givenPlainPassword, salt);

        return encryptedPassword;
    }

    /**
     * Decrypts the given plain password and compares it with the crypted password stored in the database.
     * 
     * @param givenPlainPassword The plain password provided by the user.
     * @param cryptedPasswordDB The crypted password stored in the database.
     * @return true if the decrypted password matches the crypted password, false otherwise.
     */
    private boolean decryptPassword(String givenPlainPassword, String cryptedPasswordDB) {
        return (cryptedPasswordDB.equals(Crypt.crypt(givenPlainPassword, cryptedPasswordDB)));
    }

}
