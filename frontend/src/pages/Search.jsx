import { useState } from "react";
import { Link } from "react-router-dom";

function Search() {
  const [query, setQuery] = useState("");
  const [result, setResult] = useState(null);
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

    try {
      const response = await fetch(
        `http://localhost:8080/api/history/search?query=${encodeURIComponent(query)}`
      );

      if (!response.ok) {
        throw new Error("Search request failed");
      }

      const data = await response.json();

      setResult(data);

    } catch (error) {
      console.error(error);
      setError("Unable to search history right now.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="search-page">

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

        <p className="eyebrow">
          CHRONICLE
        </p>

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


        {loading && (
          <div className="search-status">
            Searching history...
          </div>
        )}


        {error && (
          <div className="search-status">
            {error}
          </div>
        )}


        {result && !loading && (
          <section className="search-result">

            <p className="eyebrow">
              RESULT
            </p>

            <h2>
              {result.title}
            </h2>

            <p>
              {result.extract}
            </p>

            <span>
              Wikipedia page ID: {result.pageId}
            </span>

          </section>
        )}

      </section>

    </main>
  );
}

export default Search;