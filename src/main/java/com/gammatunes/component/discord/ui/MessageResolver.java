package com.gammatunes.component.discord.ui;


import com.gammatunes.component.audio.events.PlayerOutcome;
import org.springframework.stereotype.Component;

/**
 * Resolves player outcomes to user-friendly messages for Discord.
 * This class provides a mapping from {@link PlayerOutcome} to a string message
 * that can be sent to users in Discord.
 */
@Component
public class MessageResolver {

    private static final String SKIP     = "⏭️";
    private static final String PAUSE    = "⏸️";
    private static final String PLAY     = "▶️";

    /**
     * Resolves a {@link PlayerOutcome} to a user-friendly message.
     *
     * @param playerOutcome the outcome of the player action
     * @return a string message corresponding to the player outcome
     */
    public String resolve(PlayerOutcome playerOutcome) {
        return switch (playerOutcome) {
            case ENQUEUED                   -> "✅ Added to queue";
            case PLAY_STARTED               -> PLAY  + " Playing now";
            case COMPLETED                  -> "✅ Track completed";
            case SKIPPED                    -> SKIP  + " Skipped to next track";
            case NO_NEXT                    -> "❌ Nothing to skip – queue is empty";
            case REPEATING                  -> "🔁 Repeated current track";
            case REPEAT_ON                  -> "🔁 Repeat mode enabled";
            case REPEAT_OFF                 -> "🔁 Repeat mode disabled";
            case PREVIOUS                   -> SKIP  + " Playing previous track";
            case NO_PREVIOUS                -> "❌ No previous track";
            case JUMPED_TO_TRACK            -> "✅ Jumped to track";
            case INVALID_JUMP               -> "❌ Invalid jump target – track not found";
            case PAUSED                     -> PAUSE + " Paused";
            case ALREADY_PAUSED             -> "ℹ️ Already paused";
            case RESUMED                    -> PLAY  + " Resumed";
            case ALREADY_PLAYING            -> "ℹ️ Already playing";
            case STOPPED                    -> "■ Stopped player";
            case QUEUE_CLEARED              -> "🗑️  Cleared queue";
            case QUEUE_ALREADY_EMPTY        -> "ℹ️ Queue empty";
            case SHUFFLED                   -> "🔀 Shuffled queue";
            case LOAD_FAILED -> null;
            case ERROR                      -> "⚠️ Unexpected error – check logs!";
        };
    }
}
