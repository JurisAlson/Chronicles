package com.chronicle.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SupabaseService {

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SupabaseService(
            @Value("${chronicle.ai.supabase.base-url}") String baseUrl,
            @Value("${chronicle.ai.supabase.api-key}") String apiKey,
            ObjectMapper objectMapper) {

        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<JsonNode> searchHistory(
            List<Double> embedding,
            int matchCount) throws Exception {

        // Convert the embedding vector into the format expected
        // by the Supabase PostgreSQL function.
        String vector = embedding.toString();

        // Build the JSON request body.
        String json = objectMapper.writeValueAsString(
                Map.of(
                        "query_embedding", vector,
                        "match_count", matchCount
                )
        );

        // Call the Supabase RPC function to retrieve relevant
        // historical passages.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        baseUrl + "/rest/v1/rpc/search_history_chunks"
                ))
                .header("Content-Type", "application/json")
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // Send the request to Supabase.
        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        // Check whether Supabase accepted the request.
        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IllegalStateException(
                    "Supabase search failed with HTTP "
                            + response.statusCode()
                            + ": " + response.body()
            );
        }

        // Parse the response as JSON.
        JsonNode results = objectMapper.readTree(response.body());

        if (!results.isArray()) {
            throw new IllegalStateException(
                    "Supabase returned an unexpected response."
            );
        }

        // Convert the JSON array into a list of passages.
        List<JsonNode> passages = new ArrayList<>();

        for (JsonNode result : results) {
            passages.add(result);
        }

        return passages;
    }
}