package com.example.secondhand.service;

import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * <h2>JwtService</h2>
 * <p>
 * سرویس مسئول تولید، امضا و استخراج اطلاعات از توکن‌های <b>JWT</b> (JSON Web Token)
 * در سامانه. این کلاس از کتابخانه <a href="https://github.com/jwtk/jjwt">jjwt</a>
 * برای ساخت و اعتبارسنجی توکن‌ها استفاده می‌کند.
 * </p>
 * <ul>
 *   <li>تولید توکن جدید برای کاربر پس از ورود موفق</li>
 *   <li>استخراج claim های موجود در توکن (نام کاربری، شناسه کاربر، نقش)</li>
 *   <li>بررسی معتبر بودن و منقضی نشدن توکن</li>
 * </ul>
 * <p>
 * مقادیر {@code secretKey} و {@code expiration} از فایل تنظیمات (application.properties)
 * با استفاده از {@code @Value} خوانده می‌شوند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.User
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * ساخت کلید امضای HMAC از روی رشته {@code secretKey} تعریف‌شده در تنظیمات.
     * <p>
     * این متد کمکی خصوصی است و در تمام عملیات امضا و تأیید توکن استفاده می‌شود.
     * </p>
     *
     * @return شیء {@link SecretKey} برای امضا یا اعتبارسنجی توکن‌های JWT
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * تولید یک توکن JWT جدید برای کاربر مشخص‌شده.
     * <p>
     * توکن شامل نام کاربری به‌عنوان subject، و claim های {@code userId} و {@code role}
     * است. زمان انقضا بر اساس مقدار {@code expiration} (به میلی‌ثانیه) محاسبه می‌شود.
     * </p>
     *
     * @param user کاربری که توکن برای او صادر می‌شود
     * @return رشته توکن JWT امضاشده و آماده استفاده
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * استخراج تمام claim های موجود در یک توکن JWT پس از اعتبارسنجی امضا.
     *
     * @param token رشته توکن JWT
     * @return شیء {@link Claims} حاوی تمام اطلاعات ذخیره‌شده در توکن
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * استخراج نام کاربری (subject) از توکن JWT.
     *
     * @param token رشته توکن JWT
     * @return نام کاربری ذخیره‌شده در توکن
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * استخراج شناسه (ID) کاربر از claim مربوطه در توکن JWT.
     *
     * @param token رشته توکن JWT
     * @return شناسه کاربر به‌صورت {@link Long}
     */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    /**
     * استخراج نقش (Role) کاربر از claim مربوطه در توکن JWT.
     *
     * @param token رشته توکن JWT
     * @return مقدار {@link Role} متناظر با نقش ذخیره‌شده در توکن
     */
    public Role extractRole(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        return Role.valueOf(role);
    }

    /**
     * بررسی می‌کند که آیا توکن JWT منقضی شده است یا خیر.
     *
     * @param token رشته توکن JWT
     * @return {@code true} در صورتی که تاریخ انقضای توکن گذشته باشد، در غیر این صورت {@code false}
     */
    public boolean isTokenExpired(String token) {
        Date expirationDate = extractAllClaims(token).getExpiration();
        return expirationDate.before(new Date());
    }

    /**
     * اعتبارسنجی کامل یک توکن JWT برای یک نام کاربری مشخص.
     * <p>
     * این متد بررسی می‌کند که نام کاربری استخراج‌شده از توکن با نام کاربری
     * ورودی مطابقت داشته باشد و توکن هنوز منقضی نشده باشد. در صورت بروز هرگونه
     * خطا (مانند امضای نامعتبر یا فرمت اشتباه) مقدار {@code false} بازگردانده می‌شود.
     * </p>
     *
     * @param token    رشته توکن JWT
     * @param username نام کاربری‌ای که باید با subject توکن مطابقت داده شود
     * @return {@code true} در صورت معتبر بودن توکن برای این کاربر، در غیر این صورت {@code false}
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            return tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
