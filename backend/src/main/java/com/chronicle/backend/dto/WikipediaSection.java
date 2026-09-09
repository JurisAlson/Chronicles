package com.chronicle.backend.dto;

public class WikipediaSection {

    private String title;
    private int index;

    public WikipediaSection() {
    }

    public WikipediaSection(String title, int index) {
        this.title = title;
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public int getIndex() {
        return index;
    }
}