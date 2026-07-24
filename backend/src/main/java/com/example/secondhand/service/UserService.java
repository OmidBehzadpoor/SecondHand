package com.example.secondhand.service;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.AdminUserResponse;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.exception.InvalidCredentialsException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.exception.UserNotFoundException;
import com.example.secondhand.exception.UserStateConflictException;
import com.example.secondhand.exception.UserBlockedException;
import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.repository.UserRepository;
import com.example.secondhand.exception.UserAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


/**
 * <h2>UserService</h2>
 * <p>
 * سرویس اصلی مدیریت کاربران در سامانه. این کلاس مسئولیت‌های زیر را بر عهده دارد:
 * </p>
 * <ul>
 *   <li><b>ثبت‌نام</b> کاربران جدید و بررسی تکراری نبودن نام کاربری و شماره تماس</li>
 *   <li><b>ورود (Login)</b> کاربران و صدور توکن JWT پس از احراز هویت موفق</li>
 *   <li><b>مدیریت کاربران توسط ادمین</b>: مشاهده لیست کاربران، مسدودسازی و رفع مسدودیت</li>
 * </ul>
 * <p>
 * این کلاس با {@code @Service} به‌عنوان یک Bean در اسپرینگ ثبت شده و با استفاده از
 * {@code @RequiredArgsConstructor} (لومبوک) وابستگی‌های نهایی (final) را از طریق
 * سازنده تزریق می‌کند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.repository.UserRepository
 * @see com.example.secondhand.service.JwtService
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * ثبت‌نام یک کاربر جدید در سامانه.
     * <p>
     * پیش از ذخیره‌سازی، این متد بررسی می‌کند که نام کاربری و شماره تماس ارسالی
     * تکراری نباشند. سپس رمز عبور با استفاده از {@link PasswordEncoder} هش شده
     * و کاربر جدید در پایگاه داده ذخیره می‌شود.
     * </p>
     *
     * @param request شیء حاوی اطلاعات ثبت‌نام (نام، نام کاربری، رمز عبور، شماره تماس، ایمیل)
     * @return شناسه (ID) کاربر تازه ایجاد شده
     * @throws UserAlreadyExistsException در صورتی که نام کاربری یا شماره تماس قبلاً ثبت شده باشد
     */
    public Long register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("این نام کاربری قبلاً ثبت شده است");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("این شماره تماس قبلاً ثبت شده است");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .password(hashedPassword)
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        return userRepository.save(user).getId();
    }

    /**
     * ورود کاربر به سامانه و صدور توکن JWT.
     * <p>
     * این متد نام کاربری را در پایگاه داده جست‌وجو کرده، رمز عبور ارسالی را با
     * رمز عبور هش‌شده مقایسه می‌کند و در صورت معتبر بودن اطلاعات و فعال بودن
     * وضعیت کاربر، یک توکن JWT صادر می‌کند.
     * </p>
     *
     * @param request شیء حاوی نام کاربری و رمز عبور برای ورود
     * @return {@link LoginResponse} شامل نام کاربر، توکن JWT، شناسه، نام کاربری و نقش او
     * @throws InvalidCredentialsException در صورتی که نام کاربری یافت نشود یا رمز عبور نادرست باشد
     * @throws UserBlockedException در صورتی که حساب کاربری مسدود شده باشد
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("نام کاربری یا رمز عبور اشتباه است"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("نام کاربری یا رمز عبور اشتباه است");
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException("حساب کاربری شما مسدود شده است");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(user.getName(), token, user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * دریافت لیست تمام کاربران سامانه، مخصوص پنل مدیریت (ادمین).
     * <p>
     * این متد به‌صورت {@code readOnly} در یک تراکنش اجرا می‌شود چون فقط
     * عملیات خواندن از پایگاه داده انجام می‌دهد.
     * </p>
     *
     * @return لیستی از {@link AdminUserResponse} شامل اطلاعات خلاصه هر کاربر
     */
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsersForAdmin() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());
    }

    /**
     * مسدود کردن یک کاربر توسط ادمین.
     * <p>
     * ادمین امکان مسدود کردن سایر ادمین‌ها را ندارد و همچنین کاربری که از قبل
     * مسدود شده باشد، نمی‌تواند دوباره مسدود شود.
     * </p>
     *
     * @param userId شناسه کاربری که باید مسدود شود
     * @return {@link AdminUserResponse} حاوی اطلاعات به‌روزشده کاربر پس از مسدودسازی
     * @throws UserNotFoundException در صورتی که کاربر با شناسه داده‌شده یافت نشود
     * @throws UnauthorizedActionException در صورتی که هدف عملیات یک کاربر با نقش ادمین باشد
     * @throws UserStateConflictException در صورتی که کاربر از قبل در وضعیت مسدود باشد
     */
    @Transactional
    public AdminUserResponse blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("کاربر مورد نظر یافت نشد"));

        if (user.getRole() == Role.ADMIN) {
            throw new UnauthorizedActionException("امکان تغییر وضعیت دسترسی سایر مدیران وجود ندارد");
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UserStateConflictException("کاربر از قبل مسدود شده است");
        }

        user.setStatus(UserStatus.BLOCKED);
        return mapToAdminResponse(userRepository.save(user));
    }

    /**
     * رفع مسدودیت یک کاربر توسط ادمین.
     * <p>
     * در صورتی که کاربر از قبل در وضعیت فعال باشد، عملیات با خطا مواجه می‌شود.
     * </p>
     *
     * @param userId شناسه کاربری که باید از حالت مسدود خارج شود
     * @return {@link AdminUserResponse} حاوی اطلاعات به‌روزشده کاربر پس از رفع مسدودیت
     * @throws UserNotFoundException در صورتی که کاربر با شناسه داده‌شده یافت نشود
     * @throws UserStateConflictException در صورتی که کاربر از قبل در وضعیت فعال باشد
     */
    @Transactional
    public AdminUserResponse unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("کاربر مورد نظر یافت نشد"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new UserStateConflictException("کاربر از قبل فعال است");
        }

        user.setStatus(UserStatus.ACTIVE);
        return mapToAdminResponse(userRepository.save(user));
    }

    /**
     * تبدیل شیء {@link User} به {@link AdminUserResponse} برای نمایش در پنل ادمین.
     * <p>
     * این یک متد کمکی خصوصی (private) است که فقط در داخل همین کلاس استفاده می‌شود
     * و اطلاعات حساس مانند رمز عبور را در خروجی قرار نمی‌دهد.
     * </p>
     *
     * @param user موجودیت کاربر که باید به DTO تبدیل شود
     * @return شیء {@link AdminUserResponse} حاوی اطلاعات نمایشی کاربر
     */
    private AdminUserResponse mapToAdminResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .userStatus(user.getStatus())
                .build();
    }
}
