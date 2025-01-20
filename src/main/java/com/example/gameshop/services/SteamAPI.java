package com.example.gameshop.services;

import com.example.gameshop.models.Game;
import com.example.gameshop.models.GameDetails;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SteamAPI {
    private static final String API_KEY = "your_steam_api_key_here";
    private static final String BASE_URL = "http://api.steampowered.com";
    
    public List<Game> getOwnedGames(String steamId) throws IOException, InterruptedException {
        String url = String.format("%s/IPlayerService/GetOwnedGames/v0001/?key=%s&steamid=%s&format=json&include_appinfo=1",
                BASE_URL, API_KEY, steamId);
                
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
                
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        
        // Add error checking for empty or invalid response
        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new IOException("Empty response from Steam API");
        }
        
        try {
            JSONObject json = new JSONObject(responseBody);
            List<Game> games = new ArrayList<>();
            
            if (json.has("response") && json.getJSONObject("response").has("games")) {
                JSONArray gamesArray = json.getJSONObject("response").getJSONArray("games");
                for (int i = 0; i < gamesArray.length(); i++) {
                    JSONObject gameObj = gamesArray.getJSONObject(i);
                    try {
                        GameDetails details = getGameDetails(gameObj.getInt("appid"));
                        Game game = new Game(
                            gameObj.getString("name"),
                            details,
                            gameObj.optInt("playtime_forever", 0)
                        );
                        game.setAppId(gameObj.getInt("appid"));
                        games.add(game);
                    } catch (Exception e) {
                        System.err.println("Failed to load game: " + gameObj.toString() + "\nError: " + e.getMessage());
                        // Continue loading other games even if one fails
                        continue;
                    }
                }
            }
            return games;
        } catch (JSONException e) {
            throw new IOException("Invalid JSON response from Steam API: " + e.getMessage() + "\nResponse: " + responseBody);
        }
    }
    
    private GameDetails getGameDetails(int appId) throws IOException, InterruptedException {
        String url = String.format("https://store.steampowered.com/api/appdetails?appids=%d", appId);
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
                
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        
        if (json.has(String.valueOf(appId))) {
            JSONObject appData = json.getJSONObject(String.valueOf(appId));
            if (appData.has("data") && appData.optBoolean("success", false)) {
                JSONObject data = appData.getJSONObject("data");
                return new GameDetails(
                    data.optString("developer", "Unknown Developer"),
                    data.optString("publisher", "Unknown Publisher"),
                    data.has("genres") && data.getJSONArray("genres").length() > 0 
                        ? data.getJSONArray("genres").getJSONObject(0).optString("description", "Unknown Genre")
                        : "Unknown Genre",
                    data.optString("detailed_description", "No description available"),
                    data.has("price_overview") 
                        ? data.getJSONObject("price_overview").optDouble("final", 0.0) / 100.0
                        : 0.0
                );
            }
        }
        
        return new GameDetails(
            "Unknown Developer",
            "Unknown Publisher",
            "Unknown Genre",
            "No description available",
            0.0
        );
    }
} 