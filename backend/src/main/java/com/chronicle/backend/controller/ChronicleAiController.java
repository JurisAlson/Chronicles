
package com.chronicle.backend.controller;

import com.chronicle.backend.dto.ChronicleAiRequest;
import com.chronicle.backend.dto.ChronicleAiResponse;
import com.chronicle.backend.service.ChronicleAiService;
import com.chronicle.backend.service.HistoryTopicValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chronicle-ai")
public class ChronicleAiController {

    private static final Logger log =
            LoggerFactory.getLogger(ChronicleAiController.class);

    private final ChronicleAiService chronicleAiService;
    private final HistoryTopicValidator historyTopicValidator;

    public ChronicleAiController(
            ChronicleAiService chronicleAiService,
            HistoryTopicValidator historyTopicValidator) {
        this.chronicleAiService = chronicleAiService;
        this.historyTopicValidator = historyTopicValidator;
    }

    @PostMapping("/ask")
    public ResponseEntity<ChronicleAiResponse> ask(
            @RequestBody ChronicleAiRequest request) {

        if (request == null
                || request.question() == null
                || request.question().isBlank()) {
            return ResponseEntity.badRequest().body(
                    new ChronicleAiResponse(
                            "invalid_request",
                            "Please enter a historical question.",
                            List.of()
                    )
            );
        }

        String question = request.question().trim();

        if (historyTopicValidator.isClearlyNonHistorical(question)) {
            return ResponseEntity.ok(
                    new ChronicleAiResponse(
                            "unsupported_topic",
                            "Chronicle AI currently focuses on history. "
                                    + "Try asking about a historical person, "
                                    + "event, civilization, or period.",
                            List.of()
                    )
            );
        }

        try {
            Map<String, Object> result =
                    chronicleAiService.ask(question);

            String status = (String) result.get("status");
            String answer = (String) result.get("answer");

            @SuppressWarnings("unchecked")
            List<String> citations =
                    (List<String>) result.get("citations");

            return ResponseEntity.ok(
                    new ChronicleAiResponse(status, answer, citations)
            );

        } catch (Exception exception) {
            log.error("Chronicle AI request failed", exception);

            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE
            ).body(
                    new ChronicleAiResponse(
                            "error",
                            "Chronicle AI couldn't complete the request. "
                                    + "Please try again.",
                            List.of()
                    )
            );
        }
    }
}
