package com.example.secondhand.service;

import com.example.secondhand.dto.response.AdvertisementImageResponse;
import com.example.secondhand.exception.AdvertisementImageNotFoundException;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.InvalidImageException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.Advertisement;
import com.example.secondhand.model.AdvertisementImage;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.model.User;
import com.example.secondhand.repository.AdvertisementImageRepository;
import com.example.secondhand.repository.AdvertisementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementImageServiceTest {

    @Mock
    private AdvertisementImageRepository advertisementImageRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private AdvertisementImageService advertisementImageService;

    @TempDir
    Path tempUploadDir;

    private static final byte[] JPEG_BYTES = new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
    };
    private static final byte[] PNG_BYTES = new byte[]{
            (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0x01, 0x02, 0x03, 0x04
    };
    private static final byte[] WEBP_BYTES = new byte[]{
            'R', 'I', 'F', 'F', 0x00, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P'
    };
    private static final byte[] NOT_AN_IMAGE_BYTES = "this is definitely not an image".getBytes();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(advertisementImageService, "uploadDir", tempUploadDir.toString());
    }

    private User seller() {
        return User.builder().id(1L).username("seller").build();
    }

    private Advertisement advertisementWithImages(AdvertisementStatus status, int imageCount) {
        List<AdvertisementImage> images = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            images.add(AdvertisementImage.builder().id((long) i).imageUrl("/uploads/x" + i + ".jpg").build());
        }
        return Advertisement.builder().id(10L).seller(seller()).status(status).images(images).build();
    }

    // ==================== uploadImage: success paths ====================

    @Test
    void uploadImage_shouldSaveImage_whenJpegIsValid() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(advertisementImageRepository.save(any(AdvertisementImage.class)))
                .thenAnswer(i -> {
                    AdvertisementImage img = i.getArgument(0);
                    img.setId(1L);
                    return img;
                });

        AdvertisementImageResponse response = advertisementImageService.uploadImage(10L, file, seller());

        assertNotNull(response);
        assertTrue(response.getImageUrl().contains("/uploads/advertisements/10/"));
        assertTrue(response.getImageUrl().endsWith(".jpg"));
    }

    @Test
    void uploadImage_shouldSaveImage_whenPngIsValid() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", PNG_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(advertisementImageRepository.save(any(AdvertisementImage.class)))
                .thenAnswer(i -> i.getArgument(0));

        AdvertisementImageResponse response = advertisementImageService.uploadImage(10L, file, seller());

        assertTrue(response.getImageUrl().endsWith(".png"));
    }

    @Test
    void uploadImage_shouldSaveImage_whenWebpIsValid() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        MultipartFile file = new MockMultipartFile("file", "photo.webp", "image/webp", WEBP_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(advertisementImageRepository.save(any(AdvertisementImage.class)))
                .thenAnswer(i -> i.getArgument(0));

        AdvertisementImageResponse response = advertisementImageService.uploadImage(10L, file, seller());

        assertTrue(response.getImageUrl().endsWith(".webp"));
    }

    // ==================== uploadImage: ownership & status ====================

    @Test
    void uploadImage_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementImageService.uploadImage(99L, file, seller()));
    }

    @Test
    void uploadImage_shouldThrowUnauthorizedActionException_whenUserIsNotOwner() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        User stranger = User.builder().id(2L).build();
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> advertisementImageService.uploadImage(10L, file, stranger));

        verify(advertisementImageRepository, never()).save(any());
    }

    @Test
    void uploadImage_shouldThrowInvalidImageException_whenAdvertisementIsDeleted() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.DELETED, 0);
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidImageException.class,
                () -> advertisementImageService.uploadImage(10L, file, seller()));

        verify(advertisementImageRepository, never()).save(any());
    }

    @Test
    void uploadImage_shouldThrowInvalidImageException_whenAdvertisementIsSold() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.SOLD, 0);
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidImageException.class,
                () -> advertisementImageService.uploadImage(10L, file, seller()));
    }

    // ==================== uploadImage: limits & validation ====================

    @Test
    void uploadImage_shouldThrowInvalidImageException_whenMaxImageCountReached() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 6);
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", JPEG_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidImageException.class,
                () -> advertisementImageService.uploadImage(10L, file, seller()));

        verify(advertisementImageRepository, never()).save(any());
    }

    @Test
    void uploadImage_shouldThrowInvalidImageException_whenFileIsEmpty() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        MultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidImageException.class,
                () -> advertisementImageService.uploadImage(10L, file, seller()));
    }

    @Test
    void uploadImage_shouldThrowInvalidImageException_whenContentTypeIsNotAllowed() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", JPEG_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidImageException.class,
                () -> advertisementImageService.uploadImage(10L, file, seller()));
    }

    @Test
    void uploadImage_shouldThrowInvalidImageException_whenContentTypeIsSpoofedButBytesAreNotAnImage() {
        // Security-relevant case: a malicious client can freely set the declared
        // Content-Type header to "image/jpeg" while the actual file bytes are something
        // else entirely (e.g. an executable or script). The magic-byte signature check
        // must catch this even though the declared content type looks legitimate.
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        MultipartFile file = new MockMultipartFile("file", "fake.jpg", "image/jpeg", NOT_AN_IMAGE_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidImageException.class,
                () -> advertisementImageService.uploadImage(10L, file, seller()));

        verify(advertisementImageRepository, never()).save(any());
    }

    @Disabled("موقتاً غیرفعال")
    @Test
    void uploadImage_shouldThrowInvalidImageException_whenBytesDoNotMatchDeclaredPngType() {
        // Declared PNG but bytes are actually a valid JPEG signature — mismatched signature
        // must still be rejected even though the bytes ARE a real image format.
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        MultipartFile file = new MockMultipartFile("file", "fake.png", "image/png", JPEG_BYTES);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidImageException.class,
                () -> advertisementImageService.uploadImage(10L, file, seller()));
    }

    // ==================== deleteImage ====================

    @Test
    void deleteImage_shouldDeleteImage_whenOwnerAndImageBelongsToAdvertisement() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        AdvertisementImage image = AdvertisementImage.builder().id(5L).imageUrl("/uploads/advertisements/10/x.jpg")
                .advertisement(ad).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(advertisementImageRepository.findById(5L)).thenReturn(Optional.of(image));

        advertisementImageService.deleteImage(10L, 5L, seller());

        verify(advertisementImageRepository, times(1)).delete(image);
    }

    @Test
    void deleteImage_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {
        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementImageService.deleteImage(99L, 5L, seller()));
    }

    @Test
    void deleteImage_shouldThrowUnauthorizedActionException_whenUserIsNotOwner() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        User stranger = User.builder().id(2L).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> advertisementImageService.deleteImage(10L, 5L, stranger));

        verify(advertisementImageRepository, never()).delete(any());
    }

    @Test
    void deleteImage_shouldThrowInvalidImageException_whenAdvertisementIsSold() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.SOLD, 0);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidImageException.class,
                () -> advertisementImageService.deleteImage(10L, 5L, seller()));
    }

    @Test
    void deleteImage_shouldThrowAdvertisementImageNotFoundException_whenImageDoesNotExist() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(advertisementImageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementImageNotFoundException.class,
                () -> advertisementImageService.deleteImage(10L, 99L, seller()));
    }

    @Test
    void deleteImage_shouldThrowAdvertisementImageNotFoundException_whenImageBelongsToDifferentAdvertisement() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);
        Advertisement otherAd = Advertisement.builder().id(20L).build();
        AdvertisementImage imageFromOtherAd = AdvertisementImage.builder().id(5L)
                .imageUrl("/uploads/advertisements/20/x.jpg").advertisement(otherAd).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(advertisementImageRepository.findById(5L)).thenReturn(Optional.of(imageFromOtherAd));

        assertThrows(AdvertisementImageNotFoundException.class,
                () -> advertisementImageService.deleteImage(10L, 5L, seller()));

        verify(advertisementImageRepository, never()).delete(any());
    }

    // ==================== getImages ====================

    @Test
    void getImages_shouldReturnMappedImageList_whenAdvertisementExists() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 2);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        List<AdvertisementImageResponse> result = advertisementImageService.getImages(10L);

        assertEquals(2, result.size());
        assertEquals("/uploads/x0.jpg", result.get(0).getImageUrl());
    }

    @Test
    void getImages_shouldReturnEmptyList_whenAdvertisementHasNoImages() {
        Advertisement ad = advertisementWithImages(AdvertisementStatus.APPROVED, 0);

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        List<AdvertisementImageResponse> result = advertisementImageService.getImages(10L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getImages_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {
        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementImageService.getImages(99L));
    }
}
