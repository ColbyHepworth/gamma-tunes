package com.gammatunes.component.discord.interaction.command.player;

import com.gammatunes.component.discord.interaction.command.AbstractBotCommand;
import com.gammatunes.service.PlayerPanelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetPlayerCommandHandler extends AbstractBotCommand {

    private final PlayerPanelService panelCoordinator;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("player", "Displays the interactive music player.");
    }

    @Override
    protected Mono<Void> handleWork(SlashCommandInteractionEvent event) {
        Guild guild = Objects.requireNonNull(event.getGuild(), "Guild cannot be null");
        TextChannel channel = (TextChannel) event.getChannel();

        log.info("Slash sanity: guildId={} channelId={} type={}",
            guild.getIdLong(), channel.getIdLong(), channel.getType());


        return panelCoordinator.createPanel(guild.getIdLong(), channel);
    }

    @Override
    public Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        return Mono.just(CommandResult.toast("Player has been added to the channel!", true));
    }
}
