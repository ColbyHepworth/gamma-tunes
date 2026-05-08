package com.gammatunes.component.discord.interaction.command.spotify;

import com.gammatunes.component.discord.interaction.command.AbstractBotCommand;
import com.gammatunes.service.SpotifyAccountLinkService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SpotifyCommand extends AbstractBotCommand {

    private static final String CONNECT_SUBCOMMAND = "connect";

    private final SpotifyAccountLinkService spotifyAccountLinkService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("spotify", "Manage Spotify integration.")
            .addSubcommands(new SubcommandData(CONNECT_SUBCOMMAND, "Connect your Spotify account."));
    }

    @Override
    protected Mono<Void> handleWork(SlashCommandInteractionEvent event) {
        if (!CONNECT_SUBCOMMAND.equals(event.getSubcommandName())) {
            return Mono.error(new IllegalArgumentException("Unknown Spotify subcommand."));
        }
        return Mono.empty();
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        Member member = Objects.requireNonNull(event.getMember(), "Member cannot be null");
        Guild guild = Objects.requireNonNull(event.getGuild(), "Guild cannot be null");

        URI authorizeUri = spotifyAccountLinkService.createAuthorizationUri(
            member.getIdLong(),
            guild.getIdLong()
        );

        return Mono.just(CommandResult.toast(
            "Connect your Spotify account:\n" + authorizeUri,
            true
        ));
    }
}
