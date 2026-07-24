package com.example.secondhand.service;

import com.example.secondhand.dto.response.AdminDashboardResponse;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CategoryRepository;
import com.example.secondhand.repository.CityRepository;
import com.example.secondhand.repository.ConversationRepository;
import com.example.secondhand.repository.FavoriteRepository;
import com.example.secondhand.repository.MessageRepository;
import com.example.secondhand.repository.SellerRatingRepository;
import com.example.secondhand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <h2>AdminDashboardService</h2>
 * <p>
 * سرویس مسئول تولید <b>آمار کلی سامانه</b> برای نمایش در داشبورد پنل مدیریت (ادمین).
 * این کلاس با فراخوانی متدهای شمارشی (count) از چندین Repository مختلف، یک
 * تصویر خلاصه و لحظه‌ای از وضعیت کاربران، آگهی‌ها، دسته‌بندی‌ها، شهرها،
 * گفت‌وگوها، پیام‌ها، علاقه‌مندی‌ها و امتیازها را فراهم می‌کند.
 * </p>
 * <p>
 * از آنجا که این سرویس صرفاً عملیات خواندن (Read) روی پایگاه داده انجام می‌دهد،
 * متد آن با {@code @Transactional(readOnly = true)} علامت‌گذاری شده است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.dto.response.AdminDashboardResponse
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final FavoriteRepository favoriteRepository;
    private final SellerRatingRepository sellerRatingRepository;

    /**
     * تولید و بازگرداندن آمار کلی سامانه برای نمایش در داشبورد ادمین.
     * <p>
     * این متد شامل تعداد کل کاربران (به‌همراه تفکیک فعال/مسدود)، تعداد کل
     * آگهی‌ها (به‌همراه تفکیک بر اساس وضعیت‌های در انتظار، تاییدشده، ردشده،
     * فروخته‌شده و حذف‌شده)، تعداد دسته‌بندی‌ها، شهرها، گفت‌وگوها، پیام‌ها،
     * علاقه‌مندی‌ها و امتیازهای ثبت‌شده است.
     * </p>
     *
     * @return {@link AdminDashboardResponse} حاوی تمام آمار خلاصه‌شده‌ی سامانه
     */
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByStatus(UserStatus.ACTIVE))
                .blockedUsers(userRepository.countByStatus(UserStatus.BLOCKED))
                .totalAdvertisements(advertisementRepository.count())
                .pendingAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.PENDING))
                .approvedAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.APPROVED))
                .rejectedAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.REJECTED))
                .soldAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.SOLD))
                .deletedAdvertisements(advertisementRepository.countByStatus(AdvertisementStatus.DELETED))
                .totalCategories(categoryRepository.count())
                .totalCities(cityRepository.count())
                .totalConversations(conversationRepository.count())
                .totalMessages(messageRepository.count())
                .totalFavorites(favoriteRepository.count())
                .totalRatings(sellerRatingRepository.count())
                .build();
    }
}
