import { Link } from "react-router-dom";
import "./Footer.css";

function Footer() {
  return (
    <footer className="site-footer">

      <div className="footer-main">

        <div className="footer-brand">
          <Link to="/" className="footer-logo">
            chronicle
          </Link>

          <p className="footer-tagline">
            History without the rabbit hole.
          </p>
        </div>

        <div className="footer-links">

          <div className="footer-column">
            <p className="footer-heading">EXPLORE</p>

            <Link to="/explore?category=ancient">
              Ancient World
            </Link>

            <Link to="/explore?category=medieval">
              Medieval World
            </Link>

            <Link to="/explore?category=wars">
              Wars & Battles
            </Link>

            <Link to="/explore?category=people">
              People & Leaders
            </Link>
          </div>

          <div className="footer-column">
            <p className="footer-heading">CHRONICLE</p>

            <Link to="/search">
              Search
            </Link>

            <Link to="/about">
              About
            </Link>
          </div>

        </div>

      </div>

      <div className="footer-bottom">

        <p>
          © {new Date().getFullYear()} Chronicle
        </p>

        <p>
          Historical reference powered by Wikipedia
        </p>

      </div>

    </footer>
  );
}

export default Footer;