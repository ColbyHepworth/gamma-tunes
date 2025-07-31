package com.gammatunes.backend.domain.exception;

public class MemberNotInVoiceChannelException extends RuntimeException {
    public MemberNotInVoiceChannelException(String message) {
        super(message);
    }
}
