package com.gammatunes.backend.domain.model;

/**
 * Unified result codes for every Audio-Player use-case.
 * Keep them semantic – never “true / false”.
 */
public enum PlayerOutcome {

    /* ────────── play / enqueue ────────── */
    ADDED_TO_QUEUE,          // track accepted, normal queueing
    PLAYING_NOW,             // played immediately (play-now, empty queue)

    /* ────────── skip / next ──────────── */
    SKIPPED,                 // went to next track
    NO_NEXT_TRACK,           // queue was empty

    /* ────────── previous ─────────────── */
    PLAYING_PREVIOUS,        // went back one track
    NO_PREVIOUS_TRACK,       // nothing in history

    /* ────────── pause / resume ───────── */
    PAUSED,                  // player paused
    ALREADY_PAUSED,          // was paused already
    RESUMED,                 // playback resumed
    ALREADY_PLAYING,         // was playing already

    /* ────────── stop / clear ─────────── */
    STOPPED,                 // playback stopped + queue cleared
    ALREADY_STOPPED,        // player was already stopped
    QUEUE_CLEARED,           // queue emptied but current track kept
    QUEUE_EMPTY,             // queue was already empty

    /* ────────── generic failure ──────── */
    ERROR                    // unexpected/unclassified failure
}
