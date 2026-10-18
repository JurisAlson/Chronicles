
package com.chronicle.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OllamaServiceTest {

    private OllamaService createService() {
        return new OllamaService(
                "http://localhost:11434",
                "nomic-embed-text:latest",
                "llama3.2:1b",
                new ObjectMapper()
        );
    }

    @Test
    void shouldGenerateAnswerUsingLocalOllama() throws Exception {
        OllamaService ollamaService = createService();

        String answer = ollamaService.generate(
                "In one sentence, who was Julius Caesar?"
        );

        System.out.println("Ollama answer: " + answer);

        assertNotNull(answer);
        assertFalse(answer.isBlank());
    }

    @Test
    void shouldCreateEmbeddingUsingLocalOllama() throws Exception {
        OllamaService ollamaService = createService();

        List<Double> embedding = ollamaService.embed(
                "Julius Caesar was a Roman general."
        );

        System.out.println("Embedding dimensions: " + embedding.size());

        assertNotNull(embedding);
        assertEquals(768, embedding.size());
    }
}
