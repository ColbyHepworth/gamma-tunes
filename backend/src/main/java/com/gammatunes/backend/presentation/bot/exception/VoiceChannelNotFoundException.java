package com.gammatunes.backend.presentation.bot.exception;

public class VoiceChannelNotFoundException extends RuntimeException {
    public VoiceChannelNotFoundException(String id) {
        super("Voice channel not found: " + id);
    }
}
