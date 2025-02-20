import React from "react";

const About = () => {
    return (
        <div
            className="bg-cover bg-center min-h-screen flex flex-col justify-center items-center"
            style={{ backgroundImage: 'url(https://via.placeholder.com/1500)' }}
        >
            <div className="text-center mt-32">
                <h1 className="text-black text-4xl font-bold">About Us</h1>
                <p className="text-gray-600 text-lg mt-4">
                    Test
                </p>
            </div>
            <footer className="absolute bottom-0 text-gray-600 py-4 text-sm">
                <a href="#" className="mx-2">FAQ</a> |
                <a href="#" className="mx-2">Terms and Conditions</a> |
                <a href="#" className="mx-2">Contact</a> |
                <a href="#" className="mx-2">Support</a>
            </footer>
        </div>
    );
};

export default About;