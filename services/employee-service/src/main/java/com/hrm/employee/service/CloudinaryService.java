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

    private static final long MAX_FILE_SIZE = 4 * 1024 * 1024;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // ===================== AVATAR =====================
    public String uploadAvatar(MultipartFile file) {
        return uploadImage(file, "avatars_hrm/employees");
    }


    // ===================== COMMON =====================
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

}
