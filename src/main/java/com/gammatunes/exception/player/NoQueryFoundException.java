package com.gammatunes.exception.player;

public class NoQueryFoundException extends RuntimeException {
    public NoQueryFoundException() {
        super("The 'query' option is required for this command.");
    }
}
