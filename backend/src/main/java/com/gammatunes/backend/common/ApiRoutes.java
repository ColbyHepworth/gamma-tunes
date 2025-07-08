package com.gammatunes.backend.common;

/**
 * A shared class that defines the API route constants for the application.
 * This provides a single source of truth for both the backend and any clients.
 */
public final class ApiRoutes {

    // Private constructor to prevent instantiation
    private ApiRoutes() {}

    public static final String SESS_ID = "{sessionId}";
    private static final String BASE_MUSIC_URL = "/api/v1/music/" + SESS_ID;

    public static final String PLAY = BASE_MUSIC_URL + "/play";
    public static final String PAUSE = BASE_MUSIC_URL + "/pause";
    public static final String RESUME = BASE_MUSIC_URL + "/resume";
    public static final String STOP = BASE_MUSIC_URL + "/stop";
    public static final String SKIP = BASE_MUSIC_URL + "/skip";
    public static final String STATUS = BASE_MUSIC_URL + "/status";

    /**
     * Helper method to build a URL with the session ID.
     * @param route The route constant (e.g., ApiRoutes.PLAY).
     * @param sessionId The session ID to insert into the route.
     * @return The fully constructed URL path.
     */
    public static String build(String route, String sessionId) {
        return route.replace(SESS_ID, sessionId);
    }
}
