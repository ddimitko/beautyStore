import {
    CheckIcon,
    LinkIcon,
    MapPinIcon,
    PencilIcon,
} from '@heroicons/react/20/solid'
import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import AppointmentCreationModal from "./appointmentCreation/AppointmentCreationModal";
import {Carousel} from "flowbite-react";

export default function ShopPage() {
    const [shop, setShop] = useState([]);
    const param = useParams();
    const [loading, setLoading] = useState(true);

    const [isModalOpen, setModalOpen] = useState(false);

    useEffect(() => {
        fetch(`http://localhost:8080/api/shops/${param.id}`, {
            method: 'GET',
            credentials: "include",
        })
            .then(response => response.json())
            .then(data => {
                setShop(data);
                console.log(data);
                setLoading(false);
            })
            .catch(error => {
                console.error('Error fetching the shop data:', error);
                setLoading(false);
            });
    }, []);

    if (loading) {
        return null;
    }

    return (
            <div className="w-full">
                {/* Carousel Section */}
                <div className="relative h-56 sm:h-64 xl:h-80 2xl:h-96 mb-6">
                    {/* Check if there are images before rendering */}
                    {shop.images.length > 0 ? (
                        <div className="absolute inset-0 flex justify-center items-center">
                            <Carousel>
                                {shop.images.map(image => (
                                    <img
                                        key={image.id}
                                        src={image.url}
                                        className="object-contain w-full h-full"
                                    />
                                ))}
                            </Carousel>
                        </div>
                    ) : (
                        // Show a placeholder if no images are available
                        <div className="absolute inset-0 flex justify-center items-center">
                            <img
                                src="https://via.placeholder.com/500x500?text=No+Images"
                                alt="No images available"
                                className="object-cover w-full h-full"
                            />
                        </div>
                    )}
                </div>
            <div className="flex lg:items-center pt-3 pb-3 pl-3 pr-3 lg:justify-between">
                <div className="min-w-0 flex-1">
                    <h2 className="text-2xl/7 font-bold text-gray-900 sm:truncate sm:text-3xl sm:tracking-tight">
                        { shop.name }
                    </h2>
                    <div className="mt-1 flex flex-col sm:mt-0 sm:flex-row sm:flex-wrap sm:space-x-6">
                        <div className="mt-2 flex items-center text-sm text-gray-500">
                            <MapPinIcon aria-hidden="true" className="mr-1.5 size-5 shrink-0 text-gray-400" />
                            {shop.location}
                        </div>
                    </div>
                </div>
                <div className="mt-5 flex lg:mt-0 lg:ml-4">
                    <span className="sm:ml-3">
                    <button
                        type="button"
                        className="inline-flex items-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                        onClick={() => setModalOpen(true)}
                    >
                        <CheckIcon aria-hidden="true" className="mr-1.5 -ml-0.5 size-5" />
                        Book Now
                    </button>
                </span>
                </div>
            </div>
            {/* 🛑 Place Modal at the End of Return */}
            <AppointmentCreationModal
                isOpen={isModalOpen}
                onClose={() => setModalOpen(false)}
                shop={shop}
            />
            </div>

    );

}