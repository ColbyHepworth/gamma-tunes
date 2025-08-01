package com.gammatunes.backend.presentation.bot.player.view;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import org.springframework.stereotype.Component;

import static com.gammatunes.backend.presentation.ui.UiConstants.*;

@Component
public final class StatusMessageMapper {

    private StatusMessageMapper() {}

    public String toStatus(PlayerOutcome o) {
        return switch (o) {
            case ADDED_TO_QUEUE -> "✅ Added to queue";
            case PLAYING_NOW -> "▶️ Playing now";
            case SKIPPED -> SKIP + " Skipped to next track";
            case NO_NEXT_TRACK -> "❌ Nothing to skip – queue is empty";
            case REPEATED -> "🔁 Repeated current track";
            case REPEAT_ENABLED -> "🔁 Repeat mode enabled";
            case REPEAT_DISABLED -> "🔁 Repeat mode disabled";
            case PLAYING_PREVIOUS -> PREVIOUS + " Playing previous track";
            case NO_PREVIOUS_TRACK -> "❌ No previous track";
            case PAUSED -> PAUSE + " Paused";
            case ALREADY_PAUSED -> "ℹ️ Already paused";
            case RESUMED -> PLAY + " Resumed";
            case ALREADY_PLAYING -> "ℹ️ Already playing";
            case STOPPED -> STOP + " Stopped player";
            case ALREADY_STOPPED -> "ℹ️ Already stopped";
            case QUEUE_CLEARED -> "🗑️ Cleared queue";
            case QUEUE_EMPTY -> "ℹ️ Queue empty";
            case SHUFFLED -> "🔀 Shuffled queue";
            case ERROR -> "⚠️ Unexpected error – check logs!";
        };
    }
}
