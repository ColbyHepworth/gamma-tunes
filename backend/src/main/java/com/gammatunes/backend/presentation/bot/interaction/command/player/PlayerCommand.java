package com.gammatunes.backend.presentation.bot.interaction.command.player;

import com.gammatunes.backend.domain.exception.MemberNotInVoiceChannelException;
import com.gammatunes.backend.presentation.bot.interaction.command.BotCommand;
import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PlayerCommand implements BotCommand {

    private static final Logger log = LoggerFactory.getLogger(PlayerCommand.class);

    protected final DiscordPlayerController discordPlayerController;

    protected PlayerCommand(DiscordPlayerController discordPlayerController) {
        this.discordPlayerController = discordPlayerController;
    }

    /**
     * Template method for execution. Handles checks and error handling.
     */
    @Override
    public final void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            event.reply("❌ You must be in a voice channel to use player commands.")
                .setEphemeral(true).queue();
            return;
        }

        try {
            event.deferReply(true).queue();
            handle(member, event);
            event.getHook().deleteOriginal().queue();
        } catch (MemberNotInVoiceChannelException e) {
            log.warn("Command '{}' failed: {}", name(), e.getMessage());
            event.getHook().sendMessage("❌ " + e.getMessage()).setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("Unhandled error in player command '{}'", name(), e);
            event.getHook().sendMessage("⚠️ An unexpected error occurred.").setEphemeral(true).queue();
        }
    }

    /**
     * The specific logic for each command, to be implemented by subclasses.
     */
    protected abstract void handle(Member member, SlashCommandInteractionEvent event) throws Exception;
}
