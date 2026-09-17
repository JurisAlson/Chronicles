# Chronicle

### History without the rabbit hole.

Chronicle is a historical reference web application designed to make exploring history simpler and more focused.

Instead of presenting users with an overwhelming amount of information at once, Chronicle provides curated historical topics and lets users explore individual subjects through a clean, distraction-free interface.

The project was built as a full-stack portfolio project to practice frontend development, backend API integration, and working with external data sources.

---

## Features

* Search historical topics using Wikipedia's API
* Browse curated historical categories
* Explore Ancient World, Medieval World, Wars & Battles, and People & Leaders
* View article introductions and historical sections
* Load section content only when needed
* Responsive dark editorial-style interface
* Smooth page transitions
* Scroll-based content reveal animations
* Direct navigation between curated topics and search results

---

## How It Works

Chronicle uses a simple architecture:

```text
User
 ↓
React Frontend
 ↓
Spring Boot Backend
 ↓
Wikipedia API
 ↓
Spring Boot
 ↓
React
```

The frontend does not communicate directly with Wikipedia.

Instead, requests are handled by the Spring Boot backend, which communicates with Wikipedia and returns the required information to the frontend.

This also allows Chronicle to control how much data is requested.

For example, article sections are loaded only when the user actually opens them instead of requesting every section immediately.

---

## Tech Stack

### Frontend

* React
* Vite
* React Router
* JavaScript
* CSS

### Backend

* Java 17
* Spring Boot
* Maven
* Spring `RestClient`

### External API

* Wikipedia API

### Deployment

Planned deployment:

* Frontend: Vercel
* Backend: Render

---

## Project Structure

```text
Chronicles/
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Footer.jsx
│   │   │   ├── PageTransition.jsx
│   │   │   └── ScrollReveal.jsx
│   │   │
│   │   ├── pages/
│   │   │   ├── Home.jsx
│   │   │   ├── Search.jsx
│   │   │   ├── Explore.jsx
│   │   │   └── About.jsx
│   │   │
│   │   └── ...
│   │
│   └── package.json
│
└── backend/
    ├── src/
    │   └── main/
    │       └── java/
    │
    ├── pom.xml
    └── mvnw
```

---

## Running Locally

### Requirements

* Java 17+
* Node.js
* npm

### Backend

Navigate to the backend:

```bash
cd backend
```

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

### Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

---

## API Endpoints

Chronicle's backend exposes a small set of endpoints:

```text
GET /api/history/search?query=
```

Search Wikipedia for historical topics.

```text
GET /api/history/article?pageId=
```

Retrieve an article introduction.

```text
GET /api/history/sections?pageId=
```

Retrieve the available article sections.

```text
GET /api/history/section?pageId=&sectionIndex=
```

Retrieve the content of a specific section.

---

## Request Efficiency

One of the main technical considerations in Chronicle is limiting unnecessary requests to the Wikipedia API.

The initial article request is intentionally kept lightweight:

```text
Search
   ↓
Article
   ↓
Section list
```

Individual section content is fetched only when the user requests it.

This avoids loading large amounts of information that the user may never read and helps reduce unnecessary API traffic.

---

## Design

Chronicle uses a dark editorial-inspired interface with:

* Playfair Display for historical headings
* DM Sans for interface text
* Large typography
* Minimal borders
* Generous whitespace
* Subtle transitions and animations

The goal is to make the application feel more like a digital historical reference book than a traditional information-heavy website.

---

## Future Plans

Chronicle is currently focused on its core historical browsing and search experience.

Possible future improvements include:

### 1. Smarter Search

Improve search with better historical topic matching, filters, related searches, and more useful search suggestions.

### 2. AI-Powered Summaries

Add optional AI-generated summaries that can explain long historical articles in a shorter and easier-to-understand format.

The goal would be to summarize information without replacing the original historical source.

### 3. Discussion Board

Allow users to discuss historical events, people, and interpretations.

Possible features could include topic discussions, replies, and community conversations.

### 4. Interactive Timelines

Add visual timelines connecting major events, people, wars, and civilizations.

This would make it easier to understand historical events in chronological context.

### 5. Accounts & Bookmarks

Allow users to create accounts, save historical topics, and build personal reading lists.

---

## What I Learned

Chronicle was built to practice several areas of full-stack development, including:

* Building a React application from scratch
* Creating a Spring Boot REST API
* Integrating an external API
* Working with asynchronous frontend requests
* Managing application state
* Using React Router
* Designing reusable components
* Handling API request efficiency
* Lazy-loading content
* Building responsive interfaces
* Connecting a frontend and backend for deployment

---

## Data Source

Chronicle uses the Wikipedia API to retrieve historical information.

The application is an independent project and is not affiliated with or endorsed by the Wikimedia Foundation.

---

## Status

**In development**

The core browsing and search experience is functional. The project is being prepared for public deployment and further improvements.

---

## License

This project is intended primarily as a personal portfolio and learning project.
