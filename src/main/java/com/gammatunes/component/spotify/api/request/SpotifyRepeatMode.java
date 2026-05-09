package com.gammatunes.component.spotify.api.request;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SpotifyRepeatMode {
    TRACK("track"),
    CONTEXT("context"),
    OFF("off");

    private final String value;

    SpotifyRepeatMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
