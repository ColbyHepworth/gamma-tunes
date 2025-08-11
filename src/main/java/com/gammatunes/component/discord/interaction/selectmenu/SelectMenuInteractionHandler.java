package com.gammatunes.component.discord.interaction.selectmenu;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SelectMenuInteractionHandler extends ListenerAdapter {

    private final Map<String, SelectMenu> menus;

    public SelectMenuInteractionHandler(List<SelectMenu> menuBeans) {
        this.menus = menuBeans.stream().collect(Collectors.toMap(SelectMenu::id, Function.identity()));
        log.info("Registered {} select menus.", menus.size());
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        var handler = menus.get(event.getComponentId());
        var member = event.getMember();

        if (handler == null) {
            event.reply("Unknown menu.").setEphemeral(true).queue();
            return;
        }
        if (member == null) {
            event.reply("Guild only.").setEphemeral(true).queue();
            return;
        }

        handler.handle(event, member);
    }
}
