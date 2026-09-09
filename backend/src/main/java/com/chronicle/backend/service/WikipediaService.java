package com.chronicle.backend.service;

import com.chronicle.backend.dto.WikipediaSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WikipediaService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WikipediaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl("https://en.wikipedia.org")
                .defaultHeader(
                        "User-Agent",
                        "Chronicle/1.0 (historical reference project)"
                )
                .build();
    }

    public WikipediaSearchResult searchWikipedia(String query) {

        // Step 1: Search Wikipedia
        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "query")
                        .queryParam("list", "search")
                        .queryParam("srsearch", query)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode firstResult = root
                    .path("query")
                    .path("search")
                    .get(0);

            String title = firstResult.path("title").asText();
            long pageId = firstResult.path("pageid").asLong();

            // Step 2: Get the actual Wikipedia introduction
            String extract = getWikipediaArticle(pageId);

            return new WikipediaSearchResult(
                    title,
                    pageId,
                    extract
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia search response",
                    e
            );
        }
    }

    public String getWikipediaArticle(long pageId) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "query")
                        .queryParam("prop", "extracts")
                        .queryParam("exintro", "true")
                        .queryParam("explaintext", "true")
                        .queryParam("pageids", pageId)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode page = root
                    .path("query")
                    .path("pages")
                    .path(String.valueOf(pageId));

            return page.path("extract").asText();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia article",
                    e
            );
        }
    }
}