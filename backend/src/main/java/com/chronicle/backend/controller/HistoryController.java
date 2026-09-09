package com.chronicle.backend.controller;

import com.chronicle.backend.dto.WikipediaSearchResult;
import com.chronicle.backend.service.WikipediaService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Temporary controller for testing purposes. This will be removed in the future when the frontend is implemented.
import com.chronicle.backend.dto.WikipediaSection;

@RestController
public class HistoryController {

    private final WikipediaService wikipediaService;

    public HistoryController(WikipediaService wikipediaService) {
        this.wikipediaService = wikipediaService;
    }

    @GetMapping("/api/history/search")
    public WikipediaSearchResult search(@RequestParam String query) {
        return wikipediaService.searchWikipedia(query);
    }

    // Temporary controller for testing purposes. This will be removed in the future when the frontend is implemented.
    @GetMapping("/api/history/sections")
    public List<WikipediaSection> sections(@RequestParam long pageId) {
    return wikipediaService.getWikipediaSections(pageId);
    }

    @GetMapping("/api/history/section")
    public String section(
            @RequestParam long pageId,
            @RequestParam int sectionIndex
    ) {
        return wikipediaService.getWikipediaSectionContent(
                pageId,
                sectionIndex
        );
    }
}