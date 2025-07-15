package com.gammatunes.backend.presentation.bot.player.view;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import static com.gammatunes.backend.presentation.ui.UiConstants.*;

/**
 * Turns a {@link PlayerOutcome} (plus optional details) into the
 * one-liner that appears in the player embed's “Status” field.
 */
public final class StatusMessageMapper {

    private StatusMessageMapper() { }

    public static String toStatus(PlayerOutcome o, String details) {
        return switch (o) {

            /* ─── Play / Enqueue ─── */
            case ADDED_TO_QUEUE      -> "➕ Added to queue: " + detailsOrBlank(details);
            case PLAYING_NOW         -> PLAY + " Playing now: " + detailsOrBlank(details);

            /* ─── Skip / Next ─── */
            case SKIPPED             -> SKIP + " Skipped to next track";
            case NO_NEXT_TRACK       -> "❌ Nothing to skip – queue is empty";

            /* ─── Previous ─── */
            case PLAYING_PREVIOUS    -> PREVIOUS + " Playing previous track";
            case NO_PREVIOUS_TRACK   -> "❌ No previous track";

            /* ─── Pause / Resume ─── */
            case PAUSED              -> PAUSE + " Paused";
            case ALREADY_PAUSED      -> "ℹ️ Already paused";
            case RESUMED             -> PLAY  + " Resumed";
            case ALREADY_PLAYING     -> "ℹ️ Already playing";

            /* ─── Stop / Clear ─── */
            case STOPPED             -> STOP + " Stopped player";
            case ALREADY_STOPPED     -> "ℹ️ Already stopped";
            case QUEUE_CLEARED       -> "🗑️ Cleared queue";
            case QUEUE_EMPTY         -> "ℹ️ Queue already empty";

            /* ─── Fallback ─── */
            case ERROR               -> "⚠️ Unexpected error – check logs!";
        };
    }

    private static String detailsOrBlank(String d) {
        return (d == null || d.isBlank()) ? "" : d;
    }
}
