import { Link } from "react-router-dom";
import Footer from "../components/Footer";
import "./About.css";

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


      {/* ========================================
          INTRO
          ======================================== */}

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
            Chronicle is a historical reference project created
            to make exploring history feel simpler, more focused,
            and more enjoyable.
          </p>

          <p>
            History has an endless number of stories. A person leads
            to a war, a war leads to an empire, and an empire leads
            to another century entirely. Before long, you can find
            yourself far away from the question that started it all.
          </p>

          <p>
            Chronicle was created to offer a different way to explore.
            Start with a subject, discover where it leads, understand
            what happened, and continue when you are ready.
          </p>

        </div>

      </section>


      {/* ========================================
          WHY CHRONICLE
          ======================================== */}

      <section className="about-section reveal">

        <p className="eyebrow">
          WHY CHRONICLE
        </p>

        <div className="about-section-content">

          <h2>
            Made for the curious.
          </h2>

          <div className="about-text">

            <p>
              Chronicle began with a simple appreciation for history
              and the feeling of discovering a story that you had
              never heard before.
            </p>

            <p>
              The purpose is not to replace the books, historians,
              museums, documentaries, and countless resources that
              already exist. It is to create a place where curiosity
              can begin.
            </p>

            <p>
              Whether you want to understand an ancient civilization,
              learn about a famous leader, revisit a war, or simply
              answer a question that came to mind, Chronicle is built
              to help you take that first step.
            </p>

          </div>

        </div>

      </section>


      {/* ========================================
          THE CREATOR
          ======================================== */}

      <section className="about-section reveal">

        <p className="eyebrow">
          BUILT WITH CURIOSITY
        </p>

        <div className="about-section-content">

          <h2>
            A project by a
            <br />
            history enthusiast.
          </h2>

          <div className="about-text">

            <p>
              Chronicle is an independent project developed by a
              history enthusiast and solo developer.
            </p>

            <p>
              What started as an idea to create a better way to
              explore historical topics has grown into an ongoing
              project with much more planned for the future.
            </p>

            <p>
              It is built one idea at a time, with the hope that
              Chronicle can eventually become a place people return
              to whenever curiosity takes them somewhere in history.
            </p>

          </div>

        </div>

      </section>


      {/* ========================================
          VISION
          ======================================== */}

      <section className="about-section reveal">

        <p className="eyebrow">
          THE VISION
        </p>

        <div className="about-vision">

          <p>
            History is not just a collection of dates.
          </p>

          <p>
            It is made of people, decisions, conflicts, cultures,
            ambitions, mistakes, discoveries, and stories that
            continue to shape the world we live in.
          </p>

          <p>
            Chronicle aims to make those stories easier to discover
            and easier to keep exploring.
          </p>

        </div>

      </section>


      {/* ========================================
          CONTACT
          ======================================== */}

      <section className="about-section about-contact reveal">

        <p className="eyebrow">
          CONTACT
        </p>

        <div className="about-section-content">

          <h2>
            Have something
            <br />
            to say?
          </h2>

          <div className="about-text">

            <p>
              Whether you have a question, feedback, an idea for
              Chronicle, or simply want to get in touch, feel free
              to reach out.
            </p>

            <a
              href="mailto:Jurisalson@gmail.com"
              className="about-email"
            >
              Jurisalson@gmail.com
            </a>

          </div>

        </div>

      </section>


      {/* ========================================
          THE ROAD AHEAD
          ======================================== */}

      <section className="about-section about-final reveal">

        <p className="eyebrow">
          THE ROAD AHEAD
        </p>

        <div className="about-section-content">

          <h2>
            Just the beginning.
          </h2>

          <div className="about-text">

            <p>
              Chronicle is still growing. There are many ideas planned
              for its future, from better ways to discover historical
              topics to new ways for people to learn, explore, and
              share their interest in history.
            </p>

            <p>
              The project will continue to evolve as new ideas,
              discoveries, and possibilities come along.
            </p>

            <p>
              For now, the goal is simple:
            </p>

            <p>
              <strong>
                Make history easier to explore.
              </strong>
            </p>

          </div>

        </div>

      </section>


      <Footer />

    </main>
  );
}

export default About;