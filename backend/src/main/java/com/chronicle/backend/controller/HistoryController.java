package com.chronicle.backend.controller;

import com.chronicle.backend.dto.WikipediaSearchOption;
import com.chronicle.backend.dto.WikipediaSection;
import com.chronicle.backend.service.WikipediaService;
import com.chronicle.backend.dto.WikipediaRelatedPage;

import java.util.List;

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
    public List<WikipediaSearchOption> search(
            @RequestParam String query
    ) {
        return wikipediaService.searchWikipedia(query);
    }

    @GetMapping("/api/history/sections")
    public List<WikipediaSection> sections(
            @RequestParam long pageId
    ) {
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

    @GetMapping("/api/history/article")
    public String article(
            @RequestParam long pageId
    ) {
        return wikipediaService.getWikipediaArticle(pageId);
    }
    @GetMapping("/api/history/related")
    public List<WikipediaRelatedPage> related(
            @RequestParam long pageId
    ) {
        return wikipediaService.getRelatedPages(pageId);
    }
    
}