import React from 'react';
import Home from './components/Home';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import AuthProvider from "./components/AuthContext";
import About from "./components/About";
import ShopList from "./components/ShopList";
import ShopPage from "./components/ShopPage";
import AppointmentsUser from "./components/AppointmentsUser";
import MainDashboard from "./components/dashboard/MainDashboard";
import NavbarController from "./components/NavbarController";
import Profile from "./components/Profile";
import {SearchProvider} from "./components/SearchProvider";

function App() {

    return (
        <AuthProvider>
                <BrowserRouter>
                    <SearchProvider>
                    <NavbarController/>
                        <Routes>
                            <Route exact path="/" element={<Home />} />
                            <Route path="/about" element={<About />} />
                            <Route path="/shops" element={<ShopList />} />
                            <Route exact path="/shops/:id" element={<ShopPage />} />
                            <Route exact path="/appointments" element={<AppointmentsUser />} />
                            <Route exact path="/profile" element={<Profile/>} />
                            <Route path="/dashboard/*" element={<MainDashboard />} />
                        </Routes>
                    </SearchProvider>
                </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
