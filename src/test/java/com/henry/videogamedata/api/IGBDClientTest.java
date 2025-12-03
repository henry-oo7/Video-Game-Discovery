package com.henry.videogamedata.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;




public class IGBDClientTest {

    @Test
    public void shouldFetchOneGame() throws InterruptedException, IOException {
        IGDBClient igdbClient = new IGDBClient();
        String testBody = "fields id, name, genres.name, platforms.name;limit 1;offset 0;";
        String testResponse = igdbClient.executePostRequest("games", testBody);
        System.out.println(testResponse);
        Assertions.assertNotNull(testResponse);
    }
}
