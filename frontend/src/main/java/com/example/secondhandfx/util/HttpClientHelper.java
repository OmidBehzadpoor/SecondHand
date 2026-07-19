package com.example.secondhandfx.util;

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
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class HttpClientHelper {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private HttpClientHelper() {
    }

    public static <T> T get(String path, TypeReference<T> responseType) throws ApiException {
        return send("GET", path, null, responseType);
    }

    public static <T> T post(String path, Object body, TypeReference<T> responseType) throws ApiException {
        return send("POST", path, body, responseType);
    }

    public static <T> T put(String path, Object body, TypeReference<T> responseType) throws ApiException {
        return send("PUT", path, body, responseType);
    }

    public static <T> T patch(String path, Object body, TypeReference<T> responseType) throws ApiException {
        return send("PATCH", path, body, responseType);
    }

    public static <T> T delete(String path, TypeReference<T> responseType) throws ApiException {
        return send("DELETE", path, null, responseType);
    }

    private static <T> T send(String method, String path, Object body, TypeReference<T> responseType) throws ApiException {
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

            builder.method(method, bodyPublisher);

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            return parseResponse(response, responseType);

        } catch (ConnectException e) {
            throw new ApiException("امکان برقراری ارتباط با سرور وجود ندارد. لطفاً مطمئن شوید سرور در حال اجراست.", 0);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("خطا در پردازش داده‌ی ارسالی یا دریافتی از سرور رخ داد.", 0);
        }
    }

    private static <T> T parseResponse(HttpResponse<String> response, TypeReference<T> responseType) throws ApiException, IOException {
        JsonNode root = objectMapper.readTree(response.body());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode dataNode = root.get("data");
            if (dataNode == null || dataNode.isNull()) {
                return null;
            }
            return objectMapper.convertValue(dataNode, objectMapper.getTypeFactory().constructType(responseType));
        }

        String errorMessage = root.has("error") ? root.get("error").asText() : "خطای نامشخصی رخ داد";
        throw new ApiException(errorMessage, response.statusCode());
    }
}