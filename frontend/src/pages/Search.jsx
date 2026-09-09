import { Link } from "react-router-dom";

function Search() {
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

        <div className="large-search">

          <span className="search-icon">
            ⌕
          </span>

          <input
            type="text"
            placeholder="Search a person, event, war, civilization..."
            autoFocus
          />

          <span className="search-hint">
            ENTER
          </span>

        </div>

        <p className="search-description">
          Search historical figures, events, battles,
          civilizations, and more.
        </p>

      </section>

    </main>
  );
}

export default Search;