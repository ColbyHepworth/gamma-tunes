package com.gammatunes.backend.presentation.bot.event;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.domain.player.event.PlayerStateChanged;
import com.gammatunes.backend.presentation.bot.player.service.PlayerMessageService;
import com.gammatunes.backend.domain.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscordPlayerStatusBridge {

    private final PlayerMessageService messageService;

    @EventListener
    public void on(PlayerStateChanged event) {

        /* Convert outcome → friendly text for the status line */
        String status = toStatusText(event.outcome());

        /* Tell the message service to update—or create—the embed */
        messageService.publishStatus(
            new Session(event.sessionId()), status);
    }

    private String toStatusText(PlayerOutcome o) {
        return switch (o) {
            case SKIPPED           -> "⏭️  Skipped to next track";
            case NO_NEXT_TRACK     -> "🚫  Queue is empty";
            case PLAYING_PREVIOUS  -> "⏮️  Playing previous track";
            case PAUSED            -> "⏸️  Paused";
            case RESUMED           -> "▶️  Resumed";
            case STOPPED           -> "⏹️  Stopped";
            default                -> "";
        };
    }
}
