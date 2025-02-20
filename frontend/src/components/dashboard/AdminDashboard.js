import React, {useEffect, useState} from "react";
import ImageUpload from "./ImageUpload";
import { Accordion } from "flowbite-react";
import {Link} from "react-router-dom";

const AdminDashboard = () => {
    const [shops, setShops] = useState([]);
    const [error, setError] = useState(null);

    // Fetch owned or assigned shops on component mount
    useEffect(() => {
        fetchOwnedShops();
    }, []);

    const fetchOwnedShops = async () => {
        try {
            const response = await fetch("http://localhost:8080/api/management/shops", {
                method: "GET",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (!response.ok) {
                throw new Error("Failed to fetch shops");
            }

            const shopData = await response.json();
            setShops(shopData);
            console.log(shopData);
        } catch (err) {
            console.error("Error fetching shops:", err.message);
            setError(err.message);
        }
    };

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold mb-4">Admin Dashboard</h2>
            {shops.length > 0 && (
            <Accordion collapseAll>
                {shops.map((shop) => (
                <Accordion.Panel key={shop.id}>
                    <Accordion.Title>{shop.name}</Accordion.Title>
                    <Accordion.Content>
                        {error && <div className="text-red-500">{error}</div>}
                            <div className="p-5 border rounded-lg shadow-md">
                                <p className="text-gray-600">Address: {shop.location}</p>
                                <p className="text-gray-600">Category: {shop.category}</p>
                                <p className="text-gray-600">Contact: {shop.phone}</p>
                                <p className="text-gray-600">Employees: {shop.employeeList.length}</p>
                            </div>
                        <div className="mt-3">
                            <Link
                                to={`/dashboard/shop/${shop.id}`}
                                className="bg-gray-700 text-white px-4 py-2 rounded-lg hover:bg-gray-500"
                            >
                                Shop Details
                            </Link>
                            <Link to={`/dashboard/payments`}
                                  state = {{shopId: shop.id}}
                                  className="bg-gray-700 text-white px-4 py-2 rounded-lg hover:bg-gray-500"
                            >
                                Payments
                            </Link>
                        </div>
                    </Accordion.Content>
                </Accordion.Panel>
                    ))}
            </Accordion>
            )}
        </div>
    );
};

export default AdminDashboard;
