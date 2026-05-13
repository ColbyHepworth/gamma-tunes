package com.gammatunes.service.playback.control;

public record PlaybackControlRequest(
    PlaybackControlAction action,
    Long positionMs
) {
    public static PlaybackControlRequest of(PlaybackControlAction action) {
        return new PlaybackControlRequest(action, null);
    }

    public static PlaybackControlRequest seek(long positionMs) {
        return new PlaybackControlRequest(PlaybackControlAction.SEEK, positionMs);
    }

    public long requiredPositionMs() {
        if (positionMs == null) {
            throw new IllegalArgumentException("Seek position is required.");
        }
        return positionMs;
    }
}
