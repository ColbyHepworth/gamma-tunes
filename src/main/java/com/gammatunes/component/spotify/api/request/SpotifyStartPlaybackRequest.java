package com.gammatunes.component.spotify.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpotifyStartPlaybackRequest(
    @JsonIgnore
    String deviceId,

    @JsonProperty("context_uri")
    String contextUri,

    @JsonProperty("uris")
    List<String> uris,

    @JsonProperty("offset")
    SpotifyPlaybackOffset offset,

    @JsonProperty("position_ms")
    Integer positionMs
) {
    public SpotifyStartPlaybackRequest {
        if (deviceId != null && deviceId.isBlank()) {
            deviceId = null;
        }
        if (contextUri != null && contextUri.isBlank()) {
            contextUri = null;
        }
        uris = clean(uris);
        if (positionMs != null && positionMs < 0) {
            throw new IllegalArgumentException("Playback position cannot be negative.");
        }
    }

    @JsonIgnore
    public String path() {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/v1/me/player/play");

        if (deviceId != null) {
            uriBuilder.queryParam("device_id", deviceId.trim());
        }

        return uriBuilder.build().toUriString();
    }

    private static List<String> clean(List<String> values) {
        if (values == null) {
            return null;
        }

        List<String> cleaned = values.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(String::trim)
            .toList();

        if (cleaned.isEmpty()) {
            return null;
        }

        return cleaned;
    }
}
