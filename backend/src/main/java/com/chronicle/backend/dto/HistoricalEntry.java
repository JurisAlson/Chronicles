package com.chronicle.backend.dto;

public class HistoricalEntry {

    private String title;
    private String overview;
    private String earlyLife;
    private String majorEvents;
    private String death;
    private String legacy;

    public HistoricalEntry() {
    }

    public HistoricalEntry(
            String title,
            String overview,
            String earlyLife,
            String majorEvents,
            String death,
            String legacy
    ) {
        this.title = title;
        this.overview = overview;
        this.earlyLife = earlyLife;
        this.majorEvents = majorEvents;
        this.death = death;
        this.legacy = legacy;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getEarlyLife() {
        return earlyLife;
    }

    public String getMajorEvents() {
        return majorEvents;
    }

    public String getDeath() {
        return death;
    }

    public String getLegacy() {
        return legacy;
    }
}