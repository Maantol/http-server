package com.server;

import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;

public class TopFiveLocations {

    private final MessageDatabase myDatabase = MessageDatabase.getInstance();

    public TopFiveLocations() {
    }

    public JSONArray getMessages()  throws JSONException, SQLException, IOException {
        return myDatabase.getTopFiveLocations();
    }
}  
