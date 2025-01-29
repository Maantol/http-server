package com.server;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeTools {

    /**
     * Converts a zone time string to epoch timestamp.
     *
     * @param time the zone time string to be converted
     * @return the epoch timestamp in milliseconds
     */
    public static long convertZoneTimeToEpoch(String time) {

        ZonedDateTime zonedTime = ZonedDateTime.parse(time);
        long epochTimestamp = zonedTime.toInstant().toEpochMilli();
        return epochTimestamp;
    }

    /**
     * Converts an epoch timestamp to a formatted string representing the
     * corresponding zone time.
     *
     * @param epoch the epoch timestamp to convert
     * @return a formatted string representing the zone time
     */
    public static String convertEpochToZoneTime(long epoch) {

        ZonedDateTime conversionTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        return conversionTime.format(formatter);
    }


    /**
     * Returns the current time in epoch format.
     *
     * @return the current time in epoch format
     */
    public static long currentTimeinEpoch() {
        return System.currentTimeMillis();
    }

    /**
     * Checks if a given time string is valid.
     *
     * @param time the time string to be checked
     * @return true if the time string is valid, false otherwise
     */
    public synchronized static boolean isGivenTimeValid(String time) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            ZonedDateTime.parse(time, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}