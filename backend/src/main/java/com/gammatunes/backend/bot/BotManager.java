package com.gammatunes.backend.bot;

import com.gammatunes.backend.bot.command.Command;
import com.gammatunes.backend.bot.listener.CommandListener;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


/**
 * A Spring component that manages the JDA bot lifecycle.
 * It automatically starts the bot when the Spring application boots up.
 */
@Component
public class BotManager {

    private static final Logger log = LoggerFactory.getLogger(BotManager.class);
    private final List<Command> commands;

    public BotManager(List<Command> commands) {
        this.commands = commands;
    }

    @PostConstruct
    public void startBot() {
        Dotenv.configure().ignoreIfMissing().load();
        String botToken = System.getenv("DISCORD_BOT_TOKEN");

        if (botToken == null || botToken.isEmpty()) {
            log.warn("DISCORD_BOT_TOKEN is not configured. The JDA bot will not start.");
            return;
        }

        try {
            log.info("Starting GAMMA TUNES...");
            JDA jda = JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                .setActivity(Activity.listening("to your commands"))
                .addEventListeners(new CommandListener(commands))
                .build()
                .awaitReady();

            log.info("Updating slash commands with Discord...");
            jda.updateCommands()
                .addCommands(commands.stream().map(Command::getCommandData).collect(Collectors.toList()))
                .queue(
                    success -> log.info("Successfully updated {} slash commands.", success.size()),
                    error -> log.error("Failed to update slash commands.", error)
                );

            log.info("Bot is ready! Logged in as {}", jda.getSelfUser().getName());

        } catch (InterruptedException e) {
            log.error("JDA startup was interrupted.", e);
            Thread.currentThread().interrupt();
        }
    }
}
