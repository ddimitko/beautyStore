import jwt from "jsonwebtoken";

const jwtService = {
    // Decode a JWT token to extract its payload
    decodeToken: (token) => {
        if (!token) return null;

        try {
            return jwt.decode(token); // Returns the decoded payload
        } catch (error) {
            console.error("Error decoding token:", error);
            return null;
        }
    },

    // Check if a token is valid and not expired
    isTokenValid: (token) => {
        if (!token) return false;

        try {
            const decoded = jwt.decode(token);
            if (!decoded || !decoded.exp) return false;

            // Check if the token is expired
            const currentTime = Math.floor(Date.now() / 1000); // Current time in seconds
            return decoded.exp > currentTime;
        } catch (error) {
            console.error("Error validating token:", error);
            return false;
        }
    },
};

export default jwtService;
