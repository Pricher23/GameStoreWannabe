package com.example.gameshop.services;

import com.example.gameshop.models.Game;
import com.example.gameshop.models.GameDetails;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class SteamApiService {
    private static final Logger logger = Logger.getLogger(SteamApiService.class.getName());
    private static final String STEAM_API_URL = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/";
    private final String apiKey;
    private final HttpClient httpClient;

    public SteamApiService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    private String fetchUserGames(String steamId) throws Exception {
        String url = STEAM_API_URL + 
                    "?key=" + apiKey + 
                    "&steamid=" + steamId + 
                    "&format=json" +
                    "&include_appinfo=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private Game parseGameFromJson(String jsonString) {
        try {
            // Remove any leading/trailing whitespace and validate JSON format
            jsonString = jsonString.trim();
            if (!jsonString.startsWith("{")) {
                logger.warning("Invalid JSON format: " + jsonString);
                return null;
            }

            JSONObject gameJson = new JSONObject(jsonString);
            
            // Required fields - if these are missing, skip the game
            if (!gameJson.has("name") || !gameJson.has("appid")) {
                logger.warning("Skipping game: Missing required fields");
                return null;
            }

            String title = gameJson.getString("name");
            int appId = gameJson.getInt("appid");
            
            // Optional fields - use defaults if missing
            int playtimeMinutes = gameJson.optInt("playtime_forever", 0);
            
            GameDetails details = new GameDetails(
                gameJson.optString("developer", "Unknown Developer"),
                gameJson.optString("publisher", "Unknown Publisher"),
                gameJson.optString("genre", "Uncategorized"),
                gameJson.optString("description", "No description available"),
                0.0  // Default price
            );
            
            return new Game(title, details, playtimeMinutes);
        } catch (Exception e) {
            logger.warning("Failed to parse game JSON: " + e.getMessage());
            return null;
        }
    }

    public List<Game> getUserGames(String steamId) {
        List<Game> games = new ArrayList<>();
        try {
            String response = fetchUserGames(steamId);
            
            // Clean up the response
            response = response.trim();
            if (!response.startsWith("{")) {
                throw new IllegalArgumentException("Invalid API response format");
            }

            JSONObject json = new JSONObject(response);
            
            if (json.has("response") && json.getJSONObject("response").has("games")) {
                JSONArray gamesArray = json.getJSONObject("response").getJSONArray("games");
                
                for (int i = 0; i < gamesArray.length(); i++) {
                    JSONObject gameJson = gamesArray.getJSONObject(i);
                    Game game = parseGameFromJson(gameJson.toString());
                    if (game != null) {
                        games.add(game);
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Error fetching Steam games: " + e.getMessage());
        }
        return games;
    }
} 