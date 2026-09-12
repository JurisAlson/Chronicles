
package com.chronicle.backend.service;

import com.chronicle.backend.dto.WikipediaRelatedPage;
import com.chronicle.backend.dto.WikipediaSearchOption;
import com.chronicle.backend.dto.WikipediaSection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WikipediaService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WikipediaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl("https://en.wikipedia.org")
                .defaultHeader(
                        "User-Agent",
                        "Chronicle/1.0 (historical reference project)"
                )
                .build();
    }

    public List<WikipediaSearchOption> searchWikipedia(String query) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "query")
                        .queryParam("list", "search")
                        .queryParam("srsearch", query)
                        .queryParam("srlimit", 8)
                        .queryParam("srprop", "snippet")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode searchResults = root
                    .path("query")
                    .path("search");

            List<WikipediaSearchOption> results = new ArrayList<>();

            for (JsonNode result : searchResults) {

                String title = result
                        .path("title")
                        .asText();

                long pageId = result
                        .path("pageid")
                        .asLong();

                String description = result
                        .path("snippet")
                        .asText();

                description = description
                        .replaceAll("<[^>]*>", "")
                        .replace("&quot;", "\"")
                        .replace("&#39;", "'")
                        .replace("&amp;", "&")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">");

                results.add(
                        new WikipediaSearchOption(
                                title,
                                pageId,
                                description
                        )
                );
            }

            return results;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia search response",
                    e
            );
        }
    }

    public List<WikipediaSection> getWikipediaSections(long pageId) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "parse")
                        .queryParam("pageid", pageId)
                        .queryParam("prop", "sections")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode sections = root
                    .path("parse")
                    .path("sections");

            List<WikipediaSection> result = new ArrayList<>();

            for (JsonNode section : sections) {

                String title = section
                        .path("line")
                        .asText();

                int index = section
                        .path("index")
                        .asInt();

                result.add(
                        new WikipediaSection(title, index)
                );
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia sections",
                    e
            );
        }
    }

    public String getWikipediaSectionContent(
            long pageId,
            int sectionIndex
    ) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "parse")
                        .queryParam("pageid", pageId)
                        .queryParam("prop", "text")
                        .queryParam("section", sectionIndex)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);

            return root
                    .path("parse")
                    .path("text")
                    .path("*")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia section content",
                    e
            );
        }
    }

    public String getWikipediaArticle(long pageId) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "query")
                        .queryParam("prop", "extracts")
                        .queryParam("exintro", "true")
                        .queryParam("explaintext", "true")
                        .queryParam("pageids", pageId)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode page = root
                    .path("query")
                    .path("pages")
                    .path(String.valueOf(pageId));

            return page
                    .path("extract")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia article",
                    e
            );
        }
    }

    public List<WikipediaRelatedPage> getRelatedPages(long pageId) {

        /*
         * ============================================================
         * 1. GET THE CURRENT ARTICLE
         * ============================================================
         */

        String currentResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "query")
                        .queryParam("pageids", pageId)
                        .queryParam("prop", "info|extracts")
                        .queryParam("exintro", "true")
                        .queryParam("explaintext", "true")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        try {

            JsonNode currentRoot =
                    objectMapper.readTree(currentResponse);

            JsonNode currentPage = currentRoot
                    .path("query")
                    .path("pages")
                    .path(String.valueOf(pageId));

            String currentTitle = currentPage
                    .path("title")
                    .asText();

            String currentDescription = currentPage
                    .path("extract")
                    .asText();

            /*
             * ========================================================
             * 2. GET DIRECTLY LINKED PAGES
             * ========================================================
             *
             * Wikipedia returns linked pages in batches.
             *
             * We now follow the continuation token and collect up
             * to 300 candidates instead of stopping at the first 100.
             *
             * This reduces the alphabetical bias from the first
             * batch of Wikipedia links.
             */

            List<WikipediaRelatedPage> candidates =
                    getLinkedCandidates(pageId, 200);

            if (candidates.isEmpty()) {
                return candidates;
            }

            /*
             * ========================================================
             * 3. GET CANDIDATE INTRODUCTIONS
             * ========================================================
             *
             * Process candidates in batches of 20 so the Wikipedia
             * request URLs do not become too large.
             */

            Map<Long, String> descriptions =
                    getCandidateDescriptions(candidates);

            /*
             * ========================================================
             * 4. SCORE EACH CANDIDATE
             * ========================================================
             */

            List<ScoredRelatedPage> scoredPages =
                    new ArrayList<>();

            for (WikipediaRelatedPage candidate : candidates) {

                String description =
                        descriptions.getOrDefault(
                                candidate.pageId(),
                                ""
                        );

                int score = relatedScore(
                        currentTitle,
                        currentDescription,
                        candidate.title(),
                        description
                );

                scoredPages.add(
                        new ScoredRelatedPage(
                                candidate.title(),
                                candidate.pageId(),
                                cleanDescription(description),
                                score
                        )
                );
            }

            /*
             * Highest relevance first.
             */
            scoredPages.sort((a, b) ->
                    Integer.compare(
                            b.score(),
                            a.score()
                    )
            );

            /*
             * ========================================================
             * 5. RETURN TOP SIX
             * ========================================================
             */

            List<WikipediaRelatedPage> result =
                    new ArrayList<>();

            for (int i = 0;
                 i < Math.min(6, scoredPages.size());
                 i++) {

                ScoredRelatedPage page =
                        scoredPages.get(i);

                result.add(
                        new WikipediaRelatedPage(
                                page.title(),
                                page.pageId(),
                                page.description()
                        )
                );
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia related pages response",
                    e
            );
        }
    }

    /*
     * ================================================================
     * GET LINKED CANDIDATES WITH PAGINATION
     * ================================================================
     *
     * Wikipedia's generator=links endpoint returns a limited number
     * of links per request.
     *
     * We follow the gplcontinue token to get additional batches.
     *
     * Maximum:
     * 300 candidates
     *
     * Expected requests:
     * 100 + 100 + 100
     */
