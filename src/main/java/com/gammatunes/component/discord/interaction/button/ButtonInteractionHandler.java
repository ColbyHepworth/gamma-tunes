package com.gammatunes.component.discord.interaction.button;

import com.gammatunes.component.discord.ui.MessageResolver;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles button interactions in Discord by mapping button IDs to their respective handlers.
 * This class listens for button interactions and invokes the appropriate handler based on the button ID.
 * If an error occurs during handling, it replies with an error message.
 */
@Slf4j
@Component
public class ButtonInteractionHandler extends ListenerAdapter {

    private final Map<String, Button> buttons;

    /**
     * Constructor to initialize the button interaction handler with a list of button beans.
     * It collects the buttons into a map for easy access by their IDs.
     *
     * @param buttonBeans     List of Button beans to register.
     * @param messageResolver Mapper to convert PlayerOutcome to status messages.
     */
    public ButtonInteractionHandler(List<Button> buttonBeans, MessageResolver messageResolver) {
        this.buttons = buttonBeans.stream()
            .collect(Collectors.toMap(Button::id, Function.identity()));
        log.info("Registered {} player buttons.", buttons.size());
    }

    /**
     * Handles button interactions by looking up the button handler by its ID and invoking it.
     * If the button is not found or an error occurs, it replies with an appropriate message.
     *
     * @param event The ButtonInteractionEvent containing the interaction details.
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            event.reply("Guild only.").setEphemeral(true).queue();
            return;
        }

        String id = event.getComponentId();
        Button handler = buttons.get(id);
        if (handler == null) {
            event.reply("Unknown button.").setEphemeral(true).queue();
            return;
        }

        try {
            handler.handle(event, member);
        } catch (Exception exception) {
            log.error("Error handling button '{}'", id, exception);
            if (!event.isAcknowledged()) {
                event.reply("❌ Unexpected error.").setEphemeral(true).queue();
            } else {
                event.getHook().sendMessage("❌ Unexpected error.").setEphemeral(true).queue();
            }
        }
    }
}
