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

    /*
     * ============================================================
     * SEARCH WIKIPEDIA
     * ============================================================
     *
     * Used by:
     *
     * GET /api/history/search?query=napoleon
     *
     * Returns several possible Wikipedia articles instead of
     * requiring the user to know the exact article title.
     */

    public List<WikipediaSearchOption> searchWikipedia(
            String query
    ) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "query")
                        .queryParam("list", "search")
                        .queryParam("srsearch", query)
                        .queryParam("srlimit", "8")
                        .queryParam("srprop", "snippet")
                        .queryParam("format", "json")
                        .build()
                )
                .retrieve()
                .body(String.class);

        List<WikipediaSearchOption> results =
                new ArrayList<>();

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode searchResults =
                    root.path("query")
                            .path("search");

            for (JsonNode result : searchResults) {

                String title =
                        result.path("title").asText();

                long pageId =
                        result.path("pageid").asLong();

                String description =
                        result.path("snippet").asText();

                description =
                        cleanDescription(description);

                if (title.isBlank() || pageId == 0) {
                    continue;
                }

                results.add(
                        new WikipediaSearchOption(
                                title,
                                pageId,
                                description
                        )
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse Wikipedia search response",
                    e
            );
        }

        return results;
    }

    /*
     * ============================================================
     * GET WIKIPEDIA SECTIONS
     * ============================================================
     *
     * Returns the table of contents / section structure for an
     * article.
     */

    public List<WikipediaSection> getWikipediaSections(
            long pageId
    ) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "parse")
                        .queryParam("pageid", pageId)
                        .queryParam("prop", "sections")
                        .queryParam("format", "json")
                        .build()
                )
                .retrieve()
                .body(String.class);

        List<WikipediaSection> sections =
                new ArrayList<>();

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode sectionResults =
                    root.path("parse")
                            .path("sections");

            for (JsonNode section :
                    sectionResults) {

                String title =
                        section.path("line").asText();

                int index =
                        section.path("index").asInt();

                if (title.isBlank()) {
                    continue;
                }

                sections.add(
                        new WikipediaSection(
                                title,
                                index
                        )
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse Wikipedia sections response",
                    e
            );
        }

        return sections;
    }

    /*
     * ============================================================
     * GET SECTION CONTENT
     * ============================================================
     *
     * Returns the HTML content of one Wikipedia section.
     */

    public String getWikipediaSectionContent(
            long pageId,
            int sectionIndex
    ) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "parse")
                        .queryParam("pageid", pageId)
                        .queryParam("prop", "text")
                        .queryParam("section", sectionIndex)
                        .queryParam("format", "json")
                        .build()
                )
                .retrieve()
                .body(String.class);
    }

    /*
     * ============================================================
     * GET ARTICLE INTRODUCTION
     * ============================================================
     *
     * Returns the plaintext introduction of an article.
     *
     * This is used by Chronicle instead of displaying the entire
     * Wikipedia article at once.
     */

    public String getWikipediaArticle(
            long pageId
    ) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "query")
                        .queryParam("prop", "extracts")
                        .queryParam("exintro", "true")
                        .queryParam("explaintext", "true")
                        .queryParam("pageids", pageId)
                        .queryParam("format", "json")
                        .build()
                )
                .retrieve()
                .body(String.class);

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode pages =
                    root.path("query")
                            .path("pages");

            JsonNode page =
                    pages.elements().hasNext()
                            ? pages.elements().next()
                            : null;

            if (page == null) {
                return "";
            }

            return page
                    .path("extract")
                    .asText();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse Wikipedia article response",
                    e
            );
        }
    }

    /*
     * ============================================================
     * GET RELATED HISTORY
     * ============================================================
     *
     * Finds historically relevant pages connected to the current
     * Wikipedia article.
     *
     * The process is:
     *
     * 1. Get the current article title + introduction.
     * 2. Get up to 200 directly linked Wikipedia pages.
     * 3. Remove obvious Wikipedia noise.
     * 4. Fetch candidate descriptions in batches.
     * 5. Score candidates for relevance.
     * 6. Sort by relevance.
     * 7. Apply diversity rules.
     * 8. Return the best six.
     */

    public List<WikipediaRelatedPage> getRelatedPages(
            long pageId
    ) {

        /*
         * ========================================================
         * 1. GET CURRENT ARTICLE INFORMATION
         * ========================================================
         */

        String currentResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/w/api.php")
                        .queryParam("action", "query")
                        .queryParam("pageids", pageId)
                        .queryParam(
                                "prop",
                                "info|extracts"
                        )
                        .queryParam(
                                "exintro",
                                "true"
                        )
                        .queryParam(
                                "explaintext",
                                "true"
                        )
                        .queryParam("format", "json")
                        .build()
                )
                .retrieve()
                .body(String.class);

        String currentTitle = "";
        String currentDescription = "";

        try {

            JsonNode root =
                    objectMapper.readTree(currentResponse);

            JsonNode pages =
                    root.path("query")
                            .path("pages");

            JsonNode page =
                    pages.elements().hasNext()
                            ? pages.elements().next()
                            : null;

            if (page != null) {

                currentTitle =
                        page.path("title").asText();

                currentDescription =
                        page.path("extract").asText();
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse current Wikipedia article",
                    e
            );
        }

        /*
         * ========================================================
         * 2. GET LINKED CANDIDATES
         * ========================================================
         *
         * Keep this at 200.
         *
         * Increasing it too much causes unnecessary Wikipedia API
         * requests and can trigger HTTP 429 rate limits.
         */

        List<WikipediaRelatedPage> candidates =
                getLinkedCandidates(
                        pageId,
                        200
                );

        if (candidates.isEmpty()) {
            return new ArrayList<>();
        }

        /*
         * ========================================================
         * 3. GET CANDIDATE DESCRIPTIONS
         * ========================================================
         *
         * Fetch descriptions in batches of 50.
         *
         * This keeps the number of Wikipedia API calls reasonable.
         */

        Map<Long, String> descriptions =
                getCandidateDescriptions(
                        candidates
                );

        /*
         * ========================================================
         * 4. SCORE CANDIDATES
         * ========================================================
         */

        List<ScoredRelatedPage> scoredPages =
                new ArrayList<>();

        for (WikipediaRelatedPage candidate :
                candidates) {

            String description =
                    descriptions.getOrDefault(
                            candidate.pageId(),
                            ""
                    );

            int score =
                    relatedScore(
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
         * ========================================================
         * 5. SORT BY RELEVANCE
         * ========================================================
         */

        scoredPages.sort((a, b) ->
                Integer.compare(
                        b.score(),
                        a.score()
                )
        );

        /*
         * ========================================================
         * 6. SELECT DIVERSE RECOMMENDATIONS
         * ========================================================
         *
         * The scoring system may naturally rank many similar pages
         * together, especially battles.
         *
         * Chronicle should provide a useful mixture of historical
         * references instead of six pages of the same type.
         */

        List<ScoredRelatedPage> selectedPages =
                new ArrayList<>();

        int battleCount = 0;

        for (ScoredRelatedPage page :
                scoredPages) {

            String lowerTitle =
                    page.title().toLowerCase();

            boolean isBattle =
                    lowerTitle.startsWith("battle of ")
                            || lowerTitle.contains(" battle");

            /*
             * Allow a maximum of two battle recommendations.
             */

            if (isBattle && battleCount >= 2) {
                continue;
            }

            selectedPages.add(page);

            if (isBattle) {
                battleCount++;
            }

            if (selectedPages.size() >= 6) {
                break;
            }
        }

        /*
         * ========================================================
         * 7. FALLBACK
         * ========================================================
         *
         * If the diversity rule prevented us from getting six
         * pages, fill the remaining slots with the highest-scoring
         * pages.
         */

        if (selectedPages.size() < 6) {

            for (ScoredRelatedPage page :
                    scoredPages) {

                if (selectedPages.contains(page)) {
                    continue;
                }

                selectedPages.add(page);

                if (selectedPages.size() >= 6) {
                    break;
                }
            }
        }

        /*
         * ========================================================
         * 8. CONVERT TO API DTOs
         * ========================================================
         */

        List<WikipediaRelatedPage> result =
                new ArrayList<>();

        for (ScoredRelatedPage page :
                selectedPages) {

            result.add(
                    new WikipediaRelatedPage(
                            page.title(),
                            page.pageId(),
                            page.description()
                    )
            );
        }

        return result;
    }

    /*
     * ============================================================
     * GET LINKED CANDIDATES
     * ============================================================
     *
     * Uses Wikipedia's generator=links API.
     *
     * Pagination is required because Wikipedia returns a limited
     * number of links per request.
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
             * Java lambda expressions require captured variables
             * to be effectively final.
             *
             * Therefore we copy the current continuation token.
             */

            final String currentContinueToken =
                    continueToken;

            String response = restClient.get()
                    .uri(uriBuilder -> {

                        var builder = uriBuilder
                                .path("/w/api.php")
                                .queryParam(
                                        "action",
                                        "query"
                                )
                                .queryParam(
                                        "generator",
                                        "links"
                                )
                                .queryParam(
                                        "pageids",
                                        pageId
                                )
                                .queryParam(
                                        "gplnamespace",
                                        "0"
                                )
                                .queryParam(
                                        "gpllimit",
                                        "100"
                                )
                                .queryParam(
                                        "prop",
                                        "info"
                                )
                                .queryParam(
                                        "format",
                                        "json"
                                );

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

                JsonNode pages =
                        root.path("query")
                                .path("pages");

                for (JsonNode page :
                        pages) {

                    String title =
                            page.path("title").asText();

                    long relatedPageId =
                            page.path("pageid").asLong();

                    if (title.isBlank()
                            || relatedPageId == 0) {
                        continue;
                    }

                    /*
                     * Do not recommend the article itself.
                     */

                    if (relatedPageId == pageId) {
                        continue;
                    }

                    /*
                     * Remove obvious Wikipedia noise.
                     */

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

                    if (candidates.size()
                            >= maximumCandidates) {
                        break;
                    }
                }

                /*
                 * Check for another Wikipedia page of results.
                 */

                JsonNode continuation =
                        root.path("continue");

                if (continuation.isMissingNode()) {
                    break;
                }

                JsonNode nextToken =
                        continuation.path(
                                "gplcontinue"
                        );

                if (nextToken.isMissingNode()
                        || nextToken.asText().isBlank()) {
                    break;
                }

                continueToken =
                        nextToken.asText();

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
     * ============================================================
     * GET CANDIDATE DESCRIPTIONS
     * ============================================================
     *
     * Fetches candidate introductions in batches.
     *
     * Batch size is intentionally 50 to reduce the possibility
     * of Wikipedia returning HTTP 429 Too Many Requests.
     */

    private Map<Long, String> getCandidateDescriptions(
            List<WikipediaRelatedPage> candidates
    ) {

        Map<Long, String> descriptions =
                new HashMap<>();

        final int batchSize = 50;

        for (
                int start = 0;
                start < candidates.size();
                start += batchSize
        ) {

            int end =
                    Math.min(
                            start + batchSize,
                            candidates.size()
                    );

            List<WikipediaRelatedPage> batch =
                    candidates.subList(
                            start,
                            end
                    );

            String pageIds =
                    batch.stream()
                            .map(candidate ->
                                    String.valueOf(
                                            candidate.pageId()
                                    )
                            )
                            .reduce(
                                    (a, b) ->
                                            a + "|" + b
                            )
                            .orElse("");

            if (pageIds.isBlank()) {
                continue;
            }

            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/w/api.php")
                            .queryParam(
                                    "action",
                                    "query"
                            )
                            .queryParam(
                                    "pageids",
                                    pageIds
                            )
                            .queryParam(
                                    "prop",
                                    "extracts"
                            )
                            .queryParam(
                                    "exintro",
                                    "true"
                            )
                            .queryParam(
                                    "explaintext",
                                    "true"
                            )
                            .queryParam(
                                    "exchars",
                                    "500"
                            )
                            .queryParam(
                                    "format",
                                    "json"
                            )
                            .build()
                    )
                    .retrieve()
                    .body(String.class);

            try {

                JsonNode root =
                        objectMapper.readTree(response);

                JsonNode pages =
                        root.path("query")
                                .path("pages");

                for (JsonNode page :
                        pages) {

                    long candidatePageId =
                            page.path("pageid").asLong();

                    String description =
                            page.path("extract").asText();

                    if (candidatePageId == 0) {
                        continue;
                    }

                    descriptions.put(
                            candidatePageId,
                            description
                    );
                }

            } catch (Exception e) {

                throw new RuntimeException(
                        "Failed to parse Wikipedia candidate descriptions response",
                        e
                );
            }
        }

        return descriptions;
    }

    /*
     * ============================================================
     * RELATED SCORE
     * ============================================================
     *
     * Determines how relevant a candidate page is to the current
     * article.
     *
     * Strongest signals:
     *
     * - Candidate explicitly mentions the current article.
     * - Candidate title contains the current title.
     * - Candidate title shares meaningful keywords.
     *
     * Weaker signals:
     *
     * - Description keyword overlap.
     * - General historical importance.
     *
     * This prevents generic historical pages from outranking
     * genuinely related pages.
     */

    private int relatedScore(
            String currentTitle,
            String currentDescription,
            String candidateTitle,
            String candidateDescription
    ) {

        int score = 0;

        String currentTitleLower =
                currentTitle.toLowerCase();

        String candidateTitleLower =
                candidateTitle.toLowerCase();

        String candidateText =
                (
                        candidateTitle
                                + " "
                                + candidateDescription
                ).toLowerCase();

        /*
         * ========================================================
         * 1. DIRECT ARTICLE RELATION
         * ========================================================
         */

        if (!currentTitleLower.isBlank()
                && candidateText.contains(
                        currentTitleLower
                )) {

            score += 35;
        }

        /*
         * Candidate title contains the current article title.
         *
         * Example:
         *
         * Current:
         * Napoleon
         *
         * Candidate:
         * Napoleon III
         */

        if (!currentTitleLower.isBlank()
                && candidateTitleLower.contains(
                        currentTitleLower
                )) {

            score += 30;
        }

        /*
         * ========================================================
         * 2. MEANINGFUL TITLE KEYWORDS
         * ========================================================
         */

        Set<String> currentKeywords =
                extractKeywords(currentTitle);

        Set<String> candidateTitleKeywords =
                extractKeywords(candidateTitle);

        int titleOverlap = 0;

        for (String keyword :
                currentKeywords) {

            if (candidateTitleKeywords.contains(
                    keyword
            )) {

                titleOverlap++;
            }
        }

        /*
         * Title matches are stronger than description matches.
         */

        score += Math.min(
                titleOverlap * 12,
                36
        );

        /*
         * ========================================================
         * 3. DESCRIPTION KEYWORD MATCHING
         * ========================================================
         */

        Set<String> candidateDescriptionKeywords =
                extractKeywords(
                        candidateDescription
                );

        int descriptionOverlap = 0;

        for (String keyword :
                currentKeywords) {

            if (candidateDescriptionKeywords.contains(
                    keyword
            )) {

                descriptionOverlap++;
            }
        }

        score += Math.min(
                descriptionOverlap * 5,
                20
        );

        /*
         * ========================================================
         * 4. ARTICLE / CANDIDATE TEXT OVERLAP
         * ========================================================
         *
         * Catches relationships where the exact title is not
         * mentioned but both articles discuss the same subject.
         */

        Set<String> currentTextKeywords =
                extractKeywords(
                        currentTitle
                                + " "
                                + currentDescription
                );

        Set<String> candidateTextKeywords =
                extractKeywords(
                        candidateTitle
                                + " "
                                + candidateDescription
                );

        int overlap = 0;

        for (String keyword :
                currentTextKeywords) {

            if (candidateTextKeywords.contains(
                    keyword
            )) {

                overlap++;
            }
        }

        score += Math.min(
                overlap * 2,
                20
        );

        /*
         * Strong shared vocabulary.
         */

        if (overlap >= 4) {
            score += 8;
        }

        if (overlap >= 7) {
            score += 8;
        }

        /*
         * ========================================================
         * 5. HISTORICAL IMPORTANCE
         * ========================================================
         *
         * This is deliberately capped so that a generic historical
         * figure cannot overpower a genuinely related page.
         */

        score += Math.min(
                historicalScore(
                        candidateTitle,
                        candidateDescription
                ),
                15
        );

        /*
         * ========================================================
         * 6. PENALIZE WEAK CONNECTIONS
         * ========================================================
         */

        if (overlap == 0
                && titleOverlap == 0
                && descriptionOverlap == 0
                && !candidateText.contains(
                        currentTitleLower
                )) {

            score -= 30;
        }

        if (overlap == 1
                && titleOverlap == 0
                && descriptionOverlap == 0
                && !candidateText.contains(
                        currentTitleLower
                )) {

            score -= 15;
        }

        /*
         * ========================================================
         * 7. HISTORICAL ENTITY TYPE BONUS
         * ========================================================
         *
         * Give a modest boost to recognizable historical entities.
         */

        String combined =
                candidateTitleLower
                        + " "
                        + candidateDescription.toLowerCase();

        if (combined.contains("emperor")
                || combined.contains("king")
                || combined.contains("queen")
                || combined.contains("president")
                || combined.contains("general")
                || combined.contains("commander")
                || combined.contains("prime minister")) {

            score += 4;
        }

        /*
         * Major events receive a modest boost.
         */

        if (candidateTitleLower.startsWith(
                "battle of "
        )
                || candidateTitleLower.contains(" war")
                || candidateTitleLower.contains("revolution")
                || candidateTitleLower.contains("siege")) {

            score += 4;
        }

        return score;
    }

    /*
     * ============================================================
     * HISTORICAL SCORE
     * ============================================================
     *
     * Detects historically meaningful entity types.
     *
     * This is intentionally a secondary scoring mechanism.
     */

    private int historicalScore(
            String title,
            String description
    ) {

        String text =
                (
                        title
                                + " "
                                + description
                ).toLowerCase();

        int score = 0;

        /*
         * Wars
         */

        if (text.contains("world war")) {
            score += 6;
        }

        if (text.contains("war")) {
            score += 3;
        }

        /*
         * Battles and military events
         */

        if (text.contains("battle")) {
            score += 3;
        }

        if (text.contains("siege")) {
            score += 3;
        }

        if (text.contains("invasion")) {
            score += 3;
        }

        /*
         * Political / historical events
         */

        if (text.contains("revolution")) {
            score += 3;
        }

        if (text.contains("rebellion")) {
            score += 3;
        }

        if (text.contains("uprising")) {
            score += 3;
        }

        if (text.contains("coup")) {
            score += 3;
        }

        /*
         * Historical people
         */

        if (text.contains("emperor")) {
            score += 3;
        }

        if (text.contains("king")) {
            score += 2;
        }

        if (text.contains("queen")) {
            score += 2;
        }

        if (text.contains("general")) {
            score += 2;
        }

        if (text.contains("commander")) {
            score += 2;
        }

        if (text.contains("president")) {
            score += 2;
        }

        /*
         * Keep this score controlled.
         */

        return Math.min(
                score,
                15
        );
    }

    /*
     * ============================================================
     * FILTER OBVIOUS WIKIPEDIA NOISE
     * ============================================================
     *
     * Removes pages that are technically linked but are not useful
     * historical recommendations for Chronicle.
     */

    private boolean isObviousNoise(
            String title
    ) {

        String lower =
                title.toLowerCase();

        if (lower.startsWith("list of ")) {
            return true;
        }

        if (lower.startsWith("category:")) {
            return true;
        }

        if (lower.startsWith("template:")) {
            return true;
        }

        if (lower.contains("isbn")) {
            return true;
        }

        if (lower.contains("regiment")) {
            return true;
        }

        if (lower.contains("brigade")) {
            return true;
        }

        if (lower.contains("battalion")) {
            return true;
        }

        if (lower.contains("division (")) {
            return true;
        }

        if (lower.contains("heraldry")) {
            return true;
        }

        return false;
    }

    /*
     * ============================================================
     * EXTRACT KEYWORDS
     * ============================================================
     *
     * Converts text into meaningful lowercase keywords while
     * removing common words.
     */

    private Set<String> extractKeywords(
            String text
    ) {

        Set<String> keywords =
                new HashSet<>();

        if (text == null || text.isBlank()) {
            return keywords;
        }

        String normalized =
                normalizeText(text);

        String[] words =
                normalized.split("\\s+");

        for (String word :
                words) {

            if (word.isBlank()) {
                continue;
            }

            if (word.length() < 3) {
                continue;
            }

            if (STOP_WORDS.contains(word)) {
                continue;
            }

            keywords.add(word);
        }

        return keywords;
    }

    /*
     * ============================================================
     * NORMALIZE TEXT
     * ============================================================
     */

    private String normalizeText(
            String text
    ) {

        return text
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9\\s]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    /*
     * ============================================================
     * CLEAN DESCRIPTION
     * ============================================================
     *
     * Wikipedia search snippets contain HTML tags and entities.
     */

    private String cleanDescription(
            String description
    ) {

        if (description == null) {
            return "";
        }

        return description
                .replaceAll(
                        "<[^>]*>",
                        ""
                )
                .replace(
                        "&quot;",
                        "\""
                )
                .replace(
                        "&#39;",
                        "'"
                )
                .replace(
                        "&amp;",
                        "&"
                )
                .replace(
                        "&lt;",
                        "<"
                )
                .replace(
                        "&gt;",
                        ">"
                )
                .trim();
    }

    /*
     * ============================================================
     * STOP WORDS
     * ============================================================
     *
     * Common words that do not provide useful historical
     * relevance information.
     */

    private static final Set<String> STOP_WORDS =
            Set.of(
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

    /*
     * ============================================================
     * INTERNAL SCORE RECORD
     * ============================================================
     */

    private record ScoredRelatedPage(
            String title,
            long pageId,
            String description,
            int score
    ) {
    }
}