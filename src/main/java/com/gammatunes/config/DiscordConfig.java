package com.gammatunes.config;

import com.gammatunes.component.discord.interaction.button.ButtonInteractionHandler;
import com.gammatunes.component.discord.interaction.command.BotCommand;
import com.gammatunes.component.discord.interaction.command.CommandInteractionHandler;
import com.gammatunes.component.discord.interaction.selectmenu.SelectMenuInteractionHandler;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration class for setting up the Discord bot using JDA (Java Discord API).
 * This class initializes
 * the JDA instance, configures it with the bot token, and registers interaction handlers.
 * It also updates the slash commands with Discord when the application context is refreshed.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DiscordConfig {

    private final LavalinkClient lavalinkClient;

    /**
     * Creates and configures the JDA instance for the Discord bot.
     * This method reads the bot token from environment variables and initializes JDA with it.
     * It also sets the bot's activity to "listening to your commands".
     *
     * @return The initialized JDA instance, or null if the token is not configured.
     * @throws InterruptedException If the JDA initialization is interrupted.
     */
    @Bean
    public JDA jda() throws InterruptedException {
        Dotenv.configure().ignoreIfMissing().load();
        String botToken = System.getenv("DISCORD_BOT_TOKEN");

        if (botToken == null || botToken.isEmpty()) {
            log.warn("DISCORD_BOT_TOKEN is not configured. JDA will not be started.");
            return null;
        }

        log.info("Starting JDA bot...");
        JDA jda = JDABuilder.createDefault(botToken)
            .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
            .enableCache(CacheFlag.VOICE_STATE)
            .setActivity(Activity.listening("/play"))
            .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkClient))
            .build()
            .awaitReady();

        log.info("Bot is ready! Logged in as {}", jda.getSelfUser().getName());
        return jda;
    }

    /**
     * Registers interaction handlers and updates slash commands when the application context is refreshed.
     * This method is triggered after the JDA instance is created and ready.
     *
     * @param event The ContextRefreshedEvent containing the application context.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        JDA jda = event.getApplicationContext().getBean(JDA.class);

        ButtonInteractionHandler buttonInteractionHandler = event.getApplicationContext().getBean(ButtonInteractionHandler.class);
        jda.addEventListener(buttonInteractionHandler);

        CommandInteractionHandler commandInteractionHandler = event.getApplicationContext().getBean(CommandInteractionHandler.class);
        jda.addEventListener(commandInteractionHandler);

        SelectMenuInteractionHandler selectMenuInteractionHandler = event.getApplicationContext().getBean(SelectMenuInteractionHandler.class);
        jda.addEventListener(selectMenuInteractionHandler);

        List<BotCommand> botCommands = event.getApplicationContext().getBeanProvider(BotCommand.class).stream().toList();

        log.info("Updating slash commands with Discord...");
        jda.updateCommands()
            .addCommands(botCommands.stream().map(BotCommand::getCommandData).collect(Collectors.toList()))
            .queue(
                success -> log.info("Successfully updated {} slash commands.", success.size()),
                error -> log.error("Failed to update slash commands.", error)
            );
    }
}
