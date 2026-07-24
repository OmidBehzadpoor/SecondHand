package com.example.secondhandfx.util;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

/**
 * <h2>HttpClientHelper</h2>
 * <p>
 * کلاس کمکی (Utility) مسئول ارتباط HTTP بین فرانت‌اند JavaFX و بک‌اند REST API.
 * این کلاس بر پایه‌ی {@link java.net.http.HttpClient} استاندارد جاوا ساخته
 * شده و متدهای ساده‌ای برای متدهای HTTP رایج ({@code GET/POST/PUT/PATCH/DELETE})
 * و همچنین آپلود فایل ({@code multipart/form-data}) فراهم می‌کند.
 * </p>
 * <p>
 * سریال‌سازی/غیرسریال‌سازی JSON با {@link ObjectMapper} (به‌همراه پشتیبانی از
 * {@code java.time} از طریق {@link JavaTimeModule}) انجام می‌شود، و توکن JWT
 * کاربر جاری (در صورت وجود) به‌صورت خودکار به هدر {@code Authorization} درخواست‌ها
 * افزوده می‌شود.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.util.SessionManager
 * @see com.example.secondhandfx.exception.ApiException
 */
public class HttpClientHelper {

    private static final String BASE_URL = Config.getApiBaseUrl();
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /** سازنده‌ی خصوصی برای جلوگیری از نمونه‌سازی؛ این کلاس فقط شامل متدهای استاتیک است. */
    private HttpClientHelper() {
    }

