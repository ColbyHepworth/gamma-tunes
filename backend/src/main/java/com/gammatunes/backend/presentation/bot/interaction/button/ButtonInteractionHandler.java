package com.gammatunes.backend.presentation.bot.interaction.button;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ButtonInteractionHandler extends ListenerAdapter {

    private final Map<String, Button> buttons;

    public ButtonInteractionHandler(List<Button> buttonBeans) {
        this.buttons = buttonBeans.stream()
            .collect(Collectors.toMap(Button::id, Function.identity()));
        log.info("Registered {} player buttons.", buttons.size());
    }

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
                buttons.get(id).handle(e, member);
            } else {
                e.reply("Unknown button.").setEphemeral(true).queue();
            }
        } catch (Exception ex) {
            log.error("Error handling button '{}'", id, ex);
            e.getHook().sendMessage("❌ Unexpected error.").setEphemeral(true).queue();
        }
    }
}
