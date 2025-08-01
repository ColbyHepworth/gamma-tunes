package com.gammatunes.backend.presentation.bot.interaction.button;

import com.gammatunes.backend.domain.model.PlayerOutcome;
import com.gammatunes.backend.presentation.bot.player.view.StatusMessageMapper;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ButtonInteractionHandler extends ListenerAdapter {

    private final Map<String, Button> buttons;
    private final StatusMessageMapper statusMessageMapper;

    /**
     * Constructor to initialize the button interaction handler with a list of button beans.
     * It collects the buttons into a map for easy access by their IDs.
     *
     * @param buttonBeans List of Button beans to register.
     * @param statusMessageMapper Mapper to convert PlayerOutcome to status messages.
     */
    public ButtonInteractionHandler(List<Button> buttonBeans, StatusMessageMapper statusMessageMapper) {
        this.buttons = buttonBeans.stream()
            .collect(Collectors.toMap(Button::id, Function.identity()));
        this.statusMessageMapper = statusMessageMapper;
        log.info("Registered {} player buttons.", buttons.size());
    }

    /**
     * Handles button interactions in Discord.
     * This method is triggered when a user clicks a button in the player panel.
     * It processes the interaction and updates the player state accordingly.
     *
     * @param e The ButtonInteractionEvent containing details about the interaction.
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent e) {
        var member = e.getMember();
        if (member == null) {
            e.reply("Guild only.").setEphemeral(true).queue();
            return;
        }

        String id = e.getComponentId();
        try {
            if (buttons.containsKey(id)) {
                e.deferEdit().queue();

                PlayerOutcome outcome = buttons.get(id).handle(e, member);

                if (outcome != null && !isStateChanging(outcome)) {
                    String toastMessage = statusMessageMapper.toStatus(outcome);

                    e.getHook().sendMessage(toastMessage).setEphemeral(true)
                        .flatMap(message -> message.delete().delay(5, TimeUnit.SECONDS))
                        .queue();
                }

            } else {
                e.reply("Unknown button.").setEphemeral(true).queue();
            }
        } catch (Exception ex) {
            log.error("Error handling button '{}'", id, ex);
            e.getHook().sendMessage("❌ Unexpected error.").setEphemeral(true).queue();
        }
    }

    /**
     * Helper to determine if an outcome should trigger a full panel refresh.
     */
    private boolean isStateChanging(PlayerOutcome outcome) {
        return switch (outcome) {
            case ADDED_TO_QUEUE, ALREADY_PAUSED, ALREADY_PLAYING, NO_NEXT_TRACK, NO_PREVIOUS_TRACK, QUEUE_EMPTY -> false;
            default -> true;
        };
    }
}
