package org.example.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

public class App {
    // The URL for the API
//    private static final String API_URL = "https://api.gitterapp.com/repositories?language=java&since=daily";
//    private static final String API_BASE_URL = "https://api.g-h-t.de/repositories";

    public static void main(String[] args) throws IOException {
        // 1. Read the language from the environment variable
        String language = System.getenv("TARGET_LANGUAGE");
        String timeframe = System.getenv("TARGET_TIMEFRAME");

        // Add a fallback for local testing in case the variable isn't set
        if (language == null || language.isEmpty()) {
            language = "java"; // Default to "java"
            System.out.println("TARGET_LANGUAGE not set. Defaulting to 'java'.");
        }

        if (timeframe == null || timeframe.isEmpty()) {
            timeframe = "daily"; // Default to "daily"
            System.out.println("TARGET_TIMEFRAME not set. Defaulting to 'daily'.");
        }

        System.out.println("Fetching trending repositories for language: " + language);

        // 2. Build the API URL dynamically
        String apiUrl = "https://api.gitterapp.com/repositories?language=" + language + "&since=" + timeframe;
//        String apiUrl = API_URL + "?language=" + language + "&since=" + timeframe;

        // 3. Fetch the data from the API
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(apiUrl).build();
        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();

        // 4. Parse the JSON data
        ObjectMapper mapper = new ObjectMapper();
        JsonNode repos = mapper.readTree(jsonData);

        // 5. Build a Markdown string
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Top Trending Java Repositories - ").append(LocalDate.now()).append("\n\n");
        markdown.append("| Rank | Name | Stars | Description |\n");
        markdown.append("|------|------|-------|-------------|\n");

        int rank = 1;
        for (JsonNode repo : repos) {
            String name = repo.get("name").asText();
            String owner = repo.get("owner").get("login").asText();
            String url = repo.get("url").asText();
            String stars = repo.get("stars").asText();
            String description = repo.get("description").asText().replace("\n", " ").trim();

            markdown.append("| ").append(rank++)
                    .append(" | [").append(owner).append("/").append(name).append("](").append(url).append(")")
                    .append(" | ").append(stars)
                    .append(" | ").append(description).append(" |\n");
        }

        // 6. Write the string to a file
        Files.writeString(Paths.get("TRENDING.md"), markdown.toString());
        System.out.println("Successfully generated trending report.");
    }
}
