package com.gammatunes.component.discord.interaction.command.player;

import com.gammatunes.component.discord.interaction.command.AbstractBotCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import reactor.core.publisher.Mono;

public abstract class PlayerCommand extends AbstractBotCommand {

    @Override
    protected final Mono<Void> handleWork(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            return Mono.error(new IllegalStateException("This command can only be used in a server."));
        }
        return handle(member, event);
    }

    protected abstract Mono<Void> handle(Member member, SlashCommandInteractionEvent event);


}
