import React from 'react';

const Home = () => {
    return (
        <div
            className="bg-cover bg-center min-h-screen flex flex-col justify-center items-center"
            style={{ backgroundImage: 'url(https://via.placeholder.com/1500)' }}
        >
            <div className="text-center mt-32">
                <h1 className="text-black text-4xl font-bold">WELCOME TO BEAUTIFY</h1>
                <p className="text-gray-600 text-lg mt-4">
                    Your new look is waiting for you!
                </p>
                <button className="bg-gray-800 text-white px-8 py-4 mt-8 rounded-lg">
                    Get Started
                </button>
            </div>
            <footer className="absolute bottom-2 text-gray-600 py-4 text-sm">
                <a href="#" className="mx-2">FAQ</a> |
                <a href="#" className="mx-2">Terms and Conditions</a> |
                <a href="#" className="mx-2">Contact</a> |
                <a href="#" className="mx-2">Support</a>
            </footer>
        </div>
    );
};

export default Home;
