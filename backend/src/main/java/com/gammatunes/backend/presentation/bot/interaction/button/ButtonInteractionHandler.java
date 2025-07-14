package com.gammatunes.backend.presentation.bot.interaction.button;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ButtonInteractionHandler extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ButtonInteractionHandler.class);
    private final Map<String, ButtonHandler> buttonHandlers;

    public ButtonInteractionHandler(List<ButtonHandler> beans) {
        buttonHandlers = beans.stream().collect(Collectors.toMap(ButtonHandler::id, Function.identity()));
        log.info("Registered {} button handlers", buttonHandlers.size());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        Member member = event.getMember();
        if (member == null) {
           event.reply("Guild button").setEphemeral(true).queue();
           return;
        }
        ButtonHandler handler = buttonHandlers.get(event.getComponentId());
        if (handler == null) {
           log.warn("No handler found for button interaction with ID '{}'", event.getComponentId());
           event.reply("Unknown button interaction.").setEphemeral(true).queue();
           return;
       }
        try {
            log.info("Handling button interaction '{}' for user {}", event.getComponentId(), member.getUser().getName());
            handler.handle(event, member);
        } catch (Exception e) {
            log.error("An error occurred while handling button interaction '{}'", event.getComponentId(), e);
            event.reply("An unexpected error occurred. Please try again later.").setEphemeral(true).queue();
       }
    }
}
