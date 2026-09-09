import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from "./pages/Home";
import Search from "./pages/Search";
import Explore from "./pages/Explore";
import About from "./pages/About";
import ScrollReveal from "./components/ScrollReveal";

import "./App.css";

function App() {
  return (
    <BrowserRouter>

      <ScrollReveal />

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/search" element={<Search />} />
        <Route path="/explore" element={<Explore />} />
        <Route path="/about" element={<About />} />
      </Routes>

    </BrowserRouter>
  );
}

export default App;