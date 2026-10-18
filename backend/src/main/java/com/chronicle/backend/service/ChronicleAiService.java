
package com.chronicle.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChronicleAiService {

    private static final int MATCH_COUNT = 5;

    // Initial threshold for testing; we'll tune this with real questions.
    private static final double MIN_SIMILARITY = 0.50;

    private final OllamaService ollamaService;
    private final SupabaseService supabaseService;

    public ChronicleAiService(
            OllamaService ollamaService,
            SupabaseService supabaseService) {
        this.ollamaService = ollamaService;
        this.supabaseService = supabaseService;
    }

    public Map<String, Object> ask(String question) throws Exception {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a historical question."
            );
        }

        // 1. Convert the question into an embedding.
        List<Double> embedding = ollamaService.embed(question);

        // 2. Retrieve matching passages from Supabase.
        List<JsonNode> results =
                supabaseService.searchHistory(embedding, MATCH_COUNT);

                
System.out.println("\n========== RAG RETRIEVAL DEBUG ==========");
System.out.println("Question: " + question);
System.out.println("Retrieved passages: " + results.size());

for (int i = 0; i < results.size(); i++) {
    JsonNode result = results.get(i);

    System.out.println("\n--- Retrieved Passage " + (i + 1) + " ---");
    System.out.println("Title: "
            + result.path("source_title").asText("MISSING"));
    System.out.println("Similarity: "
            + result.path("similarity").asDouble(-1));
    System.out.println("Content: "
            + result.path("content").asText("MISSING"));
}

System.out.println("========================================\n");


        // 3. Keep passages that meet our initial similarity threshold.
        List<JsonNode> relevantPassages = new ArrayList<>();

        for (JsonNode result : results) {
            double similarity = result.path("similarity").asDouble(-1);

            if (similarity >= MIN_SIMILARITY
                    && !result.path("content").asText().isBlank()) {
                relevantPassages.add(result);
            }
        }

        // Don't generate an answer without supporting passages.
        if (relevantPassages.isEmpty()) {
            return response(
                    "insufficient_evidence",
                    "I couldn't find sufficiently relevant historical sources "
                            + "to answer that question yet. Try another "
                            + "question or a more specific historical topic.",
                    List.of()
            );
        }

        // 4. Prepare the evidence and citations.
        StringBuilder context = new StringBuilder();
        Map<String, String> citations = new LinkedHashMap<>();

        for (int i = 0; i < relevantPassages.size(); i++) {
            JsonNode passage = relevantPassages.get(i);

            String title = passage.path("source_title").asText();
            String url = passage.path("source_url").asText();
            String content = passage.path("content").asText();
            String citationId = "[S" + (i + 1) + "]";

            context.append(citationId)
                    .append(" Title: ").append(title)
                    .append("\nURL: ").append(url)
                    .append("\nPassage: ").append(content)
                    .append("\n\n");

            citations.put(citationId, title + " — " + url);
        }

        // 5. Ask the model to answer using only the retrieved evidence.
        String prompt = """
                You are Chronicle AI, a historical research assistant.

                Answer the user's question using ONLY the source passages
                provided below.

                Rules:
                - Do not invent historical facts or unsupported details.
                - If the sources do not establish an important part of the
                  answer, clearly say what the evidence does not establish.
                - Cite factual claims using the source labels exactly as
                  written, for example [S1] or [S2].
                - Do not invent citations or sources.
                - Write a clear, concise answer.

                SOURCE PASSAGES:
                %s

                USER QUESTION:
                %s

                ANSWER:
                """.formatted(context, question);

        String answer = ollamaService.generate(prompt);

        // 6. Return the answer and its available source references.
        return response(
                "success",
                answer,
                new ArrayList<>(citations.values())
        );
    }

    private Map<String, Object> response(
            String status,
            String answer,
            List<String> citations) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status);
        result.put("answer", answer);
        result.put("citations", citations);
        return result;
    }
}
