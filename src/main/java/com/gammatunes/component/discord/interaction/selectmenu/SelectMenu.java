package com.gammatunes.component.discord.interaction.selectmenu;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public interface SelectMenu {
    String id();
    void handle(StringSelectInteractionEvent event, Member member);
}
