import { Link } from "react-router-dom";

function Explore() {
  return (
    <main className="standard-page">

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


      <section className="page-header reveal">

        <p className="eyebrow">
          DISCOVER
        </p>

        <h1>
          Explore History
        </h1>

        <p>
          Start anywhere. Discover the people, civilizations,
          conflicts, and events that shaped the world.
        </p>

      </section>


      <section className="explore-list full-explore-list">

        <Link to="/search" className="explore-item reveal">

          <div className="explore-image ancient-image"></div>

          <div className="explore-number">
            01
          </div>

          <div className="explore-info">
            <h3>Ancient World</h3>
            <p>Rome · Greece · Egypt · Persia</p>
          </div>

          <div className="explore-arrow">
            →
          </div>

        </Link>


        <Link to="/search" className="explore-item reveal">

          <div className="explore-image medieval-image"></div>

          <div className="explore-number">
            02
          </div>

          <div className="explore-info">
            <h3>Medieval World</h3>
            <p>Kingdoms · Empires · Crusades · Mongols</p>
          </div>

          <div className="explore-arrow">
            →
          </div>

        </Link>


        <Link to="/search" className="explore-item reveal">

          <div className="explore-image wars-image"></div>

          <div className="explore-number">
            03
          </div>

          <div className="explore-info">
            <h3>Wars & Battles</h3>
            <p>Conflicts that changed the course of history</p>
          </div>

          <div className="explore-arrow">
            →
          </div>

        </Link>


        <Link to="/search" className="explore-item reveal">

          <div className="explore-image people-image"></div>

          <div className="explore-number">
            04
          </div>

          <div className="explore-info">
            <h3>People & Leaders</h3>
            <p>Rulers · Generals · Thinkers · Revolutionaries</p>
          </div>

          <div className="explore-arrow">
            →
          </div>

        </Link>

      </section>

    </main>
  );
}

export default Explore;