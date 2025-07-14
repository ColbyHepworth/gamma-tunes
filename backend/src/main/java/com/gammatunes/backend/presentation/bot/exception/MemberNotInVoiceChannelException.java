package com.gammatunes.backend.presentation.bot.exception;

public class MemberNotInVoiceChannelException extends RuntimeException {
    public MemberNotInVoiceChannelException(String message) {
        super(message);
    }
}
