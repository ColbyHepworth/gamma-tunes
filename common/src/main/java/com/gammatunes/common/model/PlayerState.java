package com.gammatunes.common.model;

/**
 * Represents the various states a player can be in.
 */
public enum PlayerState {
    /**
     * The player is actively playing a track.
     */
    PLAYING,
    /**
     * The player has a track loaded but is currently paused.
     */
    PAUSED,
    /**
     * The player is stopped and the queue is clear. It is not playing anything.
     */
    STOPPED,
    /**
     * The player is in the process of loading a track but has not started playing yet.
     */
    LOADING
}
