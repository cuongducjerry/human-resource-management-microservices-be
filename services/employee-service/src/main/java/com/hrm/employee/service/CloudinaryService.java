package com.hrm.employee.service;

import com.cloudinary.Cloudinary;
import com.hrm.employee.util.error.ImageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_DOC_SIZE = 8 * 1024 * 1024;

    private static final long MAX_FILE_SIZE = 4 * 1024 * 1024;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // ===================== AVATAR =====================
    public String uploadAvatar(MultipartFile file) {
        return uploadImage(file, "avatars_hrm/employees");
    }

    // ===================== FILE =====================
    public String uploadContractFile(MultipartFile file) {
        return uploadDocument(file, "contracts_hrm");
    }


    private String uploadDocument(MultipartFile file, String folder) {

        if (file == null || file.isEmpty()) {
            throw new ImageException("File contract cannot be empty");
        }

        if (!ALLOWED_DOCUMENT_TYPES.contains(file.getContentType())) {
            throw new ImageException("Only PDF or DOC/DOCX allowed");
        }

        if (file.getSize() > MAX_DOC_SIZE) {
            throw new ImageException("File maximum 8MB");
        }

        try {

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", folder,
                            "resource_type", "auto",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {
            e.printStackTrace();
            throw new ImageException("Upload contract failed: " + e.getMessage());
        }
    }


    // ===================== IMAGE =====================
    private String uploadImage(MultipartFile file, String folder) {

        if (file == null || file.isEmpty()) {
            throw new ImageException("File ảnh không được để trống");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new ImageException("Chỉ cho phép upload ảnh JPG, PNG, WEBP");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageException("Ảnh tối đa 4MB");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new ImageException("Upload ảnh thất bại");
        }
    }

    /**
     * Delete photos by URL
     */

    public void deleteAvatar(String imageUrl) {

        try {
            String publicId = extractPublicIdAvatarUser(imageUrl);
            cloudinary.uploader().destroy(publicId, Map.of());

        } catch (Exception e) {
            throw new ImageException("Delete image failed");
        }
    }

    public void deleteContractFile(String fileUrl) {

        try {

            String publicId = extractPublicIdContract(fileUrl);

            cloudinary.uploader().destroy(publicId, Map.of());

        } catch (Exception e) {
            throw new ImageException("Delete contract file failed");
        }
    }

    /**
     * Get public_id from Cloudinary URL
     * VD:
     * https://res.cloudinary.com/xxx/image/upload/v123/properties/1/abc.jpg
     * -> properties/1/abc
     */

    private String extractPublicIdAvatarUser(String imageUrl) {
        String noExt = imageUrl.substring(0, imageUrl.lastIndexOf("."));
        return noExt.substring(noExt.indexOf("avatars_hrm/employees"));
    }

    private String extractPublicIdContract(String fileUrl) {
        String noExt = fileUrl.substring(0, fileUrl.lastIndexOf("."));
        return noExt.substring(noExt.indexOf("contracts_hrm"));
    }

}
