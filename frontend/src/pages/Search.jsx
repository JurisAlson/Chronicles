import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import "./Search.css";

function cleanWikipediaContent(html) {
  const parser = new DOMParser();
  const document = parser.parseFromString(html, "text/html");

  const selectorsToRemove = [
    ".reflist",
    ".references",
    ".mw-references-wrap",
    "ol.references",
    "sup.reference",
    ".reference",
    ".mw-cite-backlink",
    ".citation",
    ".noprint",
    ".navbox",
    ".metadata",
    ".ambox",
    ".hatnote",
    ".mw-editsection",
    ".infobox",
    ".sidebar",
  ];

  selectorsToRemove.forEach((selector) => {
    document.querySelectorAll(selector).forEach((element) => {
      element.remove();
    });
  });

  document.querySelectorAll("p").forEach((paragraph) => {
    if (!paragraph.textContent.trim()) {
      paragraph.remove();
    }
  });

  return document.body.innerHTML;
}

function Search() {
  const [query, setQuery] = useState("");

  // Selected article
  const [result, setResult] = useState(null);

  // Wikipedia search candidates
  const [searchResults, setSearchResults] = useState([]);

  const [sectionContents, setSectionContents] = useState([]);

  const [loading, setLoading] = useState(false);
  const [selectingResult, setSelectingResult] = useState(false);
  const [error, setError] = useState("");

  const [openSection, setOpenSection] = useState("introduction");
  const [activeSection, setActiveSection] = useState("introduction");
  const [showTimeline, setShowTimeline] = useState(false);

  /* =========================================
     SEARCH WIKIPEDIA
  ========================================= */

  async function handleSearch(event) {
    event.preventDefault();

    if (!query.trim()) {
      return;
    }

    setLoading(true);
    setError("");

    setResult(null);
    setSearchResults([]);
    setSectionContents([]);

    setOpenSection("introduction");
    setActiveSection("introduction");

    try {
      const response = await fetch(
        `http://localhost:8080/api/history/search?query=${encodeURIComponent(
          query
        )}`
      );

      if (!response.ok) {
        throw new Error("Search request failed");
      }

      const data = await response.json();

      setSearchResults(data);
    } catch (searchError) {
      console.error(searchError);
      setError("Unable to search history right now.");
    } finally {
      setLoading(false);
    }
  }

  /* =========================================
     SELECT SEARCH RESULT
  ========================================= */

  async function selectSearchResult(searchResult) {
    setSelectingResult(true);
    setError("");

    try {
      /* Fetch article introduction */
      const articleResponse = await fetch(
        `http://localhost:8080/api/history/article?pageId=${searchResult.pageId}`
      );

      if (!articleResponse.ok) {
        throw new Error("Failed to fetch history article");
      }

      const extract = await articleResponse.text();

      /* Fetch article sections */
      const sectionsResponse = await fetch(
        `http://localhost:8080/api/history/sections?pageId=${searchResult.pageId}`
      );

      if (!sectionsResponse.ok) {
        throw new Error("Failed to fetch history sections");
      }

      const sectionsData = await sectionsResponse.json();

      const ignoredSections = [
        "References",
        "Sources",
        "Further reading",
        "External links",
        "Notes",
        "See also",
        "Bibliography",
        "Citations",
        "Footnotes",
        "Works cited",
        "Biographical studies",
        "Historiography and memory",
        "Specialty studies",
        "External links and references",
      ];

      const usefulSections = sectionsData.filter((section) => {
        const title = section.title
          .replace(/<[^>]*>/g, "")
          .trim()
          .toLowerCase();

        return !ignoredSections.some(
          (ignored) => title === ignored.toLowerCase()
        );
      });

      /* Fetch section contents */
      const contents = [];

      for (const section of usefulSections) {
        try {
          const sectionResponse = await fetch(
            `http://localhost:8080/api/history/section?pageId=${searchResult.pageId}&sectionIndex=${section.index}`
          );

          if (!sectionResponse.ok) {
            continue;
          }

          const content = await sectionResponse.text();

          contents.push({
            title: section.title,
            index: section.index,
            content: cleanWikipediaContent(content),
          });
        } catch (sectionError) {
          console.error(
            `Failed to load section: ${section.title}`,
            sectionError
          );
        }
      }

      /* Everything is ready — open the article */
      setResult({
        title: searchResult.title,
        pageId: searchResult.pageId,
        extract,
      });

      setSectionContents(contents);
      setSearchResults([]);

      setOpenSection("introduction");
      setActiveSection("introduction");
    } catch (selectionError) {
      console.error(selectionError);
      setError("Unable to open this historical reference.");
    } finally {
      setSelectingResult(false);
    }
  }

  /* =========================================
     SECTION TOGGLE
  ========================================= */

  function toggleSection(sectionId) {
    setOpenSection((current) =>
      current === sectionId ? null : sectionId
    );
  }

  /* =========================================
     TIMELINE NAVIGATION
  ========================================= */

  function goToSection(sectionId) {
    setOpenSection(sectionId);
    setActiveSection(sectionId);

    requestAnimationFrame(() => {
      const element = document.getElementById(sectionId);

      if (element) {
        element.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
      }
    });
  }

  /* =========================================
     TRACK ACTIVE SECTION
  ========================================= */

  useEffect(() => {
    if (!result) {
      return;
    }

    const sectionIds = [
      "introduction",
      ...sectionContents.map(
        (section) => `section-${section.index}`
      ),
    ];

    const elements = sectionIds
      .map((id) => document.getElementById(id))
      .filter(Boolean);

    if (!elements.length) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        const visibleEntries = entries
          .filter((entry) => entry.isIntersecting)
          .sort(
            (a, b) =>
              a.boundingClientRect.top -
              b.boundingClientRect.top
          );

        if (visibleEntries.length > 0) {
          setActiveSection(
            visibleEntries[0].target.id
          );
        }
      },
      {
        rootMargin: "-15% 0px -65% 0px",
        threshold: 0,
      }
    );

    elements.forEach((element) => {
      observer.observe(element);
    });

    return () => observer.disconnect();
  }, [result, sectionContents]);
  /* =========================================
     SHOW TIMELINE ONLY DURING READING
  ========================================= */

  useEffect(() => {
    if (!result) {
      setShowTimeline(false);
      return;
    }

    function handleScroll() {
      const readingLayout = document.querySelector(
        ".history-reading-layout"
      );

      const relatedHistory = document.querySelector(
        ".related-history"
      );

      if (!readingLayout) {
        setShowTimeline(false);
        return;
      }

      const readingTop =
        readingLayout.getBoundingClientRect().top;

      const relatedTop = relatedHistory
        ? relatedHistory.getBoundingClientRect().top
        : Infinity;

  const shouldShow =
    readingTop <= 120 &&
    relatedTop > 700;

      setShowTimeline(shouldShow);
    }

    window.addEventListener("scroll", handleScroll);

    handleScroll();

    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, [result]);
  return (
    <main className="search-page">

      {/* =========================================
          NAVIGATION
      ========================================= */}

      <nav className="inner-navbar">
        <Link to="/" className="logo">
          chronicle
        </Link>

        <div className="nav-links">
          <Link to="/explore">Explore</Link>
          <Link to="/search">Search</Link>
          <Link to="/about">About</Link>
        </div>
      </nav>

      <section className="search-content">

        {/* =========================================
            SEARCH HEADER
        ========================================= */}

        <div className="search-header">
          <p className="eyebrow">
            CHRONICLE
          </p>

          {!searchResults.length && !result && (
            <>
              <h1>
                What are you
                <br />
                looking for?
              </h1>

              <form
                className="large-search"
                onSubmit={handleSearch}
              >
                <span className="search-icon">
                  ⌕
                </span>

                <input
                  type="text"
                  placeholder="Search a person, event, war, civilization..."
                  value={query}
                  onChange={(event) =>
                    setQuery(event.target.value)
                  }
                  autoFocus
                />

                <button
                  type="submit"
                  className="search-hint"
                >
                  ENTER
                </button>
              </form>

              <p className="search-description">
                Search historical figures, events, battles,
                civilizations, and more.
              </p>
            </>
          )}

          {(searchResults.length > 0 || result) && (
            <form
              className="large-search compact-search"
              onSubmit={handleSearch}
            >
              <span className="search-icon">
                ⌕
              </span>

              <input
                type="text"
                placeholder="Search another subject..."
                value={query}
                onChange={(event) =>
                  setQuery(event.target.value)
                }
              />

              <button
                type="submit"
                className="search-hint"
              >
                SEARCH
              </button>
            </form>
          )}
        </div>

        {/* =========================================
            LOADING / ERROR
        ========================================= */}

        {loading && (
          <div className="search-status">
            Searching history...
          </div>
        )}

        {selectingResult && (
          <div className="search-status">
            Opening historical reference...
          </div>
        )}

        {error && (
          <div className="search-status">
            {error}
          </div>
        )}

        {/* =========================================
            SEARCH RESULT SELECTION
        ========================================= */}

        {searchResults.length > 0 &&
          !result &&
          !loading &&
          !selectingResult && (
            <section className="search-selection">

              <div className="search-selection-header">
                <p className="eyebrow">
                  SEARCH RESULTS
                </p>

                <h2>
                  Choose a reference
                </h2>

                <p>
                  Select the historical subject you were
                  looking for.
                </p>
              </div>

              <div className="search-results-list">

                {searchResults.map((searchResult) => (
                  <button
                    type="button"
                    key={searchResult.pageId}
                    className="search-result-option"
                    onClick={(event) => {
                      event.currentTarget.blur();
                      selectSearchResult(searchResult);
                    }}
                  >
                    <div className="search-result-option-content">
                      <h3>
                        {searchResult.title}
                      </h3>

                      <p>
                        {searchResult.description}
                      </p>
                    </div>

                    <span className="search-result-arrow">
                      →
                    </span>
                  </button>
                ))}

              </div>
            </section>
          )}

        {/* =========================================
            NO RESULTS
        ========================================= */}

        {!loading &&
          !result &&
          !searchResults.length &&
          !error &&
          query && (
            <div className="search-status">
              No historical references found.
            </div>
          )}

        {/* =========================================
            ARTICLE
        ========================================= */}

        {result && !selectingResult && (
          <article className="history-result">

            {/* =========================================
                ARTICLE HEADER
            ========================================= */}

            <header className="history-result-header">
              <p className="eyebrow">
                HISTORICAL REFERENCE
              </p>

              <h2>
                {result.title}
              </h2>

              <p className="history-subtitle">
                A concise historical reference from Chronicle.
              </p>
            </header>

            {/* =========================================
                READING AREA
            ========================================= */}

            <div className="history-reading-layout">

              {/* =========================================
                  FIXED TIMELINE
              ========================================= */}

<aside
  className={`history-sidebar ${
    showTimeline ? "history-sidebar-visible" : ""
  }`}
>
  <div className="history-sidebar-inner">

                  <div className="history-sidebar-title">
                    Contents
                  </div>

                  <nav
                    className="history-sidebar-nav"
                    aria-label="Article contents"
                  >

                    {/* Introduction */}

                    <button
                      type="button"
                      className={`history-sidebar-link ${
                        activeSection === "introduction"
                          ? "active"
                          : ""
                      }`}
                      onClick={() =>
                        goToSection("introduction")
                      }
                    >
                      <span>
                        01
                      </span>

                      <strong>
                        Introduction
                      </strong>
                    </button>

                    {/* Other sections */}

                    {sectionContents.map(
                      (section, index) => {
                        const sectionId =
                          `section-${section.index}`;

                        return (
                          <button
                            type="button"
                            key={section.index}
                            className={`history-sidebar-link ${
                              activeSection === sectionId
                                ? "active"
                                : ""
                            }`}
                            onClick={() =>
                              goToSection(sectionId)
                            }
                          >
                            <span>
                              {String(index + 2).padStart(
                                2,
                                "0"
                              )}
                            </span>

                            <strong>
                              {section.title.replace(
                                /<[^>]*>/g,
                                ""
                              )}
                            </strong>
                          </button>
                        );
                      }
                    )}

                  </nav>
                </div>
              </aside>

              {/* =========================================
                  ARTICLE CONTENT
              ========================================= */}

              <div className="history-article">

                {/* Introduction */}

                <section
                  id="introduction"
                  className={`history-section ${
                    openSection === "introduction"
                      ? "open"
                      : ""
                  }`}
                >
                  <button
                    type="button"
                    className="history-section-toggle"
                    onClick={() =>
                      toggleSection("introduction")
                    }
                  >
                    <span className="history-section-number">
                      01
                    </span>

                    <span className="history-section-title">
                      Introduction
                    </span>

                    <span className="history-section-icon">
                      {openSection === "introduction"
                        ? "−"
                        : "+"}
                    </span>
                  </button>

                  <div className="history-section-content-wrapper">
                    <div className="history-section-content">
                      <p>
                        {result.extract}
                      </p>
                    </div>
                  </div>
                </section>

                {/* Other sections */}

                {sectionContents.map(
                  (section, index) => {
                    const sectionId =
                      `section-${section.index}`;

                    return (
                      <section
                        key={section.index}
                        id={sectionId}
                        className={`history-section ${
                          openSection === sectionId
                            ? "open"
                            : ""
                        }`}
                      >
                        <button
                          type="button"
                          className="history-section-toggle"
                          onClick={() =>
                            toggleSection(sectionId)
                          }
                        >
                          <span className="history-section-number">
                            {String(index + 2).padStart(
                              2,
                              "0"
                            )}
                          </span>

                          <span className="history-section-title">
                            {section.title.replace(
                              /<[^>]*>/g,
                              ""
                            )}
                          </span>

                          <span className="history-section-icon">
                            {openSection === sectionId
                              ? "−"
                              : "+"}
                          </span>
                        </button>

                        <div className="history-section-content-wrapper">
                          <div
                            className="history-section-content"
                            dangerouslySetInnerHTML={{
                              __html:
                                section.content,
                            }}
                          />
                        </div>
                      </section>
                    );
                  }
                )}

              </div>
            </div>

            {/* =========================================
                RELATED HISTORY
            ========================================= */}

            <section className="related-history">
              <div className="related-history-header">
                <p className="eyebrow">
                  CONTINUE EXPLORING
                </p>

                <h3>
                  Related History
                </h3>

                <p>
                  Explore the people, events, and places
                  connected to this subject.
                </p>
              </div>

              <div className="related-history-placeholder">
                Related historical references will appear here.
              </div>
            </section>

          </article>
        )}

      </section>
    </main>
  );
}

export default Search;