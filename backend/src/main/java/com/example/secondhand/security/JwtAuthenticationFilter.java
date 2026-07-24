package com.example.secondhand.security;

import com.example.secondhand.model.User;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.repository.UserRepository;
import com.example.secondhand.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * <h2>JwtAuthenticationFilter</h2>
 * <p>
 * فیلتر امنیتی Spring Security مسئول <b>احراز هویت مبتنی بر JWT</b>. این
 * فیلتر یک‌بار به‌ازای هر درخواست HTTP اجرا می‌شود ({@link OncePerRequestFilter})،
 * توکن JWT را از هدر {@code Authorization} استخراج می‌کند، آن را اعتبارسنجی
 * کرده، و در صورت معتبر بودن و فعال بودن وضعیت کاربر، شیء احراز هویت
 * متناظر را در {@link SecurityContextHolder} قرار می‌دهد.
 * </p>
 * <p>
 * این فیلتر پیش از {@code UsernamePasswordAuthenticationFilter} در زنجیره
 * فیلترهای امنیتی ثبت می‌شود (به {@link com.example.secondhand.config.SecurityConfig}
 * مراجعه شود).
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.JwtService
 * @see com.example.secondhand.config.SecurityConfig
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * پردازش هر درخواست HTTP ورودی برای احراز هویت مبتنی بر توکن JWT.
     * <p>
     * در صورت نبود هدر {@code Authorization} یا نداشتن پیشوند {@code "Bearer "}،
     * درخواست بدون تغییر به فیلتر بعدی زنجیره ارسال می‌شود. در غیر این
     * صورت، نام کاربری از توکن استخراج شده و کاربر متناظر در پایگاه داده
     * جست‌وجو می‌شود؛ اگر کاربر مسدود باشد، احراز هویت انجام نمی‌شود. اگر
     * توکن معتبر باشد، یک {@link UsernamePasswordAuthenticationToken} حاوی
     * کاربر و نقش او (با پیشوند {@code "ROLE_"}) ساخته و در
     * {@link SecurityContextHolder} ثبت می‌شود. هرگونه خطا در فرآیند
     * اعتبارسنجی توکن (مثلاً توکن نامعتبر یا منقضی) صرفاً در سطح
     * {@code debug} ثبت لاگ می‌شود و درخواست بدون احراز هویت ادامه می‌یابد.
     * </p>
     *
     * @param request     درخواست HTTP ورودی
     * @param response    پاسخ HTTP خروجی
     * @param filterChain زنجیره‌ی فیلترهای امنیتی که پردازش باید به آن ادامه یابد
     * @throws ServletException در صورت بروز خطای سرولت هنگام ادامه‌ی زنجیره‌ی فیلترها
     * @throws IOException      در صورت بروز خطای ورودی/خروجی هنگام ادامه‌ی زنجیره‌ی فیلترها
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null && user.getStatus() != UserStatus.ACTIVE) {
                    filterChain.doFilter(request, response);
                    return;
                }

                if (user != null && jwtService.validateToken(token, username)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
