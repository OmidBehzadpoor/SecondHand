package com.example.secondhandfx.util;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class HttpClientHelper {

    private static final String BASE_URL = Config.getApiBaseUrl();
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private HttpClientHelper() {
    }

    public static <T> T get(String path, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.GET, path, null, responseType);
    }

    public static <T> T post(String path, ApiRequest body, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.POST, path, body, responseType);
    }

    public static <T> T put(String path, ApiRequest body, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.PUT, path, body, responseType);
    }

    public static <T> T patch(String path, ApiRequest body, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.PATCH, path, body, responseType);
    }

    public static <T> T delete(String path, TypeReference<T> responseType) throws ApiException {
        return send(HttpMethod.DELETE, path, null, responseType);
    }

    private static <T> T send(HttpMethod method, String path, ApiRequest body, TypeReference<T> responseType) throws ApiException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json; charset=UTF-8");

            String token = SessionManager.getInstance().getToken();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }

            HttpRequest.BodyPublisher bodyPublisher = (body != null)
                    ? HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8)
                    : HttpRequest.BodyPublishers.noBody();

            builder.method(method.name(), bodyPublisher);

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

    private static <T> T parseResponse(HttpResponse<String> response, TypeReference<T> responseType) throws ApiException, IOException {
        JsonNode root = objectMapper.readTree(response.body());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.convertValue(root, objectMapper.getTypeFactory().constructType(responseType));
        }

        String errorMessage = root.has("error") ? root.get("error").asText() : "خطای نامشخصی رخ داد";
        throw new ApiException(errorMessage, response.statusCode());
    }
}