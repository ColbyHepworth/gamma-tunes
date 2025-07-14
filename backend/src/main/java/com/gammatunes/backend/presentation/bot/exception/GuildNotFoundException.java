package com.gammatunes.backend.presentation.bot.exception;

public class GuildNotFoundException extends RuntimeException {
    public GuildNotFoundException(String id) {
        super("Guild not found: " + id);
    }
}
