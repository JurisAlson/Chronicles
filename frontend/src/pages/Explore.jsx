import { Link, useSearchParams } from "react-router-dom";
import "./Explore.css";

const collections = {
  ancient: {
    eyebrow: "ANCIENT WORLD",
    title: "The Ancient World",
    description:
      "Civilizations, empires, wars, and people that shaped the foundations of the ancient world.",
    topics: [
      "Roman Republic",
      "Roman Empire",
      "Ancient Greece",
      "Persian Empire",
      "Alexander the Great",
      "Ancient Egypt",
      "Punic Wars",
      "Fall of Rome",
    ],
  },

  medieval: {
    eyebrow: "MEDIEVAL WORLD",
    title: "The Medieval World",
    description:
      "Kingdoms, empires, conflicts, and people that shaped the medieval age.",
    topics: [
      "Byzantine Empire",
      "Viking Age",
      "Norman Conquest",
      "Crusades",
      "Mongol Empire",
      "Hundred Years' War",
      "Black Death",
      "Fall of Constantinople",
    ],
  },

  wars: {
    eyebrow: "WARS & BATTLES",
    title: "Wars & Battles",
    description:
      "Conflicts and battles that changed the course of history.",
    topics: [
      "Battle of Marathon",
      "Battle of Gaugamela",
      "Battle of Cannae",
      "Battle of Hastings",
      "Battle of Tours",
      "Battle of Waterloo",
      "Napoleonic Wars",
      "World War II",
    ],
  },

  people: {
    eyebrow: "PEOPLE & LEADERS",
    title: "People & Leaders",
    description:
      "Rulers, generals, thinkers, and revolutionaries who shaped history.",
    topics: [
      "Julius Caesar",
      "Alexander the Great",
      "Augustus",
      "Cleopatra",
      "Genghis Khan",
      "Saladin",
      "Joan of Arc",
      "Napoleon Bonaparte",
    ],
  },
};

function Explore() {
  const [searchParams] = useSearchParams();

  const category = searchParams.get("category") || "ancient";

  const collection = collections[category] || collections.ancient;

  return (
    <main className="explore-page">

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


      <section className="explore-page-header">

        <p className="explore-page-eyebrow">
          {collection.eyebrow}
        </p>

        <h1 className="explore-page-title">
          {collection.title}
        </h1>

        <p className="explore-page-description">
          {collection.description}
        </p>

      </section>


      <section className="explore-topic-list">

        {collection.topics.map((topic, index) => (

          <Link
            key={topic}
            to={`/search?q=${encodeURIComponent(topic)}`}
            className="explore-topic"
          >

            <span className="explore-topic-number">
              {String(index + 1).padStart(2, "0")}
            </span>

            <span className="explore-topic-title">
              {topic}
            </span>

            <span className="explore-topic-arrow">
              →
            </span>

          </Link>

        ))}

      </section>


      <section className="explore-more">

        <p className="explore-more-eyebrow">
          EXPLORE MORE
        </p>

        <div className="explore-category-list">

          <Link
            to="/explore?category=ancient"
            className="explore-category-link"
          >
            Ancient World
          </Link>

          <Link
            to="/explore?category=medieval"
            className="explore-category-link"
          >
            Medieval World
          </Link>

          <Link
            to="/explore?category=wars"
            className="explore-category-link"
          >
            Wars & Battles
          </Link>

          <Link
            to="/explore?category=people"
            className="explore-category-link"
          >
            People & Leaders
          </Link>

        </div>

      </section>

    </main>
  );
}

export default Explore;