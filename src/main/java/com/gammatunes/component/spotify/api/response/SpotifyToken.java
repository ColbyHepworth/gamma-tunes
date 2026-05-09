package com.gammatunes.component.spotify.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyToken(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("scope")
    String scope,

    @JsonProperty("expires_in")
    long expiresIn,

    @JsonProperty("refresh_token")
    String refreshToken
) {
}
