package com.gammatunes.backend.presentation.bot.exception;

public class NoQueryFoundException extends RuntimeException {
    public NoQueryFoundException() {
        super("The 'query' option is required for this command.");
    }
}
