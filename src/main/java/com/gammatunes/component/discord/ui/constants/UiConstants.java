package com.gammatunes.component.discord.ui.constants;

/**
 * Contains constants used in the Discord UI components for the GammaTunes application.
 * This includes color codes for different player states and emoji glyphs for buttons.
 */
public final class UiConstants {

    /* ─── Colours (hex) ─── */
    public static final int COLOR_IDLE     = 0x3498db; // blue
    public static final int COLOR_PLAYING  = 0x2ecc71; // green
    public static final int COLOR_ERROR    = 0xe74c3c; // red

    /* ─── Emoji glyphs ─── */
    public static final String PREVIOUS = "⏮️";
    public static final String PLAY     = "▶️";
    public static final String PAUSE    = "⏸️";
    public static final String SKIP     = "⏭️";
    public static final String STOP     = "⏹️";
    public static final String SHUFFLE  = "🔀";
    public static final String REPEAT   = "🔁";

    private UiConstants() {}
}
