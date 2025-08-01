package com.gammatunes.backend.presentation.bot.event;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.player.event.PlayerStateChanged;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.presentation.bot.player.service.PlayerPanelCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.gammatunes.backend.presentation.ui.UiConstants.*;
import static com.gammatunes.backend.presentation.ui.UiConstants.PAUSE;
import static com.gammatunes.backend.presentation.ui.UiConstants.PLAY;
import static com.gammatunes.backend.presentation.ui.UiConstants.STOP;

@Component
@RequiredArgsConstructor
public class DiscordPlayerStatusBridge {

    private final PlayerPanelCoordinator panelCoordinator;

    @EventListener
    public void on(PlayerStateChanged event) {

        /* Convert outcome → friendly text for the status line */
        String status = toStatusText(event.outcome());

        /* Tell the message service to update—or create—the embed */
        panelCoordinator.publishStatus(
            new Session(event.sessionId()), status);
    }

    private String toStatusText(PlayerOutcome o) {
        return switch (o) {

            /* ─── Play / Add ─── */
            case ADDED_TO_QUEUE -> "✅ Added to queue";
            case PLAYING_NOW -> "▶️ Playing now";

            /* ─── Skip / Next ─── */

            case SKIPPED             -> SKIP + " Skipped to next track";
            case NO_NEXT_TRACK       -> "❌ Nothing to skip – queue is empty";

            /* ─── Repeat ─── */
            case REPEATED            -> "🔁 Repeated current track";
            case REPEAT_ENABLED      -> "🔁 Repeat mode enabled";
            case REPEAT_DISABLED     -> "🔁 Repeat mode disabled";

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
            case QUEUE_EMPTY         -> "ℹ️ Queue empty";

            /* ─── Fallback ─── */
            case SHUFFLED            -> "🔀 Shuffled queue";
            case ERROR               -> "⚠️ Unexpected error – check logs!";
        };
    }
}
