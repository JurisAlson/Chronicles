
package com.chronicle.backend.service;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class HistoryTopicValidator {

    private static final Pattern NON_HISTORY_TERMS = Pattern.compile(
            "\\b("
            + "spring boot|react|javascript|python|java code|"
            + "programming|software development|debugging|"
            + "recipe|cooking|workout|stock price|weather"
            + ")\\b",
            Pattern.CASE_INSENSITIVE
    );

    public boolean isClearlyNonHistorical(String question) {
        if (question == null || question.isBlank()) {
            return true;
        }

        String normalized = question.toLowerCase(Locale.ROOT).trim();

        return NON_HISTORY_TERMS.matcher(normalized).find();
    }
}
  