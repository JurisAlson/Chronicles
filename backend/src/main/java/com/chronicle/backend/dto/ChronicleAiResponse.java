
package com.chronicle.backend.dto;

import java.util.List;

public record ChronicleAiResponse(
        String status,
        String answer,
        List<String> citations
) {
}
