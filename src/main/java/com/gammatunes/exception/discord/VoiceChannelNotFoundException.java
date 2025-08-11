package com.gammatunes.exception.discord;

public class VoiceChannelNotFoundException extends RuntimeException {
    public VoiceChannelNotFoundException(String id) {
        super("Voice channel not found: " + id);
    }
}
