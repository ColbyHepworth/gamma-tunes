package com.gammatunes.component.audio.events;

/**
 * Discrete, user-facing outcomes of player commands or playback transitions.
 * Keep this small and intentional; use PlayerStatus for rich state.
 */
public enum PlayerOutcome {

    /* ────────── enqueue / start ────────── */
    ENQUEUED,
    PLAY_STARTED,

    /* ────────── navigation ─────────────── */
    SKIPPED,
    COMPLETED,
    NO_NEXT,
    PREVIOUS,
    NO_PREVIOUS,
    JUMPED_TO_TRACK,
    INVALID_JUMP,

    /* ────────── pause / resume ─────────── */
    PAUSED,
    ALREADY_PAUSED,
    RESUMED,
    ALREADY_PLAYING,

    /* ────────── stop / queue mgmt ──────── */
    STOPPED,
    QUEUE_CLEARED,
    QUEUE_ALREADY_EMPTY,

    /* ────────── modes / settings ───────── */
    REPEAT_ON,
    REPEAT_OFF,
    REPEATING,
    SHUFFLED,

    /* ────────── error paths ────────────── */
    LOAD_FAILED,
    ERROR
}
