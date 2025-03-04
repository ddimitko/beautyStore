import { useEffect, useRef } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const useWebSocket = (url, topic, onMessageReceived, shouldConnect) => {
    const stompClientRef = useRef(null);

    useEffect(() => {
        if (!shouldConnect || !topic) return; // Prevent connection if not needed

        // Reuse existing connection
        if (!stompClientRef.current) {
            console.log("Opening WebSocket connection...");
            const socket = new SockJS(url);
            const stompClient = new Client({
                webSocketFactory: () => socket,
                debug: (msg) => console.log(msg),
                reconnectDelay: 5000, // Auto-reconnect on failure
            });

            stompClient.onConnect = () => {
                console.log("Connected to WebSocket");
                stompClient.subscribe(topic, (message) => {
                    onMessageReceived(JSON.parse(message.body));
                });
            };

            stompClient.activate();
            stompClientRef.current = stompClient; // Store the client
        } else {
            // If already connected, just resubscribe
            console.log(`Resubscribing to: ${topic}`);
            stompClientRef.current.subscribe(topic, (message) => {
                onMessageReceived(JSON.parse(message.body));
            });
        }

        return () => {
            // Cleanup only if the modal closes or WebSocket is no longer needed
            if (!shouldConnect) {
                console.log("Deactivating WebSocket...");
                stompClientRef.current?.deactivate();
                stompClientRef.current = null;
            }
        };
    }, [url, topic, shouldConnect]); // 👈 Only re-run when `topic` or `shouldConnect` changes
};

export default useWebSocket;

