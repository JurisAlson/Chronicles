import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./App.css";

function Home() {
  return (
    <main className="home">
      <section className="hero">

        {/* Background Image */}
        <div className="hero-background"></div>

        {/* Dark overlay */}
        <div className="hero-overlay"></div>

        {/* Navigation */}
        <nav className="navbar">
          <a href="/" className="logo">
            chronicle
          </a>

          <div className="nav-links">
            <a href="/">Explore</a>
            <a href="/search">Search</a>
            <a href="#">About</a>
          </div>
        </nav>

        {/* Hero Content */}
        <div className="hero-content">

          <p className="eyebrow">
            ANCIENT WORLD
          </p>

          <h1>
            The Rise
            <br />
            of Rome
          </h1>

          <p className="hero-period">
            753 BC — 476 AD
          </p>

          <p className="hero-description">
            Explore the people, wars, and events that shaped
            one of history's greatest civilizations.
          </p>

          <a href="/search" className="hero-button">
            Explore History
          </a>

        </div>

        {/* Search */}
        <div className="hero-search">
          <span className="search-icon">⌕</span>

          <input
            type="text"
            placeholder="Search history..."
          />

          <span className="search-hint">
            ENTER
          </span>
        </div>

        {/* Slide Indicator */}
        <div className="slide-indicator">
          <span className="current-slide">01</span>
          <span className="slide-line"></span>
          <span>06</span>
        </div>

        {/* Scroll Indicator */}
        <div className="scroll-indicator">
          <span>SCROLL TO EXPLORE</span>
          <span className="scroll-line"></span>
        </div>

      </section>

      {/* Explore Section */}
      <section className="explore-section">

        <div className="section-heading">
          <p className="eyebrow">DISCOVER</p>

          <h2>
            Explore History
          </h2>

          <p>
            From ancient civilizations to the modern world,
            discover the people and events that shaped our past.
          </p>
        </div>

        <div className="category-grid">

          <a href="/search?category=people" className="category-card">
            <span>01</span>
            <h3>People & Leaders</h3>
            <p>Rulers, generals, thinkers, and influential figures.</p>
          </a>

          <a href="/search?category=empires" className="category-card">
            <span>02</span>
            <h3>Empires & Civilizations</h3>
            <p>The rise and fall of civilizations across history.</p>
          </a>

          <a href="/search?category=wars" className="category-card">
            <span>03</span>
            <h3>Wars & Battles</h3>
            <p>The conflicts that changed the course of history.</p>
          </a>

          <a href="/search?category=events" className="category-card">
            <span>04</span>
            <h3>Historical Events</h3>
            <p>Moments that transformed the world.</p>
          </a>

        </div>

      </section>

    </main>
  );
}

function Search() {
  return (
    <main className="placeholder-page">
      <h1>Search</h1>
    </main>
  );
}

function HistoryEntry() {
  return (
    <main className="placeholder-page">
      <h1>Historical Entry</h1>
    </main>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/search" element={<Search />} />
        <Route path="/history/:id" element={<HistoryEntry />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;