    /**
     * ارسال یک درخواست {@code GET} به مسیر مشخص‌شده.
     *
     * @param path         مسیر نسبی اندپوینت (نسبت به {@link #BASE_URL})
     * @param responseType نوع مورد انتظار پاسخ، برای غیرسریال‌سازی JSON
     * @param <T>          نوع داده‌ی پاسخ
     * @return شیء غیرسریال‌سازی‌شده‌ی پاسخ
     * @throws ApiException در صورت بروز خطای شبکه یا پاسخ ناموفق از سرور
     */
    public static <T> T get(String path, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.GET, path, null, responseType);
    }

    /**
     * ارسال یک درخواست {@code POST} به مسیر مشخص‌شده، به‌همراه بدنه‌ی درخواست.
     *
     * @param path         مسیر نسبی اندپوینت (نسبت به {@link #BASE_URL})
     * @param body         بدنه‌ی درخواست که به‌صورت JSON سریال می‌شود
     * @param responseType نوع مورد انتظار پاسخ، برای غیرسریال‌سازی JSON
     * @param <T>          نوع داده‌ی پاسخ
     * @return شیء غیرسریال‌سازی‌شده‌ی پاسخ
     * @throws ApiException در صورت بروز خطای شبکه یا پاسخ ناموفق از سرور
     */
    public static <T> T post(String path, ApiRequest body, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.POST, path, body, responseType);
    }

    /**
     * ارسال یک درخواست {@code PUT} به مسیر مشخص‌شده، به‌همراه بدنه‌ی درخواست.
     *
     * @param path         مسیر نسبی اندپوینت (نسبت به {@link #BASE_URL})
     * @param body         بدنه‌ی درخواست که به‌صورت JSON سریال می‌شود
     * @param responseType نوع مورد انتظار پاسخ، برای غیرسریال‌سازی JSON
     * @param <T>          نوع داده‌ی پاسخ
     * @return شیء غیرسریال‌سازی‌شده‌ی پاسخ
     * @throws ApiException در صورت بروز خطای شبکه یا پاسخ ناموفق از سرور
     */
    public static <T> T put(String path, ApiRequest body, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.PUT, path, body, responseType);
    }

    /**
     * ارسال یک درخواست {@code PATCH} به مسیر مشخص‌شده، به‌همراه بدنه‌ی درخواست.
     *
     * @param path         مسیر نسبی اندپوینت (نسبت به {@link #BASE_URL})
     * @param body         بدنه‌ی درخواست که به‌صورت JSON سریال می‌شود
     * @param responseType نوع مورد انتظار پاسخ، برای غیرسریال‌سازی JSON
     * @param <T>          نوع داده‌ی پاسخ
     * @return شیء غیرسریال‌سازی‌شده‌ی پاسخ
     * @throws ApiException در صورت بروز خطای شبکه یا پاسخ ناموفق از سرور
     */
    public static <T> T patch(String path, ApiRequest body, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.PATCH, path, body, responseType);
    }

    /**
     * ارسال یک درخواست {@code DELETE} به مسیر مشخص‌شده.
     *
     * @param path         مسیر نسبی اندپوینت (نسبت به {@link #BASE_URL})
     * @param responseType نوع مورد انتظار پاسخ، برای غیرسریال‌سازی JSON
     * @param <T>          نوع داده‌ی پاسخ
     * @return شیء غیرسریال‌سازی‌شده‌ی پاسخ
     * @throws ApiException در صورت بروز خطای شبکه یا پاسخ ناموفق از سرور
     */
    public static <T> T delete(String path, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.DELETE, path, null, responseType);
    }

    // آپلود یک فایل به‌صورت multipart/form-data — فرمتی که AdvertisementImageController
    // (با @RequestParam("file") MultipartFile) در بک‌اند انتظارش را دارد.
    /**
     * آپلود یک فایل (تصویر) به مسیر مشخص‌شده، به‌صورت {@code multipart/form-data}.
     *
     * @param path         مسیر نسبی اندپوینت آپلود (نسبت به {@link #BASE_URL})
     * @param file         فایلی که باید آپلود شود
     * @param responseType نوع مورد انتظار پاسخ، برای غیرسریال‌سازی JSON
     * @param <T>          نوع داده‌ی پاسخ
     * @return شیء غیرسریال‌سازی‌شده‌ی پاسخ
     * @throws ApiException در صورت بروز خطا در خواندن فایل، خطای شبکه، یا پاسخ ناموفق از سرور
     */
    public static <T> T uploadFile(String path, File file, TypeReference<T> responseType) throws ApiException {
        try {
            String boundary = "----SecondHandBoundary" + System.currentTimeMillis();
            byte[] multipartBody = buildMultipartBody(file, boundary);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary);

            attachAuthHeader(builder);
            builder.POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody));

            return executeAndParse(builder, responseType);
        } catch (IOException e) {
            throw new ApiException("خواندن فایل تصویر با خطا مواجه شد.", 0);
        }
    }

    /**
     * ساخت بدنه‌ی خام (بایت‌آرایه) درخواست {@code multipart/form-data} برای یک فایل.
     *
     * @param file     فایلی که باید در بدنه‌ی درخواست قرار گیرد
     * @param boundary رشته‌ی مرزبندی (boundary) یکتا برای جداسازی بخش‌های چندبخشی
     * @return بایت‌آرایه‌ی نهایی بدنه‌ی درخواست
     * @throws IOException در صورت بروز خطا در تشخیص نوع محتوا یا خواندن فایل
     */
    private static byte[] buildMultipartBody(File file, String boundary) throws IOException {
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(Files.readAllBytes(file.toPath()));
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return body.toByteArray();
    }

    /**
     * ساخت و ارسال یک درخواست HTTP با متد، مسیر و بدنه‌ی مشخص‌شده.
     *
     * @param method       متد HTTP درخواست
     * @param path         مسیر نسبی اندپوینت (نسبت به {@link #BASE_URL})
     * @param body         بدنه‌ی درخواست (ممکن است {@code null} باشد، در این صورت بدون بدنه ارسال می‌شود)
     * @param responseType نوع مورد انتظار پاسخ، برای غیرسریال‌سازی JSON
     * @param <T>          نوع داده‌ی پاسخ
     * @return شیء غیرسریال‌سازی‌شده‌ی پاسخ
     * @throws ApiException در صورت بروز خطا در آماده‌سازی درخواست، خطای شبکه، یا پاسخ ناموفق از سرور
     */
    private static <T> T send(HttpMethod method, String path, ApiRequest body, TypeReference<T> responseType) throws ApiException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json; charset=UTF-8");

            attachAuthHeader(builder);

            HttpRequest.BodyPublisher bodyPublisher = (body != null)
                    ? HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8)
                    : HttpRequest.BodyPublishers.noBody();

            builder.method(method.name(), bodyPublisher);

            return executeAndParse(builder, responseType);
        } catch (IOException e) {
            throw new ApiException("مشکلی در آماده‌سازی درخواست پیش آمد.", 0);
        }
    }

    /**
     * افزودن هدر {@code Authorization} با توکن JWT کاربر جاری به درخواست، در
     * صورتی که کاربر وارد سیستم شده باشد.
     *
     * @param builder سازنده‌ی درخواست HTTP که هدر باید به آن اضافه شود
     */
    private static void attachAuthHeader(HttpRequest.Builder builder) {
        String token = SessionManager.getInstance().getToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
    }

    // بخش مشترک بین send() و uploadFile(): فرستادن request آماده‌شده و مدیریت خطاهای شبکه
    /**
     * ارسال یک درخواست HTTP آماده‌شده و مدیریت خطاهای رایج شبکه (اتصال، Timeout و غیره).
     *
     * @param builder      سازنده‌ی درخواست HTTP نهایی که باید ارسال شود
     * @param responseType نوع مورد انتظار پاسخ، برای غیرسریال‌سازی JSON
     * @param <T>          نوع داده‌ی پاسخ
     * @return شیء غیرسریال‌سازی‌شده‌ی پاسخ
     * @throws ApiException در صورت عدم امکان اتصال به سرور، Timeout، یا سایر خطاهای شبکه
     */
    private static <T> T executeAndParse(HttpRequest.Builder builder, TypeReference<T> responseType) throws ApiException {
        try {
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return parseResponse(response, responseType);
        } catch (ConnectException e) {
            throw new ApiException("امکان برقراری ارتباط با سرور وجود ندارد. لطفاً مطمئن شوید سرور در حال اجراست.", 0);
        } catch (HttpTimeoutException e) {
            throw new ApiException("سرور در زمان مناسب پاسخ نداد. لطفاً دوباره تلاش کنید.", 0);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("مشکلی در ارتباط با سرور پیش آمد. لطفاً دوباره تلاش کنید.", 0);
        }
    }

    /**
     * پردازش پاسخ خام HTTP و غیرسریال‌سازی آن به نوع مورد انتظار، یا پرتاب
     * {@link ApiException} در صورت پاسخ ناموفق.
     *
     * @param response     پاسخ خام دریافت‌شده از سرور
     * @param responseType نوع مورد انتظار پاسخ، برای غیرسریال‌سازی JSON
     * @param <T>          نوع داده‌ی پاسخ
     * @return شیء غیرسریال‌سازی‌شده‌ی پاسخ، در صورتی که کد وضعیت پاسخ در بازه‌ی موفقیت‌آمیز باشد
     * @throws ApiException در صورتی که کد وضعیت پاسخ ناموفق باشد (همراه با پیام خطای سرور)،
     *         یا در صورت بروز خطا هنگام پردازش بدنه‌ی پاسخ
     */
    private static <T> T parseResponse(HttpResponse<String> response, TypeReference<T> responseType) throws ApiException {
        try {
            JsonNode root = objectMapper.readTree(response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.convertValue(root, objectMapper.getTypeFactory().constructType(responseType));
            }

            String errorMessage = root.has("error") ? root.get("error").asText() : "خطای نامشخصی رخ داد";
            throw new ApiException(errorMessage, response.statusCode());
        } catch (IOException e) {
            throw new ApiException("خطا در پردازش پاسخ سرور رخ داد.", response.statusCode());
        }
    }
}
