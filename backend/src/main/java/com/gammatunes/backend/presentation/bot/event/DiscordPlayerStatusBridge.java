package com.gammatunes.backend.presentation.bot.event;

import com.gammatunes.backend.domain.player.event.PlayerStateChanged;
import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.presentation.bot.player.service.PlayerPanelCoordinator;
import com.gammatunes.backend.presentation.bot.player.view.StatusMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscordPlayerStatusBridge {

    private final PlayerPanelCoordinator panelCoordinator;
    private final StatusMessageMapper statusMessageMapper;

    @EventListener
    public void on(PlayerStateChanged event) {
        String status = statusMessageMapper.toStatus(event.outcome());
        panelCoordinator.publishStatus(new Session(event.sessionId()), status);
    }
}
