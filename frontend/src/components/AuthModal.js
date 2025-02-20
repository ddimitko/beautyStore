import React, { useState, useContext } from "react";
import { AuthContext } from "./AuthContext";

function AuthModal({ isOpen, onClose }) {
    const { login } = useContext(AuthContext); // Access login function from context
    const [activeTab, setActiveTab] = useState("signup"); // To toggle between tabs

    const [signupData, setSignupData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: ""
    });

    const [loginData, setLoginData] = useState({
        email: "",
        password: ""
    });

    const handleSignupChange = (e) => {
        const { name, value } = e.target;
        setSignupData((prev) => ({
            ...prev,
            [name]: value
        }));
    };

    const handleLoginChange = (e) => {
        const { name, value } = e.target;
        setLoginData((prev) => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSignupSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch("http://localhost:8080/auth/signup", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "credentials": "include",
                },
                body: JSON.stringify(signupData),
                credentials: "include",
            });

            if (!response.ok) {
                const errorText = await response.text(); // Read error message if available
                throw new Error(`Signup failed: ${errorText}`);
            }

            // Safely parse JSON response
            const data = await response.json();
            console.log(data);
            login(data); // Update AuthContext
            alert("Account created successfully!");
            onClose();
        } catch (error) {
            console.error("Error during signup:", error);
            alert("An error occurred during signup. Please try again.");
        }
    };

    const handleLoginSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch("http://localhost:8080/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "credentials": "include",
                },
                body: JSON.stringify(loginData),
                credentials: "include",
            });
            if (response.ok) {
                const data = await response.json(); // Assuming server returns token and user details
                console.log(data);
                login(data); // Update context with token and user details
                alert("Login successful!");
                onClose();
            } else {
                alert("Failed to log in. Please try again.");
            }
        } catch (error) {
            console.error("Error:", error);
            alert("An error occurred. Please try again.");
        }
    };

    if (!isOpen) return null;

    return (
        <div
            className="fixed inset-0 bg-gray-800 bg-opacity-75 flex justify-center items-center z-50"
            onClick={onClose}
        >
            <div
                className="bg-white p-6 rounded-lg shadow-lg w-1/3 relative"
                onClick={(e) => e.stopPropagation()}
            >
                <button
                    onClick={onClose}
                    className="absolute top-2 right-2 text-gray-500 hover:text-black text-xl"
                >
                    &times;
                </button>
                <div className="flex border-b mb-4">
                    <button
                        className={`flex-1 py-2 text-center ${
                            activeTab === "signup"
                                ? "border-b-2 border-blue-500 text-blue-500"
                                : "text-gray-500"
                        }`}
                        onClick={() => setActiveTab("signup")}
                    >
                        Signup
                    </button>
                    <button
                        className={`flex-1 py-2 text-center ${
                            activeTab === "login"
                                ? "border-b-2 border-blue-500 text-blue-500"
                                : "text-gray-500"
                        }`}
                        onClick={() => setActiveTab("login")}
                    >
                        Login
                    </button>
                </div>

                {activeTab === "signup" ? (
                    <form onSubmit={handleSignupSubmit}>
                        <div className="mb-4">
                            <label className="block text-gray-700">First Name</label>
                            <input
                                type="text"
                                name="firstName"
                                value={signupData.firstName}
                                onChange={handleSignupChange}
                                className="w-full p-2 border border-gray-300 rounded-lg"
                                placeholder="Enter your first name"
                                required
                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-gray-700">Last Name</label>
                            <input
                                type="text"
                                name="lastName"
                                value={signupData.lastName}
                                onChange={handleSignupChange}
                                className="w-full p-2 border border-gray-300 rounded-lg"
                                placeholder="Enter your last name"
                                required
                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-gray-700">Email</label>
                            <input
                                type="email"
                                name="email"
                                value={signupData.email}
                                onChange={handleSignupChange}
                                className="w-full p-2 border border-gray-300 rounded-lg"
                                placeholder="Enter your email"
                                required
                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-gray-700">Password</label>
                            <input
                                type="password"
                                name="password"
                                value={signupData.password}
                                onChange={handleSignupChange}
                                className="w-full p-2 border border-gray-300 rounded-lg"
                                placeholder="Enter your password"
                                required
                            />
                        </div>
                        <div className="flex justify-end">
                            <button
                                type="submit"
                                className="bg-gray-800 text-white px-4 py-2 rounded-lg"
                            >
                                Create Account
                            </button>
                        </div>
                    </form>
                ) : (
                    <form onSubmit={handleLoginSubmit}>
                        <div className="mb-4">
                            <label className="block text-gray-700">Email</label>
                            <input
                                type="email"
                                name="email"
                                value={loginData.email}
                                onChange={handleLoginChange}
                                className="w-full p-2 border border-gray-300 rounded-lg"
                                placeholder="Enter your email"
                                required
                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-gray-700">Password</label>
                            <input
                                type="password"
                                name="password"
                                value={loginData.password}
                                onChange={handleLoginChange}
                                className="w-full p-2 border border-gray-300 rounded-lg"
                                placeholder="Enter your password"
                                required
                            />
                        </div>
                        <div className="flex justify-end">
                            <button
                                type="submit"
                                className="bg-gray-800 text-white px-4 py-2 rounded-lg"
                            >
                                Log In
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
}

export default AuthModal;

