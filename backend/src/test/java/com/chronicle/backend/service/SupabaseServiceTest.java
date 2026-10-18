
package com.chronicle.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupabaseServiceTest {

    @Test
    void shouldRetrieveHistoricalPassagesFromSupabase() throws Exception {
        String baseUrl = System.getenv("CHRONICLE_SUPABASE_URL");
        String apiKey = System.getenv("SUPABASE_API_KEY");

        assertNotNull(baseUrl, "Set CHRONICLE_SUPABASE_URL first.");
        assertNotNull(apiKey, "Set SUPABASE_API_KEY first.");

        OllamaService ollamaService = new OllamaService(
                "http://localhost:11434",
                "nomic-embed-text:latest",
                "llama3.2:1b",
                new ObjectMapper()
        );

        SupabaseService supabaseService = new SupabaseService(
                baseUrl,
                apiKey,
                new ObjectMapper()
        );

        List<Double> embedding = ollamaService.embed(
                "How did Julius Caesar's assassination lead to the rise of Augustus?"
        );

        List<JsonNode> passages = supabaseService.searchHistory(
                embedding,
                5
        );

        System.out.println("Retrieved passages: " + passages.size());

        for (JsonNode passage : passages) {
            System.out.println(
                    passage.path("source_title").asText()
                            + " | similarity: "
                            + passage.path("similarity").asDouble()
            );
        }

        assertFalse(passages.isEmpty(),
                "Supabase should return at least one passage.");
    }
}
