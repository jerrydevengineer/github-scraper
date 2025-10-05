package org.example.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

public class App {
    // The base URL for the GitHub trending page
    private static final String GITHUB_TRENDING_URL = "https://github.com/trending/";

    public static void main(String[] args) {
        try {
            // 1. Read the language from the environment variable
            String language = System.getenv("TARGET_LANGUAGE");
            String timeframe = System.getenv("TARGET_TIMEFRAME");

            if (language == null || language.trim().isEmpty()) {
                language = "java"; // Default to "java" if not set
                System.out.println("TARGET_LANGUAGE not set. Defaulting to 'java'.");
            }

            if (timeframe == null || timeframe.isEmpty()) {
                timeframe = "daily"; // Default to "daily"
                System.out.println("TARGET_TIMEFRAME not set. Defaulting to 'daily'.");
            }

            System.out.println("Scraping GitHub trending for language: " + language);

            // 2. Build the full URL to scrape
            String scrapeUrl = GITHUB_TRENDING_URL + language + "?since=" + timeframe;

            // 3. Use Jsoup to connect and get the HTML document
            Document doc = Jsoup.connect(scrapeUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .get();

            // 4. Select all repository rows from the page
            // Each trending repository is listed in an <article class="Box-row">
            Elements repoRows = doc.select("article.Box-row");

            // 5. Build the Markdown string
            StringBuilder markdown = new StringBuilder();
            markdown.append("# Top Trending ").append(capitalize(language)).append(" Repositories - ").append(LocalDate.now()).append("\n\n");
            markdown.append("| Rank | Name | Stars | Description |\n");
            markdown.append("|------|------|-------|-------------|\n");

            int rank = 1;
            for (Element row : repoRows) {
                // Extract repository name and URL from the <h2><a> tag
                Element titleElement = row.select("h2 a").first();
                String repoName = titleElement.text(); // e.g., "owner / repo-name"
                String repoUrl = "https://github.com" + titleElement.attr("href");

                // Extract description from the <p> tag
                Element descriptionElement = row.select("p").first();
                String description = (descriptionElement != null) ? descriptionElement.text() : "No description available.";

                // Extract stars. It's in an <a> tag that contains "/stargazers" in its href
                Element starElement = row.select("a[href*='/stargazers']").first();
                String stars = (starElement != null) ? starElement.text().trim() : "N/A";

                markdown.append("| ").append(rank++)
                        .append(" | [").append(repoName).append("](").append(repoUrl).append(")")
                        .append(" | ").append(stars)
                        .append(" | ").append(description).append(" |\n");
            }

            // 6. Write the string to a file
            Files.writeString(Paths.get("TRENDING.md"), markdown.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Successfully generated trending report: TRENDING.md");

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

