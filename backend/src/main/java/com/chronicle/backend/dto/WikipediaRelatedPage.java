package com.chronicle.backend.dto;

public record WikipediaRelatedPage(
        String title,
        long pageId,
        String description
) {
}