package com.chronicle.backend.dto;

public record WikipediaSearchOption(
        String title,
        long pageId,
        String description
) {
}