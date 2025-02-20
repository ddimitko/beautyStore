import { useLocation } from "react-router-dom";
import Navbar from "./Navbar";

const NavbarController = () => {
    const location = useLocation();

    // Hide Navbar only for dashboard-related routes
    const hideNavbar = location.pathname.startsWith("/dashboard");

    return !hideNavbar ? <Navbar /> : null;
};

export default NavbarController;