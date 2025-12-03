package com.henry.videogamedata.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henry.videogamedata.Entity.VideoGame;
import com.henry.videogamedata.SensitiveInfo.SensitiveInfo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class IGDBClient {
    //TOP SECRET information stored somewhere else and retrieved
    private final String CLIENT_ID = SensitiveInfo.getClientId();
    private final String CLIENT_SECRET = SensitiveInfo.getClientSecret();

    ObjectMapper objectMapper;

    public IGDBClient() {
        objectMapper = new ObjectMapper();
    }

    public String refreshAccessToken() {
        //the Set-up
        /*
        What it does: This creates the actual set up string we are sending to Twitch
        Why: Twitch expects data in the format of 'key=value&key=value'.
             We are manually stitching our ID, secret, and grant type into this string
        */
        String postRequestFormat = "client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&grant_type=client_credentials";
        //building the request
        /*
        .URI is the destination we want to get to
        .header tells the twitch servers that our message is in the form of URL-encoded form
        .POST attaches the payload, what we set up before
        .build finishes the build and stores a HttpRequest object in request
         */
        String TWITCH_AUTH_URL = "https://id.twitch.tv/oauth2/token";
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(TWITCH_AUTH_URL))
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(postRequestFormat))
                .build();
        //Preparing the client
        /*
        What it does: Creates the object responsible for sending the message through the internet
        BodyHandler is the instruction manual for the client (our server)
                    when a response comes back to turn those bytes to String text
         */
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();

        //The execution (the delivery)
        /*
        This is the moment when the package is actually sent and our server is waiting for a response
        objectMapper takes the JSON string and turns it into a java object we can interact with
        jsonNode.get(access_token) reads the json object and returns just the text we need
         */
        try {
            HttpResponse<String> response = client.send(request, responseBodyHandler);
            JsonNode jsonNode = objectMapper.readTree(response.body());
            return jsonNode.get("access_token").asText();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Login failed", e);
        }
    }

    //the method that starts it all, sends the actual request to the igdb and returns a JSON STRING that needs to be parsed
    public String executePostRequest(String endpoint, String requestBody) {

        //the request builder
        String BASE_IGDB_URL = "https://api.igdb.com/v4";
        HttpRequest request = HttpRequest
                .newBuilder(URI.create(BASE_IGDB_URL + "/" + endpoint))
                .header("Client-ID", SensitiveInfo.getClientId())
                .header("Authorization", "Bearer " + refreshAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        //the sender
        HttpClient client = HttpClient.newHttpClient();

        //the one that handles the incoming response
        HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();

        //the actual sending
        try {
            HttpResponse<String> response = client.send(request, responseBodyHandler);

            if (response.statusCode() != 200) {

                System.err.println("IGDB Error: " + response.body());
                throw new RuntimeException("IGDB API returned status: " + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //builds the body, runes the postrequest method, uses jackson object mapper to return a list of our video game objects
    public List<VideoGame> fetchGamesBatch(long updatedAt) throws JsonProcessingException {

        String body = "fields id, name, genres.name, platforms.name, updated_at, cover.url, total_rating, first_release_date, total_rating_count;" +
                " limit 500;" +
                " offset 0;" +
                " where updated_at > " + updatedAt + ";" +
                " sort updated_at asc;";

        String response = executePostRequest("games", body);

        List<VideoGame> gamesToBeAdded = objectMapper.readValue(response, new TypeReference<List<VideoGame>>() {
        });
        List<VideoGame> games = new ArrayList<>(gamesToBeAdded);

        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return games;
    }
}
