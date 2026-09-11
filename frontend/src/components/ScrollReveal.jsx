import { useEffect } from "react";
import { useLocation } from "react-router-dom";

function ScrollReveal() {
  const location = useLocation();

  useEffect(() => {
    // Chronicle's Search page has its own scroll behavior
    // and section observer. Do not apply global reveals there.
    if (location.pathname === "/search") {
      return;
    }

    const elements = document.querySelectorAll(".reveal");

    if (!elements.length) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("visible");
            observer.unobserve(entry.target);
          }
        });
      },
      {
        threshold: 0.15,
      }
    );

    elements.forEach((element) => {
      observer.observe(element);
    });

    return () => {
      observer.disconnect();
    };
  }, [location.pathname]);

  return null;
}

export default ScrollReveal;