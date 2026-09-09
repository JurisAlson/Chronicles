package com.chronicle.backend.service;

import com.chronicle.backend.dto.WikipediaSearchResult;
import com.chronicle.backend.dto.WikipediaSection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

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

            if (firstResult == null) {
                throw new RuntimeException(
                        "No Wikipedia results found for: " + query
                );
            }

            String title = firstResult.path("title").asText();
            long pageId = firstResult.path("pageid").asLong();

            // Step 2: Get the actual Wikipedia article
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

    public List<WikipediaSection> getWikipediaSections(long pageId) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "parse")
                        .queryParam("pageid", pageId)
                        .queryParam("prop", "sections")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode sections = root
                    .path("parse")
                    .path("sections");

            List<WikipediaSection> result = new ArrayList<>();

            for (JsonNode section : sections) {

                String title = section
                        .path("line")
                        .asText();

                int index = section
                        .path("index")
                        .asInt();

                result.add(
                        new WikipediaSection(title, index)
                );
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia sections",
                    e
            );
        }
    }
    public String getWikipediaSectionContent(long pageId, int sectionIndex) {

    String response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/w/api.php")
                    .queryParam("action", "parse")
                    .queryParam("pageid", pageId)
                    .queryParam("prop", "text")
                    .queryParam("section", sectionIndex)
                    .queryParam("format", "json")
                    .build())
            .retrieve()
            .body(String.class);

    try {
        JsonNode root = objectMapper.readTree(response);

        return root
                .path("parse")
                .path("text")
                .path("*")
                .asText();

    } catch (Exception e) {
        throw new RuntimeException(
                "Failed to parse Wikipedia section content",
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

            return page
                    .path("extract")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia article",
                    e
            );
        }
    }
}