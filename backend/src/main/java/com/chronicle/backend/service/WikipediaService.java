package com.chronicle.backend.service;

import com.chronicle.backend.dto.WikipediaSearchOption;
import com.chronicle.backend.dto.WikipediaSection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

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
     */

    public List<WikipediaSearchOption> searchWikipedia(
            String query
    ) {

        String response;

        try {

            response = restClient.get()
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

        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {

            System.out.println(
                    "Wikipedia search rate-limited (429)"
            );

            return new ArrayList<>();

        } catch (Exception e) {

            System.out.println(
                    "Failed to fetch Wikipedia search: "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }

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
     */

    public List<WikipediaSection> getWikipediaSections(
            long pageId
    ) {

        String response;

        try {

            response = restClient.get()
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

        } catch (
                org.springframework.web.client.HttpClientErrorException.TooManyRequests e
        ) {

            System.out.println(
                    "Wikipedia sections rate-limited (429) for page "
                            + pageId
            );

            return new ArrayList<>();

        } catch (Exception e) {

            System.out.println(
                    "Failed to fetch Wikipedia sections for page "
                            + pageId
                            + ": "
                            + e.getMessage()
            );

            return new ArrayList<>();
        }

        List<WikipediaSection> sections =
                new ArrayList<>();

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode sectionResults =
                    root.path("parse")
                            .path("sections");

            for (JsonNode section : sectionResults) {

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
     */

    public String getWikipediaSectionContent(
            long pageId,
            int sectionIndex
    ) {

        String response;

        try {

            response = restClient.get()
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

        } catch (
                org.springframework.web.client.HttpClientErrorException.TooManyRequests e
        ) {

            System.out.println(
                    "Wikipedia section rate-limited (429) for page "
                            + pageId
                            + ", section "
                            + sectionIndex
            );

            return "";

        } catch (Exception e) {

            System.out.println(
                    "Failed to fetch Wikipedia section for page "
                            + pageId
                            + ", section "
                            + sectionIndex
                            + ": "
                            + e.getMessage()
            );

            return "";
        }

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            String html =
                    root.path("parse")
                            .path("text")
                            .path("*")
                            .asText("");

            if (html.isBlank()) {
                return "";
            }

            return cleanWikipediaHtml(html);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse Wikipedia section response",
                    e
            );
        }
    }

    /*
     * ============================================================
     * GET WIKIPEDIA ARTICLE INTRODUCTION
     * ============================================================
     */

    public String getWikipediaArticle(
            long pageId
    ) {

        String response;

        try {

            response = restClient.get()
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

        } catch (
                org.springframework.web.client.HttpClientErrorException.TooManyRequests e
        ) {

            System.out.println(
                    "Wikipedia article rate-limited (429) for page "
                            + pageId
            );

            return "";

        } catch (Exception e) {

            System.out.println(
                    "Failed to fetch Wikipedia article for page "
                            + pageId
                            + ": "
                            + e.getMessage()
            );

            return "";
        }

        try {

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode pages =
                    root.path("query")
                            .path("pages");

            var iterator =
                    pages.elements();

            if (!iterator.hasNext()) {
                return "";
            }

            JsonNode page =
                    iterator.next();

            return page
                    .path("extract")
                    .asText("");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse Wikipedia article response",
                    e
            );
        }
    }

    /*
     * ============================================================
     * CLEAN WIKIPEDIA HTML
     * ============================================================
     */

    private String cleanWikipediaHtml(
            String html
    ) {

        if (html == null || html.isBlank()) {
            return "";
        }

        String cleaned = html;

        cleaned = cleaned.replaceAll(
                "(?is)<p[^>]*>\\s*Cite error:.*?</p>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?is)Cite error:\\s*There are.*?(?=<p|<div|<h[1-6]|$)",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?is)Cite error:\\s*There are.*?(?=<br\\s*/?>|$)",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<span[^>]*class=\"[^\"]*mw-editsection[^\"]*\"[^>]*>.*?</span>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<a[^>]*href=\"[^\"]*action=edit[^\"]*\"[^>]*>.*?</a>",
                ""
        );

        cleaned = cleaned.replace(
                "[edit]",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<div[^>]*class=\"[^\"]*(reflist|references|mw-references-wrap)[^\"]*\"[^>]*>.*?</div>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<ol[^>]*class=\"[^\"]*references[^\"]*\"[^>]*>.*?</ol>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<sup[^>]*class=\"[^\"]*(reference|citation)[^\"]*\"[^>]*>.*?</sup>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<table[^>]*class=\"[^\"]*(navbox|vertical-navbox|sidebar|infobox)[^\"]*\"[^>]*>.*?</table>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<table[^>]*>.*?</table>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<div[^>]*role=\"navigation\"[^>]*>.*?</div>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<div[^>]*class=\"[^\"]*(metadata|hatnote|ambox|portal|sistersitebox|catlinks|authority-control)[^\"]*\"[^>]*>.*?</div>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<script[^>]*>.*?</script>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<style[^>]*>.*?</style>",
                ""
        );

        cleaned = cleaned.replaceAll(
                "(?s)<p>\\s*</p>",
                ""
        );

        cleaned = cleaned.replace(
                "[edit]",
                ""
        );

        return cleaned.trim();
    }

    /*
     * ============================================================
     * CLEAN SEARCH DESCRIPTION
     * ============================================================
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
}