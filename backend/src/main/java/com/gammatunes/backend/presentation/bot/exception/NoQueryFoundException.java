package com.gammatunes.backend.presentation.bot.exception;

public class NoQueryFoundException extends RuntimeException {
    public NoQueryFoundException() {
        super("No query found for the current voice channel.");
    }
}
