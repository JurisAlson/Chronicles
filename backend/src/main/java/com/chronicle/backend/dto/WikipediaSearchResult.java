package com.chronicle.backend.dto;

public class WikipediaSearchResult {

    private String title;
    private long pageId;
    private String extract;

    public WikipediaSearchResult() {
    }

    public WikipediaSearchResult(String title, long pageId, String extract) {
        this.title = title;
        this.pageId = pageId;
        this.extract = extract;
    }

    public String getTitle() {
        return title;
    }

    public long getPageId() {
        return pageId;
    }

    public String getExtract() {
        return extract;
    }
}