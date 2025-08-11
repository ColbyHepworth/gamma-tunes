package com.gammatunes.exception.player;

public class MemberNotInVoiceChannelException extends RuntimeException {
    public MemberNotInVoiceChannelException(String message) {
        super(message);
    }
}
