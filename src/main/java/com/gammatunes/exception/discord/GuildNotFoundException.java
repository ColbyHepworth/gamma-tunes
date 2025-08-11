package com.gammatunes.exception.discord;

public class GuildNotFoundException extends RuntimeException {
    public GuildNotFoundException(String id) {
        super("Guild not found: " + id);
    }
}
