import React, {useEffect, useRef, useState} from "react";
import {Link, useLocation} from "react-router-dom";
import {useAuth} from "../AuthContext";

function DashboardNavbar() {

    const [menuOpen, setMenuOpen] = useState(false);
    const location = useLocation();
    const menuRef = useRef(null);
    const {user, logout} = useAuth();

    // Close the dropdown if clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            // Check if the click is outside the dropdown menu
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setMenuOpen(false);
            }
        };

        // Add event listener
        document.addEventListener('mousedown', handleClickOutside);

        // Cleanup event listener
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    return (
        <header className="bg-transparent">
            <nav className="mx-auto flex max-w-7xl items-center justify-between p-3 lg:px-8" aria-label="Global">
                <div className="flex lg:flex-1">
                    <a href="#" className="-m-1.5 p-1.5">
                        <span className="sr-only">Your Company</span>
                        <img className="h-8 w-auto"
                             src="https://tailwindui.com/plus/img/logos/mark.svg?color=indigo&shade=600" alt=""/>
                    </a>
                </div>
                <div className="flex lg:hidden">
                    <button type="button"
                            className="-m-2.5 inline-flex items-center justify-center rounded-md p-2.5 text-gray-700">
                        <span className="sr-only">Open main menu</span>
                        <svg className="size-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor"
                             aria-hidden="true" data-slot="icon">
                            <path strokeLinecap="round" strokeLinejoin="round"
                                  d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"/>
                        </svg>
                    </button>
                </div>
                <div className="hidden lg:flex lg:gap-x-12">
                    <div className="relative">
                        <button type="button"
                                className="flex items-center gap-x-1 text-sm/6 font-semibold text-gray-900"
                                aria-expanded="false">
                            Overview
                        </button>
                    </div>
                    <Link to="/dashboard/payments" className="text-sm/6 font-semibold text-gray-900">Payments</Link>
                </div>
                <div className="hidden lg:flex lg:flex-1 lg:justify-end">
                    <div className="relative ml-3" ref={menuRef}>
                        <div>
                            <button type="button"
                                    className="relative flex rounded-full bg-gray-800 text-sm focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800"
                                    id="user-menu-button" aria-expanded="false" aria-haspopup="true"
                                    onClick={() => setMenuOpen(!menuOpen)}>
                                <span className="absolute -inset-1.5"></span>
                                <span className="sr-only">Open user menu</span>
                                <img className="size-8 rounded-full"
                                     src={user.profilePicture}
                                     alt=""/>
                            </button>
                        </div>

                        {menuOpen && (
                            <div
                                className="absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black/5 focus:outline-none"
                                role="menu" aria-orientation="vertical" aria-labelledby="user-menu-button"
                                tabIndex="-1">
                                {/*}Active: "bg-gray-100 outline-none", Not Active: "" */}
                                <a className="block px-4 py-2 text-sm font-thin" role="menuitem" disabled>{user.firstName + " " + user.lastName}</a>
                                <Link to="/" className="block px-4 py-2 text-sm text-gray-700 hover:shadow" role="menuitem"
                                   tabIndex="-1" id="user-menu-item-0">Back to Main</Link>
                                <Link to="/" className="block px-4 py-2 text-sm text-gray-700 hover:shadow" role="menuitem"
                                      tabIndex="-1" id="user-menu-item-3" onClick={logout}>Sign out</Link>
                            </div>
                        )}
                    </div>
                </div>
            </nav>
            <div className="lg:hidden" role="dialog" aria-modal="true">
                <div className="fixed inset-0 z-10"></div>
                <div
                    className="fixed inset-y-0 right-0 z-10 w-full overflow-y-auto bg-white px-6 py-6 sm:max-w-sm sm:ring-1 sm:ring-gray-900/10">
                    <div className="flex items-center justify-between">
                        <a href="#" className="-m-1.5 p-1.5">
                            <span className="sr-only">Your Company</span>
                            <img className="h-8 w-auto"
                                 src="https://tailwindui.com/plus/img/logos/mark.svg?color=indigo&shade=600" alt=""/>
                        </a>
                        <button type="button" className="-m-2.5 rounded-md p-2.5 text-gray-700">
                            <span className="sr-only">Close menu</span>
                            <svg className="size-6" fill="none" viewBox="0 0 24 24" strokeWidth="1.5"
                                 stroke="currentColor" aria-hidden="true" data-slot="icon">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12"/>
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        </header>
    )
}

export default DashboardNavbar;