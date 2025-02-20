import React, {createContext, useState, useEffect, useContext} from "react";

export const AuthContext = createContext();

const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [user, setUser] = useState(null);
    const [shops, setShops] = useState([]);
    const [loading, setLoading] = useState(true); // Tracks whether auth state is being checked

    // Fetch user data on app load
    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await fetch("http://localhost:8080/api/me", {
                    method: "GET",
                    credentials: "include", // Ensures cookies are sent
                });

                if (!response.ok) {
                    throw new Error("User not authenticated");
                }

                const data = await response.json();
                console.log(data);
                setUser(data);
                setIsAuthenticated(true);
            } catch (error) {
                setIsAuthenticated(false);
                setUser(null);
            } finally {
                setLoading(false); // Stop loading after request completes
            }
        };

        fetchUser();
    }, []);

    // Login function (after successful authentication)
    const login = (userDetails) => {
        setIsAuthenticated(true);
        setUser(userDetails);
    };

    // Logout function
    const logout = async () => {
        try {
            await fetch("http://localhost:8080/auth/logout", {
                method: "POST",
                credentials: "include", // Include cookies with the request
            });

            // Clear auth state
            setIsAuthenticated(false);
            setUser(null);
        } catch (error) {
            console.error("Error during logout:", error);
        }
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, user, shops, login, logout, loading }}>
            {!loading && children} {/* Render children only after auth check */}
        </AuthContext.Provider>
    );
};

export default AuthProvider;

export const useAuth = () => useContext(AuthContext);
