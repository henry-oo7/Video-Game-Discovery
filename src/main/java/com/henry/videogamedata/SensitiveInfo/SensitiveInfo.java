package com.henry.videogamedata.SensitiveInfo;

public class SensitiveInfo {
    public static String getClientId() {
        // Reads from environment variable "TWITCH_CLIENT_ID"
        return System.getenv("TWITCH_CLIENT_ID");
    }
    public static String getClientSecret() {
        // Reads from environment variable "TWITCH_CLIENT_SECRET"
        return System.getenv("TWITCH_CLIENT_SECRET");
    }
}