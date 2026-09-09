import { Link } from "react-router-dom";

function About() {
  return (
    <main className="about-page">

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


      <section className="about-content reveal">

        <p className="eyebrow">
          ABOUT CHRONICLE
        </p>

        <h1>
          History without
          <br />
          the rabbit hole.
        </h1>

        <div className="about-text">

          <p>
            Chronicle is a historical reference application
            designed to make learning about the past simple,
            focused, and accessible.
          </p>

          <p>
            Instead of sending you through endless pages,
            Chronicle focuses on the people, events, conflicts,
            and civilizations that matter.
          </p>

          <p>
            Search. Understand. Move on.
          </p>

        </div>

      </section>

    </main>
  );
}

export default About;