package com.chronicle.backend.controller;

import com.chronicle.backend.dto.WikipediaSearchResult;
import com.chronicle.backend.service.WikipediaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}