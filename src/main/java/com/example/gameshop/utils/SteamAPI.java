package com.example.gameshop.utils;

import com.example.gameshop.models.Game;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SteamAPI {
    private static final String API_KEY = "B48783AA1FB9AE58728983E400007FF7"; // Replace with your Steam API key
    private static final String STEAM_API_URL = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/";

    public static List<Game> getUserGames(String steamId) throws IOException, InterruptedException {
        String url = STEAM_API_URL + "?key=" + API_KEY + "&steamid=" + steamId + "&include_appinfo=1&format=json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch Steam games: " + response.statusCode());
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray games = json.getJSONObject("response").getJSONArray("games");
        List<Game> steamGames = new ArrayList<>();

        for (int i = 0; i < games.length(); i++) {
            JSONObject gameJson = games.getJSONObject(i);
            Game game = new Game(
                gameJson.getString("name"),
                "", // Description will be fetched separately if needed
                0.0 // Price not available from this API
            );
            game.setAppId(gameJson.getInt("appid"));
            game.setPlaytime(gameJson.getInt("playtime_forever"));
            steamGames.add(game);
        }

        return steamGames;
    }
} 