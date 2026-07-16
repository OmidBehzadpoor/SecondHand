package com.example.secondhand.service;

import com.example.secondhand.dto.response.AdvertisementImageResponse;
import com.example.secondhand.exception.AdvertisementImageNotFoundException;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.InvalidImageException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.Advertisement;
import com.example.secondhand.model.AdvertisementImage;
import com.example.secondhand.model.User;
import com.example.secondhand.repository.AdvertisementImageRepository;
import com.example.secondhand.repository.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdvertisementImageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    private final AdvertisementImageRepository advertisementImageRepository;
    private final AdvertisementRepository advertisementRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional
    public AdvertisementImageResponse uploadImage(Long advertisementId, MultipartFile file, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما اجازه‌ی افزودن تصویر به این آگهی را ندارید");
        }

        if (file.isEmpty()) {
            throw new InvalidImageException("فایل تصویر ارسال نشده است");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidImageException("فرمت فایل مجاز نیست؛ فقط JPEG، PNG و WEBP پذیرفته می‌شود");
        }

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };

        String filename = UUID.randomUUID() + extension;

        try {
            Path directory = Paths.get(uploadDir, advertisementId.toString());
            Files.createDirectories(directory);
            file.transferTo(directory.resolve(filename));
        } catch (IOException e) {
            throw new InvalidImageException("خطا در ذخیره‌سازی فایل تصویر");
        }

        String imageUrl = "/uploads/advertisements/" + advertisementId + "/" + filename;

        AdvertisementImage image = AdvertisementImage.builder()
                .imageUrl(imageUrl)
                .advertisement(advertisement)
                .build();

        return mapToResponse(advertisementImageRepository.save(image));
    }

    @Transactional
    public void deleteImage(Long advertisementId, Long imageId, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما اجازه‌ی حذف تصویر این آگهی را ندارید");
        }

        AdvertisementImage image = advertisementImageRepository.findById(imageId)
                .filter(img -> img.getAdvertisement().getId().equals(advertisementId))
                .orElseThrow(() -> new AdvertisementImageNotFoundException("تصویر مورد نظر یافت نشد"));

        advertisementImageRepository.delete(image);

        String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf('/') + 1);
        try {
            Files.deleteIfExists(Paths.get(uploadDir, advertisementId.toString(), filename));
        } catch (IOException ignored) {
        }
    }

    private AdvertisementImageResponse mapToResponse(AdvertisementImage image) {
        return AdvertisementImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();
    }
}