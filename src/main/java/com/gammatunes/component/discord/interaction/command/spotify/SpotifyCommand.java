package com.gammatunes.component.discord.interaction.command.spotify;

import com.gammatunes.component.discord.interaction.command.AbstractBotCommand;
import com.gammatunes.exception.player.MemberNotInVoiceChannelException;
import com.gammatunes.service.SpotifyAccountLinkService;
import com.gammatunes.service.SpotifyControlPlaybackService;
import com.gammatunes.service.SpotifyControlService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SpotifyCommand extends AbstractBotCommand {

    private static final String CONNECT_SUBCOMMAND = "connect";
    private static final String CONTROL_GROUP = "control";
    private static final String CONTROL_START_SUBCOMMAND = "start";
    private static final String CONTROL_STOP_SUBCOMMAND = "stop";

    private final SpotifyAccountLinkService spotifyAccountLinkService;
    private final SpotifyControlService spotifyControlService;
    private final SpotifyControlPlaybackService spotifyControlPlaybackService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("spotify", "Manage Spotify integration.")
            .addSubcommands(new SubcommandData(CONNECT_SUBCOMMAND, "Connect your Spotify account."))
            .addSubcommandGroups(new SubcommandGroupData(CONTROL_GROUP, "Manage Spotify control for this server.")
                .addSubcommands(
                    new SubcommandData(CONTROL_START_SUBCOMMAND, "Use your Spotify account to control this server."),
                    new SubcommandData(CONTROL_STOP_SUBCOMMAND, "Stop Spotify control for this server.")
                ));
    }

    @Override
    protected Mono<Void> handleWork(SlashCommandInteractionEvent event) {
        if (isConnect(event) || isControlStart(event) || isControlStop(event)) {
            return Mono.empty();
        }

        return Mono.error(new IllegalArgumentException("Unknown Spotify subcommand."));
    }

    @Override
    protected Mono<CommandResult> resultAfterSuccess(SlashCommandInteractionEvent event) {
        if (isConnect(event)) {
            return Mono.just(CommandResult.toast(connectMessage(event), true));
        }

        if (isControlStart(event)) {
            return startControl(event);
        }

        if (isControlStop(event)) {
            return stopControl(event);
        }

        return Mono.error(new IllegalArgumentException("Unknown Spotify subcommand."));
    }

    private Mono<CommandResult> startControl(SlashCommandInteractionEvent event) {
        Member member = member(event);
        Guild guild = guild(event);
        long voiceChannelId = voiceChannelId(member);
        Long textChannelId = event.getChannel() instanceof TextChannel textChannel
            ? textChannel.getIdLong()
            : null;

        return spotifyControlService.startControl(guild.getIdLong(), member.getIdLong(), voiceChannelId, textChannelId)
            .flatMap(session -> spotifyControlPlaybackService.syncNow(session.guildId())
                .onErrorResume(error -> Mono.empty())
                .thenReturn(CommandResult.toast(
                    "Spotify control enabled. This server is now using your Spotify account.",
                    true
                )))
            .onErrorResume(IllegalArgumentException.class, error -> Mono.just(CommandResult.toast(
                connectMessage(event) + "\n\nRun `/spotify control start` again after connecting.",
                true
            )));
    }

    private Mono<CommandResult> stopControl(SlashCommandInteractionEvent event) {
        Guild guild = guild(event);

        return spotifyControlService.stopControl(guild.getIdLong())
            .thenReturn(CommandResult.toast("Spotify control disabled.", true));
    }

    private String connectMessage(SlashCommandInteractionEvent event) {
        Member member = member(event);
        Guild guild = guild(event);

        URI authorizeUri = spotifyAccountLinkService.createAuthorizationUri(
            member.getIdLong(),
            guild.getIdLong()
        );

        return "Connect your Spotify account:\n" + authorizeUri;
    }

    private boolean isConnect(SlashCommandInteractionEvent event) {
        return event.getSubcommandGroup() == null
            && CONNECT_SUBCOMMAND.equals(event.getSubcommandName());
    }

    private boolean isControlStart(SlashCommandInteractionEvent event) {
        return CONTROL_GROUP.equals(event.getSubcommandGroup())
            && CONTROL_START_SUBCOMMAND.equals(event.getSubcommandName());
    }

    private boolean isControlStop(SlashCommandInteractionEvent event) {
        return CONTROL_GROUP.equals(event.getSubcommandGroup())
            && CONTROL_STOP_SUBCOMMAND.equals(event.getSubcommandName());
    }

    private Member member(SlashCommandInteractionEvent event) {
        return Objects.requireNonNull(event.getMember(), "Member cannot be null");
    }

    private Guild guild(SlashCommandInteractionEvent event) {
        return Objects.requireNonNull(event.getGuild(), "Guild cannot be null");
    }

    private long voiceChannelId(Member member) {
        var voiceState = member.getVoiceState();
        if (voiceState == null || voiceState.getChannel() == null) {
            throw new MemberNotInVoiceChannelException("Member must be in a voice channel to start Spotify control.");
        }
        return voiceState.getChannel().getIdLong();
    }
}
