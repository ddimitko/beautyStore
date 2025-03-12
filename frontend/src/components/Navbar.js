import React, {useContext, useEffect, useRef, useState} from "react";
import AuthModal from "./AuthModal";
import {useAuth} from "./AuthContext";
import {Link, useLocation, useNavigate} from "react-router-dom";
import {TextInput} from "flowbite-react";
import {CiSearch} from "react-icons/ci";
import {SearchContext} from "./SearchProvider";
import useWebSocket from "./hooks/useWebSocket";

function Navbar() {
    const { isAuthenticated, user, logout } = useAuth();
    const [isModalOpen, setModalOpen] = useState(false);
    const [menuOpen, setMenuOpen] = useState(false);
    const [notifOpen, setNotifOpen] = useState(false);
    const location = useLocation();
    const menuRef = useRef(null);
    const notifRef = useRef(null);
    const debounceTimeout = useRef(null);

    const { setSearchTerm } = useContext(SearchContext);
    const navigate = useNavigate();
    const [previousPage, setPreviousPage] = useState(null);

    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);

    // Function to determine if a link is active
    const isActive = (path) => location.pathname === path;

    const fetchNotifications = async () => {
        try {
            const response = await fetch("http://localhost:8080/api/notifications", {
                method: "GET",
                credentials: "include", // Ensures cookies (tokens) are sent with the request
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error("Failed to fetch notifications");
            }

            const data = await response.json();
            setNotifications(data);
        } catch (error) {
            console.error("Error fetching notifications:", error);
        }
    };

    useEffect(() => {
        if (isAuthenticated && user?.id) {
            fetchNotifications();
        }
    }, [isAuthenticated, user]);  // Ensure the user is available


    useEffect(() => {
        if (location.pathname !== "/shops") {
            setPreviousPage(location.pathname);
        }
    }, [location]);

    const handleSearchChange = (e) => {
        const value = e.target.value;
        setSearchTerm(value);

        // Clear any existing timeout
        if(debounceTimeout.current) {
            clearTimeout(debounceTimeout);
        }

        // Set a new timeout
        debounceTimeout.current = setTimeout(() => {
            if (value.trim() === "") {
                if (previousPage) {
                    navigate(previousPage);
                }
            } else {
                navigate("/shops");
            }
        }, 500); // Adjust debounce delay as needed
    };

    // Close the dropdown if clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            // Check if the click is outside the dropdown menu
            if (menuRef.current && !menuRef.current.contains(event.target)
                && notifRef.current && !notifRef.current.contains(event.target)) {
                setMenuOpen(false);
                setNotifOpen(false);
            }
        };

        // Add event listener
        document.addEventListener('mousedown', handleClickOutside);

        // Cleanup event listener
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [menuRef, notifRef]);

    const handleToggleMenu = () => {
        setMenuOpen((prev) => !prev);
        setNotifOpen(false);
    }

    const handleToggleNotifications = async () => {
        if (!notifOpen) {
            setNotifOpen(true);
            setMenuOpen(false);

            // Mark all as read when opening
            await fetch(`http://localhost:8080/api/notifications/read-all/`, {
                method: "PUT",
                credentials: "include",
            });

            setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
            setUnreadCount(0);
        } else {
            setNotifOpen(false);
        }
    }

    const handleNewNotification = (newNotification) => {
        setNotifications((prev) => {
            const updatedNotifications = [...prev, newNotification].sort(
                (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
            );

            // Play notification sound for important updates
            if (newNotification) {
                const audio = new Audio("/notification.wav");
                audio.play();
            }

            return updatedNotifications;
        });

        setUnreadCount((prev) => prev + 1);
    };

    const markAsRead = async (notificationId) => {
        await fetch(`http://localhost:8080/api/notifications/read/${notificationId}`, {
            method: "POST",
        });

        setNotifications((prev) =>
            prev.map((n) =>
                n.id === notificationId ? { ...n, read: true } : n
            )
        );
    };

    const shouldSubscribe = isAuthenticated && !!user?.id;
    const notificationTopic = shouldSubscribe ? `/topic/notifications/${user.id}` : null;

    useWebSocket(
        "http://localhost:8080/ws",
        notificationTopic,
        handleNewNotification,
        shouldSubscribe
    );


    return (
        <>
            <nav className="bg-transparent">
                <div className="mx-auto max-w-7xl px-2 sm:px-6 lg:px-8">
                    <div className="relative flex h-16 items-center justify-between">
                        <div className="absolute inset-y-0 left-0 flex items-center sm:hidden">
                            {/*}Mobile menu button*/}
                            <button type="button"
                                    className="relative inline-flex items-center justify-center rounded-md p-2 text-gray-400 hover:bg-gray-700 hover:text-white focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
                                    aria-controls="mobile-menu" aria-expanded="false">
                                <span className="absolute -inset-0.5"></span>
                                <span className="sr-only">Open main menu</span>

                                <svg className="block size-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5"
                                     stroke="currentColor" aria-hidden="true" data-slot="icon">
                                    <path strokeLinecap="round" strokeLinejoin="round"
                                          d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"/>
                                </svg>

                                <svg className="hidden size-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5"
                                     stroke="currentColor" aria-hidden="true" data-slot="icon">
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12"/>
                                </svg>
                            </button>
                        </div>
                        <div className="flex flex-1 items-center justify-center sm:items-stretch sm:justify-start">
                            <div className="flex shrink-0 items-center">
                                <img className="h-8 w-auto"
                                     src="/apple-logo.svg"
                                     alt="Your Company"/>
                            </div>
                            <div className="hidden sm:ml-6 sm:block">
                                <div className="flex space-x-4">
                                    {/*}Current: "bg-gray-900 text-white", Default: "text-gray-300 hover:bg-gray-700 hover:text-white" */}
                                    <Link to="/" className={`rounded-md px-3 py-2 text-sm font-medium ${
                                        isActive('/') ? 'bg-gray-900 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                                    }`}>Home</Link>
                                    <Link to="/shops" className={`rounded-md px-3 py-2 text-sm font-medium ${
                                        isActive('/shops') ? 'bg-gray-900 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'
                                    }`}>Shops</Link>
                                </div>
                            </div>
                        </div>
                        <div className="max-w-md">
                            <TextInput id="searchBar" type="searchBar" icon={CiSearch} placeholder="Search..." onChange={handleSearchChange} />
                        </div>

                        {isAuthenticated ? (
                            <div
                                className="relative flex rounded-full bg-gray-200 ml-2 text-sm focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800" ref={notifRef}>
                                <button type="button"
                                        id="notif-menu-button"
                                        aria-expanded="false"
                                        className="relative rounded-full bg-transparent p-1 text-gray-400 hover:text-gray-800 hover:shadow focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800"
                                onClick={handleToggleNotifications}>
                                <span className="absolute -inset-1.5"></span>
                                <span className="sr-only">View notifications</span>
                                <svg className="size-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5"
                                     stroke="currentColor" aria-hidden="true" data-slot="icon">
                                    <path strokeLinecap="round" strokeLinejoin="round"
                                          d="M14.857 17.082a23.848 23.848 0 0 0 5.454-1.31A8.967 8.967 0 0 1 18 9.75V9A6 6 0 0 0 6 9v.75a8.967 8.967 0 0 1-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 0 1-5.714 0m5.714 0a3 3 0 1 1-5.714 0"/>
                                </svg>
                                    {unreadCount > 0 && (
                                        <span className="absolute -top-1 -right-1 bg-red-600 text-white text-xs px-1 rounded-full">
                                                {unreadCount}
                                            </span>
                                    )}
                            </button>
                                {notifOpen && (
                                    <div
                                        className="absolute right-0 z-10 top-full mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black/5 focus:outline-none"
                                        role="menu" aria-orientation="vertical" aria-labelledby="notif-menu-button"
                                        tabIndex="-1">
                                        <div className="p-2">
                                            {notifications.length > 0 ? (
                                                notifications.map((n) => (
                                                    <div key={n.id} className="p-2 border-b last:border-none text-sm text-gray-700">
                                                        <p className="p-2 text-center w-auto">{n.message}</p>
                                                        <small className="text-xs text-gray-500 text-center">
                                                            {new Date(n.createdAt).toLocaleString()}
                                                        </small>
                                                    </div>
                                                ))
                                            ) : (
                                                <p className="p-2 text-center text-sm text-gray-500">No new notifications</p>
                                            )}
                                        </div>
                                    </div>
                                )}

                            {/*}Profile dropdown*/}
                            <div className="relative ml-3" ref={menuRef}>
                                <div>
                                    <button type="button"
                                            className="relative flex rounded-full bg-gray-800 text-sm focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800"
                                            id="user-menu-button" aria-expanded="false" aria-haspopup="true"
                                    onClick={handleToggleMenu}>
                                        <span className="absolute -inset-1.5"></span>
                                        <span className="sr-only">Open user menu</span>
                                        <img className="size-8 rounded-full"
                                             src={user.profilePicture}
                                             alt=""/>
                                    </button>
                                </div>

                                {menuOpen && (
                                <div
                                    className="absolute right-0 z-10 top-full mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black/5 focus:outline-none"
                                    role="menu" aria-orientation="vertical" aria-labelledby="user-menu-button"
                                    tabIndex="-1">
                                    <a className="block px-4 py-2 text-sm font-thin" role="menuitem" disabled>{user.firstName + " " + user.lastName}</a>
                                    <Link to="/profile" className="block px-4 py-2 text-sm text-gray-700 hover:shadow" role="menuitem"
                                       tabIndex="-1" id="user-menu-item-0">Account Details</Link>
                                    {(user?.role === "ROLE_OWNER" || user?.role === "ROLE_EMPLOYEE") && (
                                        <Link to="/dashboard" className="block px-4 py-2 text-sm text-gray-700 hover:shadow" role="menuitem" tabIndex="-1" id="user-menu-item-1">
                                            Dashboard</Link>
                                    )}
                                    <Link to="/appointments" className="block px-4 py-2 text-sm text-gray-700 hover:shadow" role="menuitem"
                                       tabIndex="-1" id="user-menu-item-2">Appointments</Link>
                                    <a href="#" className="block px-4 py-2 text-sm text-gray-700 hover:shadow" role="menuitem"
                                       tabIndex="-1" id="user-menu-item-3" onClick={logout}>Sign out</a>
                                </div>
                                )}
                            </div>
                        </div>
                        ):(
                            <button
                                onClick={() => setModalOpen(true)}
                                className="bg-gray-800 text-white px-3.5 ml-2 py-2 rounded-lg"
                            >
                                Join Now
                            </button>
                        )}
                    </div>
                </div>

                {/*}Mobile menu, show/hide based on menu state.*/}
                <div className="sm:hidden" id="mobile-menu">
                    <div className="space-y-1 px-2 pb-3 pt-2">
                        {/*Current: "bg-gray-900 text-white", Default: "text-gray-300 hover:bg-gray-700 hover:text-white"*/}
                        <a href="#" className="block rounded-md bg-gray-900 px-3 py-2 text-base font-medium text-white"
                           aria-current="page">Dashboard</a>
                        <a href="#"
                           className="block rounded-md px-3 py-2 text-base font-medium text-gray-300 hover:bg-gray-700 hover:text-white">Team</a>
                        <a href="#"
                           className="block rounded-md px-3 py-2 text-base font-medium text-gray-300 hover:bg-gray-700 hover:text-white">Projects</a>
                        <a href="#"
                           className="block rounded-md px-3 py-2 text-base font-medium text-gray-300 hover:bg-gray-700 hover:text-white">Calendar</a>
                    </div>
                </div>
            </nav>

            {/*CustomerCreation Modal*/}
            <AuthModal
                isOpen={isModalOpen}
                onClose={() => setModalOpen(false)}
            />
        </>
    );
}

export default Navbar;


