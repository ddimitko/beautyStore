import { useState } from "react";
import { Snackbar, Alert } from "@mui/material";

const ImageUpload = ({ shopId, userId, isProfilePicture = false }) => {
    const [uploading, setUploading] = useState(false);
    const [uploadSuccess, setUploadSuccess] = useState(false);
    const [uploadError, setUploadError] = useState(false);

    // Validate image file type
    const validateImageFile = (file) => {
        const validImageTypes = ["image/jpeg", "image/png", "image/jpg"];
        return validImageTypes.includes(file.type);
    };

    // Handle file selection
    const handleFileSelect = async (event) => {
        const files = Array.from(event.target.files);
        if (!files.length) return;

        // Check file restrictions
        if (isProfilePicture && files.length > 1) {
            setUploadError(true);
            alert("You can only upload one profile picture.");
            return;
        } else if (!isProfilePicture && files.length > 3) {
            setUploadError(true);
            alert("You can upload up to 3 shop images at a time.");
            return;
        }

        setUploading(true);

        // Check all selected files for valid types
        const invalidFiles = files.filter((file) => !validateImageFile(file));
        if (invalidFiles.length > 0) {
            setUploadError(true);
            setUploading(false);
            return;
        }

        const formData = new FormData();
        files.forEach((file) => formData.append("file", file));

        // Determine the correct upload URL
        const uploadUrl = isProfilePicture
            ? `http://localhost:8080/api/profile/${userId}/upload`
            : `http://localhost:8080/api/shops/${shopId}/upload`;

        try {
            const response = await fetch(uploadUrl, {
                method: "PUT",
                credentials: "include",
                body: formData,
            });

            if (!response.ok) {
                throw new Error("Upload failed.");
            }

            setUploadSuccess(true);
        } catch (error) {
            console.error("Upload failed", error);
            setUploadError(true);
        } finally {
            setUploading(false);
        }
    };

    return (
        <>
            <input
                id="image-upload-input"
                type="file"
                accept="image/jpeg, image/png, image/jpg"
                multiple={!isProfilePicture} // Allow multiple files for shop images
                style={{ display: "none" }}
                onChange={handleFileSelect}
                disabled={uploading}
            />

            {/* Success Alert */}
            <Snackbar open={uploadSuccess} autoHideDuration={3000} onClose={() => setUploadSuccess(false)}>
                <Alert severity="success" onClose={() => setUploadSuccess(false)}>Upload successful!</Alert>
            </Snackbar>

            {/* Error Alert */}
            <Snackbar open={uploadError} autoHideDuration={3000} onClose={() => setUploadError(false)}>
                <Alert severity="error" onClose={() => setUploadError(false)}>Upload failed. Please, try again.</Alert>
            </Snackbar>
        </>
    );
};

export default ImageUpload;


