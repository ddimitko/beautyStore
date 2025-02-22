import DashboardNavbar from "./DashboardNavbar";
import {useAuth} from "../AuthContext";
import {Navigate, Route, Routes} from "react-router-dom";
import AdminDashboard from "./AdminDashboard";
import EmployeeDashboard from "./EmployeeDashboard";
import ShopProfile from "./ShopProfile";
import Payments from "./Payments";

const MainDashboard = () => {

    const { user, shops, isAuthenticated } = useAuth();

    if (!isAuthenticated || (user.role !== "ROLE_EMPLOYEE" && user.role !== "ROLE_OWNER")) {
        return <Navigate to="/" />;
    }


    return(
        <div>
        <DashboardNavbar/>
            <Routes>
                {user.role === "ROLE_OWNER" ? (
                    <>
                    <Route index element={<AdminDashboard shop={shops} />} />
                    <Route path="payments" element={<Payments />} />
                    </>
                ) : user.role === "ROLE_EMPLOYEE" && (
                        <Route index element={<EmployeeDashboard />} />
                    )}
                <Route path="shop/:id" element={<ShopProfile user={user}/>} />
            </Routes>
        </div>
    );
};
export default MainDashboard;