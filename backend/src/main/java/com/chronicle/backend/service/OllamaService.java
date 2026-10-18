
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
public class OllamaService {

    private final String baseUrl;
    private final String embeddingModel;
    private final String generationModel;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaService(
            @Value("${chronicle.ai.ollama.base-url}") String baseUrl,
            @Value("${chronicle.ai.ollama.embedding-model}") String embeddingModel,
            @Value("${chronicle.ai.ollama.generation-model}") String generationModel,
            ObjectMapper objectMapper) {

        this.baseUrl = baseUrl;
        this.embeddingModel = embeddingModel;
        this.generationModel = generationModel;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<Double> embed(String text) throws Exception {
        String json = objectMapper.writeValueAsString(
                Map.of(
                        "model", embeddingModel,
                        "input", text
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/embed"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Ollama embedding request failed: " + response.body()
            );
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode embedding = root.path("embeddings").path(0);

        if (!embedding.isArray()) {
            throw new IllegalStateException(
                    "Ollama did not return an embedding vector."
            );
        }

        List<Double> vector = new ArrayList<>();

        for (JsonNode value : embedding) {
            vector.add(value.asDouble());
        }

        return vector;
    }

    public String generate(String prompt) throws Exception {
        String json = objectMapper.writeValueAsString(
                Map.of(
                        "model", generationModel,
                        "prompt", prompt,
                        "stream", false
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Ollama generation request failed: " + response.body()
            );
        }

        JsonNode root = objectMapper.readTree(response.body());
        String answer = root.path("response").asText();

        if (answer.isBlank()) {
            throw new IllegalStateException(
                    "Ollama returned an empty response."
            );
        }

        return answer;
    }
}
