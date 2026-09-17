import { useLocation } from "react-router-dom";
import "./PageTransition.css";

function PageTransition({ children }) {
  const location = useLocation();

  return (
    <div key={location.pathname + location.search} className="page-transition">
      {children}
    </div>
  );
}

export default PageTransition;