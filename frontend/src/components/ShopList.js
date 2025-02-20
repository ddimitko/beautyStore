// src/App.js
import React, {useState, useEffect, useContext} from 'react';
import {Link} from "react-router-dom";
import {SearchContext} from "./SearchProvider";

function ShopList() {

    const { searchTerm } = useContext(SearchContext);
    const [shopList, setShopList] = useState([]);

    useEffect(() => {
        const delaySearch = setTimeout(() => {
            fetchShops();
        }, 300); // Debounce API calls

        return () => clearTimeout(delaySearch); // Cleanup on every keystroke
    }, [searchTerm]); // Fetch whenever searchTerm changes

    const fetchShops = async () => {
        let url = "http://localhost:8080/api/shops"; // Default: fetch all shops

        if (searchTerm) {
            url += `?shopName=${encodeURIComponent(searchTerm)}`;
        }

        try {
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error("Failed to fetch shops");
            }
            const data = await response.json();
            setShopList(data);
        } catch (error) {
            console.error("Error fetching shops:", error);
            setShopList([]); // Clear results on error
        }
    };

    return (
        <div className="bg-white">
            <div className="mx-auto max-w-2xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                <div className="mt-6 grid grid-cols-1 gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-4 xl:gap-x-8">
                    {shopList.length === 0 && searchTerm && <p>No shops found.</p>}
                    {shopList.map((shop) => (
                        <div key={shop.id} className="group relative">
                            <img
                                src={shop.images.find(image => {
                                    return image.main;
                                })?.url || "default-thumbnail.jpg"}
                                className="aspect-square w-full rounded-md bg-gray-200 object-fit group-hover:opacity-75 lg:aspect-auto lg:h-80"
                            />
                            <div className="mt-4 flex justify-between">
                                <div>
                                    <h3 className="text-sm text-gray-700">
                                        <Link to = {`/shops/${shop.id}`} state = {{shop: 'shop'}}>
                                            <span aria-hidden="true" className="absolute inset-0" />
                                            {shop.name}
                                        </Link>
                                    </h3>
                                    <p className="mt-1 text-sm text-gray-500">{shop.location}</p>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default ShopList;
