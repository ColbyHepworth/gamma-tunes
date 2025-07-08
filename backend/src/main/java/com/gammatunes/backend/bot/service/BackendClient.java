package com.gammatunes.backend.bot.service;

import com.gammatunes.backend.common.ApiRoutes;
import com.gammatunes.backend.common.dto.PlayRequest;
import com.gammatunes.backend.common.dto.PlayerStatusResponse;
import com.gammatunes.backend.common.dto.StatusResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * An HTTP client responsible for all communication with our Spring Boot backend API.
 */
public class BackendClient {

    private static final Logger log = LoggerFactory.getLogger(BackendClient.class);
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final HttpUrl backendApiUrl;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public BackendClient(String backendApiUrl) {
        this.backendApiUrl = Objects.requireNonNull(HttpUrl.parse(backendApiUrl), "Invalid backend API URL");
        this.httpClient = new OkHttpClient();
        // Create a Gson instance that can handle Java's Duration class
        this.gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, typeOfT, context) -> Duration.ofMillis(json.getAsLong()))
            .create();
    }

    public void play(String sessionId, String query) throws IOException {
        PlayRequest playRequest = new PlayRequest(query);
        String jsonBody = gson.toJson(playRequest);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
            .url(Objects.requireNonNull(backendApiUrl.resolve(ApiRoutes.build(ApiRoutes.PLAY, sessionId))))
            .post(body)
            .build();
        executeRequest(request, "play");
    }

    public void pause(String sessionId) throws IOException {
        Request request = new Request.Builder()
            .url(Objects.requireNonNull(backendApiUrl.resolve(ApiRoutes.build(ApiRoutes.PAUSE, sessionId))))
            .post(RequestBody.create("", null))
            .build();
        executeRequest(request, "pause");
    }

    public void resume(String sessionId) throws IOException {
        Request request = new Request.Builder()
            .url(Objects.requireNonNull(backendApiUrl.resolve(ApiRoutes.build(ApiRoutes.RESUME, sessionId))))
            .post(RequestBody.create("", null))
            .build();
        executeRequest(request, "resume");
    }

    public void stop(String sessionId) throws IOException {
        Request request = new Request.Builder()
            .url(Objects.requireNonNull(backendApiUrl.resolve(ApiRoutes.build(ApiRoutes.STOP, sessionId))))
            .post(RequestBody.create("", null))
            .build();
        executeRequest(request, "stop");
    }

    public void skip(String sessionId) throws IOException {
        Request request = new Request.Builder()
            .url(Objects.requireNonNull(backendApiUrl.resolve(ApiRoutes.build(ApiRoutes.SKIP, sessionId))))
            .post(RequestBody.create("", null))
            .build();
        executeRequest(request, "skip");
    }

    public PlayerStatusResponse getStatus(String sessionId) throws IOException {
        Request request = new Request.Builder()
            .url(Objects.requireNonNull(backendApiUrl.resolve(ApiRoutes.build(ApiRoutes.STATUS, sessionId))))
            .get()
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                log.error("Failed to get status for session {}: {} - {}", sessionId, response.code(), errorBody);
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = Objects.requireNonNull(response.body()).string();
            return gson.fromJson(responseBody, PlayerStatusResponse.class);
        }
    }

    private void executeRequest(Request request, String action) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (!response.isSuccessful()) {
                log.error("Failed to {} for session: {} - {}", action, response.code(), responseBody);
                throw new IOException("Unexpected code " + response);
            }
            StatusResponse status = gson.fromJson(responseBody, StatusResponse.class);
            log.info("Backend response for action '{}': {}", action, status.status());
        }
    }
}
