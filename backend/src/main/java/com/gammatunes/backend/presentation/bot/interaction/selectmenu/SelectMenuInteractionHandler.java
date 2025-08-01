package com.gammatunes.backend.presentation.bot.interaction.selectmenu;

import com.gammatunes.backend.presentation.bot.player.controller.DiscordPlayerController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectMenuInteractionHandler extends ListenerAdapter {

    private final DiscordPlayerController discordPlayerController;

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("player:queue-jump")) {
            log.error("{} is not a string select interaction", event.getComponentId());
            return;
        }

        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            log.error("{} is not a string select interaction", event.getComponentId());
            event.reply("You must be in a voice channel to use this feature.").setEphemeral(true).queue();
            return;
        }

        try {
            event.deferEdit().queue();

            String selectedTrackIdentifier = event.getValues().getFirst();
            discordPlayerController.jumpToTrack(member, selectedTrackIdentifier);

        } catch (Exception e) {
            log.error("Error handling select menu 'player:queue-jump'", e);
            event.getHook().sendMessage("❌ An error occurred while jumping to the track.").setEphemeral(true).queue();
        }
    }
}