private List<WikipediaRelatedPage> getLinkedCandidates(
        long pageId,
        int maximumCandidates
) {

    List<WikipediaRelatedPage> candidates =
            new ArrayList<>();

    String continueToken = null;

    while (candidates.size() < maximumCandidates) {

        /*
         * Lambda expressions require captured local variables
         * to be final or effectively final.
         *
         * continueToken changes after every request, so we make
         * a final copy for the current request.
         */
        final String currentContinueToken = continueToken;

        String response = restClient.get()
                .uri(uriBuilder -> {

                    var builder = uriBuilder
                            .path("/w/api.php")
                            .queryParam("action", "query")
                            .queryParam("generator", "links")
                            .queryParam("pageids", pageId)
                            .queryParam("gplnamespace", "0")
                            .queryParam("gpllimit", "100")
                            .queryParam("prop", "info")
                            .queryParam("format", "json");

                    if (currentContinueToken != null) {
                        builder.queryParam(
                                "gplcontinue",
                                currentContinueToken
                        );
                    }

                    return builder.build();
                })
                .retrieve()
                .body(String.class);

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode pages = root
                    .path("query")
                    .path("pages");

            for (JsonNode page : pages) {

                String title = page
                        .path("title")
                        .asText();

                long relatedPageId = page
                        .path("pageid")
                        .asLong();

                if (title.isBlank() || relatedPageId == 0) {
                    continue;
                }

                if (relatedPageId == pageId) {
                    continue;
                }

                if (isObviousNoise(title)) {
                    continue;
                }

                candidates.add(
                        new WikipediaRelatedPage(
                                title,
                                relatedPageId,
                                ""
                        )
                );

                if (candidates.size() >= maximumCandidates) {
                    break;
                }
            }

            /*
             * ========================================================
             * READ WIKIPEDIA CONTINUATION TOKEN
             * ========================================================
             */

            JsonNode continuation =
                    root.path("continue");

            if (continuation.isMissingNode()) {
                break;
            }

            JsonNode nextToken =
                    continuation.path("gplcontinue");

            if (nextToken.isMissingNode()
                    || nextToken.asText().isBlank()) {

                break;
            }

            continueToken = nextToken.asText();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse Wikipedia linked pages response",
                    e
            );
        }
    }

    return candidates;
}
    /*
     * ================================================================
     * GET CANDIDATE DESCRIPTIONS IN SMALL BATCHES
     * ================================================================
     */

    private Map<Long, String> getCandidateDescriptions(
            List<WikipediaRelatedPage> candidates
    ) {

        Map<Long, String> descriptions =
                new HashMap<>();

        final int batchSize = 50;

        for (int start = 0;
             start < candidates.size();
             start += batchSize) {

            int end = Math.min(
                    start + batchSize,
                    candidates.size()
            );

            List<WikipediaRelatedPage> batch =
                    candidates.subList(start, end);

            String pageIds = batch.stream()
                    .map(candidate ->
                            String.valueOf(candidate.pageId()))
                    .reduce((a, b) -> a + "|" + b)
                    .orElse("");

            if (pageIds.isBlank()) {
                continue;
            }

            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/w/api.php")
                            .queryParam("action", "query")
                            .queryParam("pageids", pageIds)
                            .queryParam("prop", "extracts")
                            .queryParam("exintro", "true")
                            .queryParam("explaintext", "true")
                            .queryParam("exchars", "500")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(String.class);

            try {

                JsonNode root =
                        objectMapper.readTree(response);

                JsonNode pages = root
                        .path("query")
                        .path("pages");

                for (JsonNode page : pages) {

                    long pageId = page
                            .path("pageid")
                            .asLong();

                    String extract = page
                            .path("extract")
                            .asText();

                    if (pageId != 0) {
                        descriptions.put(
                                pageId,
                                extract
                        );
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to parse Wikipedia candidate descriptions",
                        e
                );
            }
        }

        return descriptions;
    }

    private boolean isObviousNoise(String title) {

        String lowerTitle = title.toLowerCase();

        /*
         * Lists and Wikipedia maintenance pages.
         */
        if (lowerTitle.startsWith("list of ")) {
            return true;
        }

        if (lowerTitle.startsWith("category:")) {
            return true;
        }

        if (lowerTitle.startsWith("template:")) {
            return true;
        }

        /*
         * Bibliographic information.
         */
        if (lowerTitle.contains("isbn")) {
            return true;
        }

        /*
         * Military units that are usually too specific
         * for Chronicle's recommendation section.
         */
        if (lowerTitle.contains("regiment")) {
            return true;
        }

        if (lowerTitle.contains("brigade")) {
            return true;
        }

        if (lowerTitle.contains("battalion")) {
            return true;
        }

        if (lowerTitle.contains("division (")) {
            return true;
        }

        /*
         * Specialized/non-historical material.
         */
        if (lowerTitle.contains("heraldry")) {
            return true;
        }

        return false;
    }

    private int relatedScore(
            String currentTitle,
            String currentDescription,
            String candidateTitle,
            String candidateDescription
    ) {

        String currentTitleNormalized =
                normalizeText(currentTitle);

        String currentDescriptionNormalized =
                normalizeText(currentDescription);

        String candidateTitleNormalized =
                normalizeText(candidateTitle);

        String candidateDescriptionNormalized =
                normalizeText(candidateDescription);

        String candidateText =
                candidateTitleNormalized
                        + " "
                        + candidateDescriptionNormalized;

        int score = 0;

        /*
         * ============================================================
         * 1. DIRECT TITLE CONNECTION
         * ============================================================
         */

        if (!currentTitleNormalized.isBlank()) {

            /*
             * Candidate introduction/title explicitly mentions
             * the current article.
             */
            if (candidateText.contains(currentTitleNormalized)) {
                score += 30;
            }

            /*
             * Candidate title contains the current article title.
             */
            if (candidateTitleNormalized.contains(
                    currentTitleNormalized
            )) {
                score += 20;
            }

            /*
             * Meaningful words from the current title.
             */
            Set<String> titleKeywords =
                    extractKeywords(currentTitleNormalized);

            for (String keyword : titleKeywords) {

                if (candidateTitleNormalized.contains(keyword)) {
                    score += 10;
                } else if (candidateDescriptionNormalized
                        .contains(keyword)) {
                    score += 7;
                }
            }
        }

        /*
         * ============================================================
         * 2. CONTEXTUAL OVERLAP
         * ============================================================
         */

        Set<String> currentKeywords =
                extractKeywords(
                        currentTitleNormalized
                                + " "
                                + currentDescriptionNormalized
                );

        Set<String> candidateKeywords =
                extractKeywords(candidateText);

        int overlapCount = 0;

        for (String keyword : currentKeywords) {

            if (candidateKeywords.contains(keyword)) {
                overlapCount++;
            }
        }

        /*
         * Shared contextual vocabulary is useful, but capped.
         */
        score += Math.min(
                overlapCount * 3,
                21
        );

        /*
         * Additional boost for candidates with multiple
         * meaningful connections.
         */
        if (overlapCount >= 3) {
            score += 8;
        }

        if (overlapCount >= 5) {
            score += 8;
        }

        /*
         * ============================================================
         * 3. HISTORICAL QUALITY
         * ============================================================
         *
         * This is intentionally capped.
         *
         * Being a king, general, emperor, etc. should never be
         * enough to make an unrelated historical person rank
         * above a genuinely related subject.
         */

        score += Math.min(
                historicalScore(candidateText),
                10
        );

        /*
         * ============================================================
         * 4. CONNECTION PENALTY
         * ============================================================
         */

        if (overlapCount == 0
                && !candidateText.contains(currentTitleNormalized)) {

            score -= 25;
        }

        if (overlapCount == 1
                && !candidateText.contains(currentTitleNormalized)) {

            score -= 12;
        }

        /*
         * ============================================================
         * 5. NON-HISTORICAL PENALTIES
         * ============================================================
         */

        score += nonHistoricalPenalty(candidateText);

        return score;
    }

    private int historicalScore(String text) {

        int score = 0;

        /*
         * Major historical events.
         */
        if (text.contains("world war")) {
            score += 6;
        }

        if (text.contains("war")) {
            score += 3;
        }

        if (text.contains("battle")) {
            score += 3;
        }

        if (text.contains("revolution")) {
            score += 3;
        }

        if (text.contains("rebellion")) {
            score += 2;
        }

        if (text.contains("uprising")) {
            score += 2;
        }

        if (text.contains("invasion")) {
            score += 2;
        }

        if (text.contains("siege")) {
            score += 2;
        }

        if (text.contains("treaty")) {
            score += 2;
        }

        if (text.contains("campaign")) {
            score += 2;
        }

        if (text.contains("empire")) {
            score += 2;
        }

        if (text.contains("kingdom")) {
            score += 2;
        }

        if (text.contains("dynasty")) {
            score += 2;
        }

        /*
         * Historical people.
         */
        if (text.contains("emperor")) {
            score += 3;
        }

        if (text.contains("empress")) {
            score += 3;
        }

        if (text.contains("king")) {
            score += 2;
        }

        if (text.contains("queen")) {
            score += 2;
        }

        if (text.contains("president")) {
            score += 2;
        }

        if (text.contains("prime minister")) {
            score += 2;
        }

        if (text.contains("general")) {
            score += 2;
        }

        if (text.contains("military commander")) {
            score += 3;
        }

        if (text.contains("politician")) {
            score += 1;
        }

        if (text.contains("monarch")) {
            score += 2;
        }

        if (text.contains("statesman")) {
            score += 2;
        }

        if (text.contains("duke")) {
            score += 2;
        }

        if (text.contains("marshal")) {
            score += 2;
        }

        if (text.contains("historical")) {
            score += 1;
        }

        if (text.contains("military")) {
            score += 1;
        }

        if (text.contains("political")) {
            score += 1;
        }

        return score;
    }

    private int nonHistoricalPenalty(String text) {

        int score = 0;

        if (text.contains("film")) {
            score -= 8;
        }

        if (text.contains("novel")) {
            score -= 6;
        }

        if (text.contains("album")) {
            score -= 8;
        }

        if (text.contains("song")) {
            score -= 8;
        }

        if (text.contains("video game")) {
            score -= 8;
        }

        if (text.contains("species")) {
            score -= 8;
        }

        if (text.contains("painting")) {
            score -= 5;
        }

        if (text.contains("ship")) {
            score -= 3;
        }

        return score;
    }

    private Set<String> extractKeywords(String text) {

        Set<String> keywords = new HashSet<>();

        if (text == null || text.isBlank()) {
            return keywords;
        }

        String[] words = text
                .toLowerCase()
                .split("[^a-z0-9]+");

        for (String word : words) {

            if (word.length() < 4) {
                continue;
            }

            if (STOP_WORDS.contains(word)) {
                continue;
            }

            keywords.add(word);
        }

        return keywords;
    }

    private String normalizeText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String cleanDescription(String description) {

        if (description == null || description.isBlank()) {
            return "";
        }

        description = description
                .replaceAll("\\s+", " ")
                .trim();

        if (description.length() > 180) {
            return description.substring(0, 180) + "...";
        }

        return description;
    }

    private static final Set<String> STOP_WORDS = Set.of(
            "about",
            "after",
            "again",
            "also",
            "been",
            "being",
            "before",
            "between",
            "both",
            "could",
            "during",
            "each",
            "from",
            "have",
            "having",
            "into",
            "more",
            "most",
            "other",
            "over",
            "same",
            "some",
            "such",
            "than",
            "that",
            "their",
            "them",
            "then",
            "there",
            "these",
            "they",
            "this",
            "those",
            "through",
            "under",
            "very",
            "were",
            "what",
            "when",
            "where",
            "which",
            "while",
            "with",
            "would",
            "whose",
            "will",
            "first",
            "second",
            "third",
            "later",
            "early",
            "part",
            "parts",
            "known",
            "became",
            "born",
            "died",
            "year",
            "years",
            "century",
            "centuries",
            "history",
            "historical"
    );

    private record ScoredRelatedPage(
            String title,
            long pageId,
            String description,
            int score
    ) {
    }
}