import { Link } from "react-router-dom";

function Home() {
  return (
    <main className="home">

      <section className="hero">

        <div className="hero-background"></div>
        <div className="hero-overlay"></div>

        <nav className="navbar">

          <Link to="/" className="logo">
            chronicle
          </Link>

          <div className="nav-links">
            <Link to="/explore">Explore</Link>
            <Link to="/search">Search</Link>
            <Link to="/about">About</Link>
          </div>

        </nav>

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

          <Link to="/explore" className="hero-button">
            Explore History
          </Link>

        </div>

        <Link to="/search" className="hero-search">

          <span className="search-icon">
            ⌕
          </span>

          <span className="search-placeholder">
            Search history...
          </span>

          <span className="search-hint">
            ENTER
          </span>

        </Link>

        <div className="slide-indicator">
          <span className="current-slide">01</span>
          <span className="slide-line"></span>
          <span>04</span>
        </div>

        <div className="scroll-indicator">
          <span>SCROLL TO EXPLORE</span>
          <span className="scroll-line"></span>
        </div>

      </section>


      <section className="explore-preview">

        <div className="section-heading reveal">

          <p className="eyebrow">
            DISCOVER
          </p>

          <h2>
            Explore History
          </h2>

          <p>
            Begin with a period, a civilization, a conflict,
            or the people who shaped the world.
          </p>

        </div>


        <div className="explore-list">

          <Link
            to="/explore"
            className="explore-item reveal"
          >

            <div className="explore-image ancient-image"></div>

            <div className="explore-number">
              01
            </div>

            <div className="explore-info">

              <h3>
                Ancient World
              </h3>

              <p>
                Rome · Greece · Egypt · Persia
              </p>

            </div>

            <div className="explore-arrow">
              →
            </div>

          </Link>


          <Link
            to="/explore"
            className="explore-item reveal"
          >

            <div className="explore-image medieval-image"></div>

            <div className="explore-number">
              02
            </div>

            <div className="explore-info">

              <h3>
                Medieval World
              </h3>

              <p>
                Kingdoms · Empires · Crusades · Mongols
              </p>

            </div>

            <div className="explore-arrow">
              →
            </div>

          </Link>


          <Link
            to="/explore"
            className="explore-item reveal"
          >

            <div className="explore-image wars-image"></div>

            <div className="explore-number">
              03
            </div>

            <div className="explore-info">

              <h3>
                Wars & Battles
              </h3>

              <p>
                The conflicts that changed the course of history
              </p>

            </div>

            <div className="explore-arrow">
              →
            </div>

          </Link>


          <Link
            to="/explore"
            className="explore-item reveal"
          >

            <div className="explore-image people-image"></div>

            <div className="explore-number">
              04
            </div>

            <div className="explore-info">

              <h3>
                People & Leaders
              </h3>

              <p>
                Rulers · Generals · Thinkers · Revolutionaries
              </p>

            </div>

            <div className="explore-arrow">
              →
            </div>

          </Link>

        </div>

      </section>

    </main>
  );
}

export default Home;