import { useState } from "react";
import { Link } from "react-router-dom";
import "./Search.css";

function Search() {
  const [query, setQuery] = useState("");
  const [result, setResult] = useState(null);
  const [sections, setSections] = useState([]);
  const [sectionContents, setSectionContents] = useState([]);
  const [openSection, setOpenSection] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSearch(event) {
    event.preventDefault();

    if (!query.trim()) {
      return;
    }

    setLoading(true);
    setError("");
    setResult(null);
    setSections([]);
    setSectionContents([]);
    setOpenSection(null);

    try {
      // SEARCH WIKIPEDIA
      const response = await fetch(
        `http://localhost:8080/api/history/search?query=${encodeURIComponent(
          query
        )}`
      );

      if (!response.ok) {
        throw new Error("Search request failed");
      }

      const data = await response.json();

      setResult(data);

      // GET WIKIPEDIA SECTIONS
      const sectionsResponse = await fetch(
        `http://localhost:8080/api/history/sections?pageId=${data.pageId}`
      );

      if (!sectionsResponse.ok) {
        throw new Error("Failed to fetch history sections");
      }

      const sectionsData = await sectionsResponse.json();

      // REMOVE UNNECESSARY WIKIPEDIA SECTIONS
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
      ];

      const usefulSections = sectionsData.filter(
        (section) =>
          !ignoredSections.some(
            (ignored) =>
              section.title.toLowerCase() === ignored.toLowerCase()
          )
      );

      setSections(usefulSections);

      // LOAD SECTION CONTENT
      const contents = [];

      for (const section of usefulSections) {
        try {
          const sectionResponse = await fetch(
            `http://localhost:8080/api/history/section?pageId=${data.pageId}&sectionIndex=${section.index}`
          );

          if (!sectionResponse.ok) {
            continue;
          }

          const content = await sectionResponse.text();

          contents.push({
            title: section.title,
            index: section.index,
            content: content,
          });
        } catch (sectionError) {
          console.error(
            `Failed to load section: ${section.title}`,
            sectionError
          );
        }
      }

      setSectionContents(contents);
    } catch (error) {
      console.error(error);
      setError("Unable to search history right now.");
    } finally {
      setLoading(false);
    }
  }

  function toggleSection(sectionIndex) {
    setOpenSection((current) =>
      current === sectionIndex ? null : sectionIndex
    );
  }

  return (
    <main className="search-page">

      {/* NAVIGATION */}
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

      {/* SEARCH AREA */}
      <section className="search-content">

        <div className="search-header">

          <p className="eyebrow">
            CHRONICLE
          </p>

          {!result && (
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
                  onChange={(event) => setQuery(event.target.value)}
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

          {result && (
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
                onChange={(event) => setQuery(event.target.value)}
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

        {/* LOADING */}
        {loading && (
          <div className="search-status">
            Searching history...
          </div>
        )}

        {/* ERROR */}
        {error && (
          <div className="search-status">
            {error}
          </div>
        )}

        {/* HISTORICAL ARTICLE */}
        {result && !loading && (
          <article className="history-result">

            {/* ARTICLE HEADER */}
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

            {/* INTRODUCTION */}
            <section className="history-introduction">
              <div className="history-introduction-label">
                Introduction
              </div>

              <div className="history-introduction-content">
                <p>
                  {result.extract}
                </p>
              </div>
            </section>

            {/* TABLE OF CONTENTS */}
            {sections.length > 0 && (
              <nav className="history-contents">
                <div className="history-contents-header">
                  Contents
                </div>

                <div className="history-contents-list">
                  {sections.map((section, index) => (
                    <a
                      key={section.index}
                      href={`#section-${section.index}`}
                      onClick={() => setOpenSection(section.index)}
                    >
                      <span>
                        {String(index + 1).padStart(2, "0")}
                      </span>

                      {section.title}
                    </a>
                  ))}
                </div>
              </nav>
            )}

            {/* ARTICLE SECTIONS */}
            <div className="history-article">

              {sectionContents.map((section, index) => {
                const isOpen = openSection === section.index;

                return (
                  <section
                    key={section.index}
                    id={`section-${section.index}`}
                    className={`history-section ${
                      isOpen ? "open" : ""
                    }`}
                  >

                    {/* SECTION HEADER */}
                    <button
                      className="history-section-toggle"
                      onClick={() =>
                        toggleSection(section.index)
                      }
                      aria-expanded={isOpen}
                    >
                      <span className="history-section-number">
                        {String(index + 1).padStart(2, "0")}
                      </span>

                      <span className="history-section-title">
                        {section.title}
                      </span>

                      <span className="history-section-icon">
                        {isOpen ? "−" : "+"}
                      </span>
                    </button>

                    {/* SECTION CONTENT */}
                    <div
                      className="history-section-content-wrapper"
                    >
                      <div
                        className="history-section-content"
                        dangerouslySetInnerHTML={{
                          __html: section.content,
                        }}
                      />
                    </div>

                  </section>
                );
              })}

            </div>

            {/* RELATED HISTORY */}
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
