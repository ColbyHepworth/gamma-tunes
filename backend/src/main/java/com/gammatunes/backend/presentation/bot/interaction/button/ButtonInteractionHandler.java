package com.gammatunes.backend.presentation.bot.interaction.button;

import com.gammatunes.backend.domain.model.Session;
import com.gammatunes.backend.presentation.bot.player.service.PlayerMessageService;
import com.gammatunes.backend.presentation.bot.player.view.StatusMessageMapper;
import com.gammatunes.backend.presentation.bot.player.view.dto.PlayerOutcomeResult;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ButtonInteractionHandler extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ButtonInteractionHandler.class);

    private final Map<String, ButtonHandler> buttons;
    private final PlayerMessageService playerView;

    public ButtonInteractionHandler(List<ButtonHandler> buttons, PlayerMessageService view) {

        this.buttons = buttons.stream()
            .collect(Collectors.toMap(ButtonHandler::id, Function.identity()));
        this.playerView = view;

        logger.info("Registered {} player buttons.", this.buttons.size());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent e) {

        Member m = e.getMember();
        if (m == null) { e.reply("Guild only.").setEphemeral(true).queue(); return; }

        String id = e.getComponentId();
        try {
            if (buttons.containsKey(id)) {
                e.deferEdit().queue();                       // acknowledge

                PlayerOutcomeResult result = buttons.get(id).handle(e, m);

                String   status = StatusMessageMapper.toStatus(result.outcome(), result.details());
                Session  session    = new Session(Objects.requireNonNull(e.getGuild()).getId());
                playerView.publishStatus(session, status);
            } else {
                e.reply("Unknown button.").setEphemeral(true).queue();
            }

        } catch (Exception ex) {
            logger.error("Error handling button '{}'", id, ex);
            e.reply("❌ Unexpected error.").setEphemeral(true).queue();
        }
    }
}

