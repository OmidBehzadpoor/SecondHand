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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * <h2>AdvertisementImageService</h2>
 * <p>
 * سرویس مسئول مدیریت <b>تصاویر آگهی‌ها</b>، شامل آپلود، حذف و دریافت لیست تصاویر
 * مرتبط با یک آگهی. این کلاس علاوه بر ذخیره‌سازی فیزیکی فایل‌ها روی دیسک، اعتبارسنجی
 * فرمت و محتوای فایل ارسالی را نیز انجام می‌دهد.
 * </p>
 * <ul>
 *   <li>بررسی مالکیت آگهی توسط کاربر جاری پیش از هر عملیات</li>
 *   <li>محدودیت تعداد تصاویر مجاز برای هر آگهی ({@value #MAX_IMAGES_PER_ADVERTISEMENT} عدد)</li>
 *   <li>بررسی نوع فایل (Content-Type) و همچنین امضای واقعی محتوای فایل (Magic Number)
 *       برای جلوگیری از آپلود فایل‌های جعلی یا مخرب</li>
 * </ul>
 * <p>
 * مسیر ذخیره‌سازی فایل‌ها از طریق مقدار {@code app.upload.dir} در تنظیمات برنامه
 * تعیین می‌شود.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.AdvertisementImage
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementImageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final int MAX_IMAGES_PER_ADVERTISEMENT = 6;

    private final AdvertisementImageRepository advertisementImageRepository;
    private final AdvertisementRepository advertisementRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * آپلود یک تصویر جدید برای آگهی مشخص‌شده.
     * <p>
     * پیش از ذخیره‌سازی، مالکیت آگهی، وضعیت آگهی (غیرحذف‌شده و غیرفروخته‌شده)،
     * محدودیت تعداد تصاویر، خالی نبودن فایل، نوع محتوا و امضای واقعی فایل
     * بررسی می‌شوند. در صورت موفقیت، فایل روی دیسک ذخیره و رکورد مربوطه در
     * پایگاه داده ثبت می‌شود.
     * </p>
     *
     * @param advertisementId شناسه آگهی مقصد برای افزودن تصویر
     * @param file             فایل تصویر ارسالی توسط کاربر
     * @param currentUser      کاربر جاری که درخواست آپلود را ارسال کرده است
     * @return {@link AdvertisementImageResponse} حاوی اطلاعات تصویر ذخیره‌شده
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     * @throws UnauthorizedActionException در صورتی که کاربر جاری مالک آگهی نباشد
     * @throws InvalidImageException در صورت نامعتبر بودن وضعیت آگهی، عبور از سقف مجاز تصاویر،
     *         خالی بودن فایل، نامعتبر بودن فرمت، عدم تطابق محتوا با فرمت اعلام‌شده،
     *         یا بروز خطا در ذخیره‌سازی فایل روی دیسک
     */
    @Transactional
    public AdvertisementImageResponse uploadImage(Long advertisementId, MultipartFile file, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما اجازه‌ی افزودن تصویر به این آگهی را ندارید");
        }

        if (advertisement.getStatus() == AdvertisementStatus.DELETED
                || advertisement.getStatus() == AdvertisementStatus.SOLD) {
            throw new InvalidImageException("امکان حذف تصویر این آگهی وجود ندارد");
        }

        if (advertisement.getImages().size() >= MAX_IMAGES_PER_ADVERTISEMENT) {
            throw new InvalidImageException("حداکثر تعداد مجاز تصویر برای هر آگهی " + MAX_IMAGES_PER_ADVERTISEMENT + " عدد است");
        }

        if (file.isEmpty()) {
            throw new InvalidImageException("فایل تصویر ارسال نشده است");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidImageException("فرمت فایل مجاز نیست؛ فقط JPEG، PNG و WEBP پذیرفته می‌شود");
        }

        if (!hasValidImageSignature(file)) {
            throw new InvalidImageException("محتوای فایل با فرمت اعلام‌شده مطابقت ندارد");
        }

        String extension = switch (contentType.toLowerCase()) {
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

    /**
     * حذف یک تصویر مشخص از یک آگهی.
     * <p>
     * علاوه بر حذف رکورد از پایگاه داده، تلاش می‌شود فایل فیزیکی مرتبط نیز از
     * روی دیسک حذف شود. در صورت شکست حذف فیزیکی فایل، فقط یک هشدار ثبت می‌شود
     * و عملیات با خطا متوقف نمی‌شود.
     * </p>
     *
     * @param advertisementId شناسه آگهی‌ای که تصویر به آن تعلق دارد
     * @param imageId          شناسه تصویری که باید حذف شود
     * @param currentUser      کاربر جاری که درخواست حذف را ارسال کرده است
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     * @throws UnauthorizedActionException در صورتی که کاربر جاری مالک آگهی نباشد
     * @throws InvalidImageException در صورتی که وضعیت آگهی اجازه حذف تصویر را ندهد
     * @throws AdvertisementImageNotFoundException در صورتی که تصویر مورد نظر یافت نشود
     *         یا متعلق به آگهی داده‌شده نباشد
     */
    @Transactional
    public void deleteImage(Long advertisementId, Long imageId, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما اجازه‌ی حذف تصویر این آگهی را ندارید");
        }

        if (advertisement.getStatus() == AdvertisementStatus.DELETED
                || advertisement.getStatus() == AdvertisementStatus.SOLD) {
            throw new InvalidImageException("امکان حذف تصویر این آگهی وجود ندارد");
        }

        AdvertisementImage image = advertisementImageRepository.findById(imageId)
                .filter(img -> img.getAdvertisement().getId().equals(advertisementId))
                .orElseThrow(() -> new AdvertisementImageNotFoundException("تصویر مورد نظر یافت نشد"));

        advertisementImageRepository.delete(image);

        String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf('/') + 1);
        try {
            Files.deleteIfExists(Paths.get(uploadDir, advertisementId.toString(), filename));
        } catch (IOException e) {
            log.warn("Failed to delete physical image file for advertisement {}: {}", advertisementId, filename, e);
        }
    }

    /**
     * دریافت لیست تمام تصاویر مرتبط با یک آگهی مشخص.
     *
     * @param advertisementId شناسه آگهی مورد نظر
     * @return لیستی از {@link AdvertisementImageResponse} مرتبط با آگهی
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     */
    @Transactional(readOnly = true)
    public List<AdvertisementImageResponse> getImages(Long advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        return advertisement.getImages().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * تبدیل شیء {@link AdvertisementImage} به DTO خروجی {@link AdvertisementImageResponse}.
     *
     * @param image موجودیت تصویر آگهی
     * @return شیء {@link AdvertisementImageResponse} متناظر
     */
    private AdvertisementImageResponse mapToResponse(AdvertisementImage image) {
        return AdvertisementImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();
    }

    /**
     * بررسی معتبر بودن امضای واقعی (Magic Number) فایل تصویر ارسالی.
     * <p>
     * این متد صرفاً به Content-Type اعلام‌شده توسط کلاینت اکتفا نمی‌کند و با
     * خواندن چند بایت ابتدایی فایل، فرمت واقعی آن را (JPEG، PNG یا WEBP)
     * تشخیص می‌دهد تا از آپلود فایل‌های جعلی جلوگیری شود.
     * </p>
     *
     * @param file فایل تصویر ارسالی
     * @return {@code true} در صورتی که فایل دارای امضای معتبر JPEG، PNG یا WEBP باشد،
     *         در غیر این صورت {@code false}
     */
    private boolean hasValidImageSignature(MultipartFile file) {
        try {
            byte[] header = new byte[12];
            int bytesRead;
            try (var inputStream = file.getInputStream()) {
                bytesRead = inputStream.read(header);
            }

            if (bytesRead < 3) {
                return false;
            }

            boolean isJpeg = (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;

            boolean isPng = bytesRead >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                    && header[4] == 0x0D && header[5] == 0x0A
                    && header[6] == 0x1A && header[7] == 0x0A;

            boolean isWebp = bytesRead >= 12
                    && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';

            return isJpeg || isPng || isWebp;
        } catch (IOException e) {
            return false;
        }
    }
}
